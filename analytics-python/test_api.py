import requests
import base64
import numpy as np
import cv2
import io
from PIL import Image

def test_local_extract():
    # Create a dummy RGB 8-bit image (100x100 white square)
    img = np.ones((100, 100, 3), dtype=np.uint8) * 255
    pil_img = Image.fromarray(img)
    buff = io.BytesIO()
    pil_img.save(buff, format="JPEG")
    img_b64 = base64.b64encode(buff.getvalue()).decode()
    
    url = "http://localhost:4242/biometric/extract"
    payload = {"image_base64": img_b64}
    
    print(f"DEBUG: Sending test request to {url}...")
    try:
        response = requests.post(url, json=payload)
        print(f"DEBUG: Response Status: {response.status_code}")
        print(f"DEBUG: Response Body: {response.text}")
    except Exception as e:
        print(f"DEBUG: Request Failed: {e}")

if __name__ == "__main__":
    test_local_extract()
