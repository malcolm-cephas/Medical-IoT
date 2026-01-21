import json
import base64
import os
from Crypto.PublicKey import RSA
from Crypto.Cipher import AES, PKCS1_OAEP
from Crypto.Random import get_random_bytes
from Crypto.Hash import SHA256
from Crypto.Signature import pkcs1_15

class ABEEngine:
    def __init__(self):
        self.master_key_pair = None
        self.public_key = None
        self.key_file = "abe_master.pem"
        self._load_or_generate_keys()

    def _load_or_generate_keys(self):
        if os.path.exists(self.key_file):
            with open(self.key_file, 'rb') as f:
                self.master_key_pair = RSA.import_key(f.read())
                self.public_key = self.master_key_pair.publickey()
        else:
            self.setup()

    def setup(self):
        self.master_key_pair = RSA.generate(2048)
        self.public_key = self.master_key_pair.publickey()
        with open(self.key_file, 'wb') as f:
            f.write(self.master_key_pair.export_key())
        return "Setup Complete. Master Key Generated."

    def encrypt(self, message, policy_attributes):
        # 1. Generate AES Key and Nonce
        aes_key = get_random_bytes(16)
        cipher_aes = AES.new(aes_key, AES.MODE_EAX)
        ciphertext, tag = cipher_aes.encrypt_and_digest(message.encode('utf-8'))
        
        # 2. Encrypt AES Key with RSA Public Key (Simulating ABE Master Key)
        cipher_rsa = PKCS1_OAEP.new(self.public_key)
        enc_aes_key = cipher_rsa.encrypt(aes_key)
        
        # 3. Package
        package = {
            "policy": sorted(policy_attributes),
            "ciphertext": base64.b64encode(ciphertext).decode('utf-8'),
            "nonce": base64.b64encode(cipher_aes.nonce).decode('utf-8'),
            "tag": base64.b64encode(tag).decode('utf-8'),
            "encrypted_key": base64.b64encode(enc_aes_key).decode('utf-8')
        }
        return package

# Singleton
abe = ABEEngine()
