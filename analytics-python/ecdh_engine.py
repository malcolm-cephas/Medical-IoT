from cryptography.hazmat.primitives.asymmetric import ec
from cryptography.hazmat.primitives import hashes, serialization
from cryptography.hazmat.primitives.kdf.hkdf import HKDF
from cryptography.hazmat.primitives.ciphers import Cipher, algorithms, modes
from cryptography.hazmat.backends import default_backend
import os
import numpy as np
import cv2
import hashlib
import math
import pydicom
import struct
from collections import Counter
from skimage.metrics import structural_similarity as ssim
from scipy.stats import pearsonr
import base64

class ECDHEngine:
    def __init__(self):
        self.receiver_private_key = None
        self.receiver_public_key = None
        self.key_dir = "keys"
        os.makedirs(self.key_dir, exist_ok=True)
        self._load_or_generate_keys()

    def _load_or_generate_keys(self):
        priv_path = os.path.join(self.key_dir, "receiver_private.pem")
        pub_path = os.path.join(self.key_dir, "receiver_public.pem")
        
        if os.path.exists(priv_path):
            try:
                with open(priv_path, "rb") as f:
                    self.receiver_private_key = serialization.load_pem_private_key(
                        f.read(), password=None, backend=default_backend()
                    )
                self.receiver_public_key = self.receiver_private_key.public_key()
            except Exception as e:
                print(f"Error loading keys: {e}")
                self.generate_receiver_keys()
        else:
            self.generate_receiver_keys()

    def generate_receiver_keys(self):
        self.receiver_private_key = ec.generate_private_key(ec.SECP256R1(), default_backend())
        self.receiver_public_key = self.receiver_private_key.public_key()
        
        priv_path = os.path.join(self.key_dir, "receiver_private.pem")
        pub_path = os.path.join(self.key_dir, "receiver_public.pem")

        with open(priv_path, "wb") as f:
            f.write(self.receiver_private_key.private_bytes(
                encoding=serialization.Encoding.PEM,
                format=serialization.PrivateFormat.PKCS8,
                encryption_algorithm=serialization.NoEncryption()
            ))
        
        with open(pub_path, "wb") as f:
            f.write(self.receiver_public_key.public_bytes(
                encoding=serialization.Encoding.PEM,
                format=serialization.PublicFormat.SubjectPublicKeyInfo
            ))

    def derive_aes_key(self, shared_secret: bytes, salt: bytes) -> bytes:
        hkdf = HKDF(
            algorithm=hashes.SHA256(),
            length=32,
            salt=salt,
            info=b'ecdh-image-encryption',
            backend=default_backend()
        )
        return hkdf.derive(shared_secret)

    def scramble_image(self, image: np.ndarray, seed: int) -> np.ndarray:
        h, w, c = image.shape
        rng = np.random.default_rng(seed)
        indices = np.arange(h * w)
        rng.shuffle(indices)
        flat = image.reshape(-1, c)
        scrambled_flat = np.zeros_like(flat)
        scrambled_flat[indices] = flat
        return scrambled_flat.reshape(h, w, c)

    def unscramble_image(self, scrambled: np.ndarray, seed: int) -> np.ndarray:
        h, w, c = scrambled.shape
        rng = np.random.default_rng(seed)
        indices = np.arange(h * w)
        rng.shuffle(indices)
        flat = scrambled.reshape(-1, c)
        unscrambled_flat = np.zeros_like(flat)
        unscrambled_flat[np.argsort(indices)] = flat
        return unscrambled_flat.reshape(h, w, c)

    def calculate_metrics(self, img1: np.ndarray, img2: np.ndarray):
        gray1 = cv2.cvtColor(img1, cv2.COLOR_BGR2GRAY)
        gray2 = cv2.cvtColor(img2, cv2.COLOR_BGR2GRAY)
        
        if gray1.shape != gray2.shape:
            gray2 = cv2.resize(gray2, (gray1.shape[1], gray1.shape[0]))
            
        # Entropy
        def entropy(img):
            hist = Counter(img.flatten())
            total = sum(hist.values())
            return -sum((count / total) * math.log2(count / total) for count in hist.values() if count > 0)

        # UACI
        diff = np.abs(gray1.astype(np.int16) - gray2.astype(np.int16))
        uaci = np.mean(diff) / 255 * 100
        
        # NPCR
        npcr = np.sum(gray1 != gray2) / gray1.size * 100
        
        # SSIM
        ssim_val = ssim(gray1, gray2)
        
        # Correlation
        corr, _ = pearsonr(gray1.flatten(), gray2.flatten())
        
        return {
            "entropy1": entropy(gray1),
            "entropy2": entropy(gray2),
            "ssim": ssim_val,
            "correlation": corr,
            "uaci": uaci,
            "npcr": npcr
        }

    def encrypt_image_data(self, image_bytes: bytes):
        # Decode image
        nparr = np.frombuffer(image_bytes, np.uint8)
        image = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
        if image is None:
            raise ValueError("Failed to decode image")

        # Sender ephemeral keys
        sender_private = ec.generate_private_key(ec.SECP256R1(), default_backend())
        sender_public = sender_private.public_key()
        sender_pub_bytes = sender_public.public_bytes(
            encoding=serialization.Encoding.DER,
            format=serialization.PublicFormat.SubjectPublicKeyInfo
        )

        # Shared secret and AES key
        shared = sender_private.exchange(ec.ECDH(), self.receiver_public_key)
        salt = os.urandom(16)
        aes_key = self.derive_aes_key(shared, salt)

        # Scramble
        seed = int.from_bytes(hashlib.sha256(aes_key + salt).digest()[:4], 'big')
        scrambled = self.scramble_image(image, seed)
        
        # AES-GCM Encrypt
        plaintext = scrambled.tobytes()
        iv = os.urandom(12)
        encryptor = Cipher(algorithms.AES(aes_key), modes.GCM(iv), backend=default_backend()).encryptor()
        ciphertext = encryptor.update(plaintext) + encryptor.finalize()
        tag = encryptor.tag

        # Header: shape(6) | salt(16) | sender_pub_len(2) | sender_pub | iv(12) | tag(16)
        shape_bytes = np.array(scrambled.shape, dtype=np.uint16).tobytes()
        sender_len = struct.pack(">H", len(sender_pub_bytes))
        
        header = shape_bytes + salt + sender_len + sender_pub_bytes + iv + tag
        
        # Calculate metrics
        metrics = self.calculate_metrics(image, scrambled)

        return {
            "encrypted_data": base64.b64encode(header + ciphertext).decode('utf-8'),
            "metrics": metrics
        }

    def decrypt_image_data(self, encrypted_bytes: bytes):
        # Parse data
        ptr = 0
        shape = tuple(np.frombuffer(encrypted_bytes[ptr:ptr+6], dtype=np.uint16))
        ptr += 6
        salt = encrypted_bytes[ptr:ptr+16]; ptr += 16
        sender_len = struct.unpack(">H", encrypted_bytes[ptr:ptr+2])[0]; ptr += 2
        sender_pub_bytes = encrypted_bytes[ptr:ptr+sender_len]; ptr += sender_len
        iv = encrypted_bytes[ptr:ptr+12]; ptr += 12
        tag = encrypted_bytes[ptr:ptr+16]; ptr += 16
        ciphertext = encrypted_bytes[ptr:]

        # Load sender public key
        sender_public = serialization.load_der_public_key(sender_pub_bytes, backend=default_backend())
        shared = self.receiver_private_key.exchange(ec.ECDH(), sender_public)
        aes_key = self.derive_aes_key(shared, salt)

        # Decrypt
        decryptor = Cipher(algorithms.AES(aes_key), modes.GCM(iv, tag), backend=default_backend()).decryptor()
        decrypted = decryptor.update(ciphertext) + decryptor.finalize()
        
        # Unscramble
        decrypted_array = np.frombuffer(decrypted, dtype=np.uint8).reshape(shape)
        seed = int.from_bytes(hashlib.sha256(aes_key + salt).digest()[:4], 'big')
        unscrambled = self.unscramble_image(decrypted_array, seed)

        # Encode back to PNG for returning
        _, buffer = cv2.imencode('.png', unscrambled)
        return base64.b64encode(buffer).decode('utf-8')

ecdh = ECDHEngine()
