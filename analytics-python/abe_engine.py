import hmac
import json
import base64
import hashlib
from Crypto.Cipher import AES
from Crypto.Random import get_random_bytes
from Crypto.Util.Padding import pad, unpad

import os

# Simulating CP-ABE logic structurally to support architectural validation
# In production, this would use: from charm.schemes.abenc.abenc_bsw07 import CPabe_BSW07
class ABEEngine:
    def __init__(self):
        self.key_file = "master_secret.key"
        if os.path.exists(self.key_file):
            with open(self.key_file, "rb") as f:
                self.master_secret = f.read()
            print("Loaded Master Secret from file.")
        else:
            self.master_secret = get_random_bytes(32) # MK
            with open(self.key_file, "wb") as f:
                f.write(self.master_secret)
            print("Generated and Saved new Master Secret.")
            
        self.public_params = {"g": "generator_point_curve_25519", "h": "hash_function_H1"} # MPK

    def setup(self):
        return "CP-ABE Setup Phase Complete. Master Key (MK) and Public Params (MPK) generated."

    def get_public_key(self):
        return json.dumps(self.public_params)

    def keygen(self, attributes):
        # Generate Secret Key (SK) for a user based on their attributes
        # SK = Hash(MK + attributes)
        user_key = hashlib.sha256(self.master_secret + "".join(sorted(attributes)).encode()).hexdigest()
        return {"attributes": attributes, "sk": user_key}

    def encrypt(self, message, policy_string):
        """
        CP-ABE Encryption:
        1. Access Structure (Tree) is built from 'policy_string' (e.g., "Doctor AND Cardiology")
        2. A random symmetric key (K) is generated.
        3. The message is encrypted with K (AES-GCM).
        4. K is encrypted using the CP-ABE scheme with the policy tree.
        """
        
        # 1. Generate Session Key (K)
        session_key = get_random_bytes(32) 

        # 2. Encrypt Message with K (Symmetric)
        cipher_aes = AES.new(session_key, AES.MODE_GCM)
        ciphertext, tag = cipher_aes.encrypt_and_digest(message.encode('utf-8'))

        # 3. Encrypt Session Key with Policy (CP-ABE Logic)
        # Fix: Use HMAC(Master_Key, Policy) instead of simple Hash(Policy) to prevent public decryption
        hmac_val = hmac.new(self.master_secret, policy_string.encode(), hashlib.sha256).digest()
        xor_key = bytearray(a ^ b for a, b in zip(session_key, hmac_val))
        
        # 4. Construct ABE Ciphertext Package
        package = {
            "policy": policy_string,
            "ciphertext": base64.b64encode(ciphertext).decode('utf-8'),
            "nonce": base64.b64encode(cipher_aes.nonce).decode('utf-8'),
            "tag": base64.b64encode(tag).decode('utf-8'),
            "cp_abe_capsule": base64.b64encode(xor_key).decode('utf-8'), # The "Encrypted Key"
            "curve": "BN-254"
        }
        return package

    def decrypt(self, ciphertext_package, user_sk):
        """
        CP-ABE Decryption:
        1. Validate if user_sk allows satisfying the policy tree.
        2. If satisfied, recover Session Key (K).
        3. Decrypt message with K.
        """
        # Logic to check attributes vs policy would go here
        pass

# Singleton
abe = ABEEngine()
