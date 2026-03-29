import cv2
import numpy as np
import base64
import io
import os
import requests
from PIL import Image

class BiometricEngine:
    """
    Modern AI-Powered Biometric Engine using OpenCV Deep Learning.
    - Simplified and robust fallback for model loading.
    """

    def __init__(self):
        self.detector = None
        self.recognizer = None
        self._ensure_onnx_models()

    def _ensure_onnx_models(self):
        """ Downloads the necessary ONNX weights with integrity checks. """
        
        # Correct Raw URLs for OpenCV Zoo Models
        models = {
            "face_detection_yunet_2023mar.onnx": "https://raw.githubusercontent.com/opencv/opencv_zoo/master/models/face_detection_yunet/face_detection_yunet_2023mar.onnx",
            "face_recognition_sface_2021dec.onnx": "https://raw.githubusercontent.com/opencv/opencv_zoo/master/models/face_recognition_sface/face_recognition_sface_2021dec.onnx"
        }
        
        for name, url in models.items():
            # If missing or truncated (< 100KB)
            if not os.path.exists(name) or os.path.getsize(name) < 100000:
                print(f"BIOMETRIC_LOG: Downloading {name} model from {url}...")
                try:
                    r = requests.get(url, allow_redirects=True, stream=True)
                    r.raise_for_status()
                    with open(name, 'wb') as f:
                        for chunk in r.iter_content(chunk_size=8192):
                            f.write(chunk)
                    print(f"BIOMETRIC_LOG: {name} downloaded successfully. Size: {os.path.getsize(name)} bytes.")
                except Exception as e:
                    print(f"BIOMETRIC_LOG: CRITICAL - Download failed for {name}: {e}")

        try:
             self.detector = cv2.FaceDetectorYN.create("face_detection_yunet_2023mar.onnx", "", (320, 320), 0.9, 0.3, 5000)
             self.recognizer = cv2.FaceRecognizerSF.create("face_recognition_sface_2021dec.onnx", "")
             print("BIOMETRIC_LOG: AI Models initialized.")
        except Exception as e:
             print(f"BIOMETRIC_LOG: ONNX Load Error. This occurs if models are corrupted: {e}")

    def extract_descriptor(self, image_base64: str):
        """
        Extracts 128-d ArcFace identity mappings.
        """
        try:
            if not self.detector: self._ensure_onnx_models()

            # 1. Decode and Normalize
            img_data = base64.b64decode(image_base64.split(",")[-1])
            img_pil = Image.open(io.BytesIO(img_data)).convert('RGB')
            img_bgr = cv2.cvtColor(np.array(img_pil), cv2.COLOR_RGB2BGR)
            
            # Detect faces
            h, w, _ = img_bgr.shape
            self.detector.setInputSize((w, h))
            ret, faces = self.detector.detect(img_bgr)
            
            if faces is None or len(faces) == 0:
                # If YuNet fails, we'll try one last time with a slightly lower confidence
                self.detector.setScoreThreshold(0.6)
                ret, faces = self.detector.detect(img_bgr)
                if faces is None or len(faces) == 0:
                    return {"error": "No face found in frame. Ensure your full face is visible and well-lit."}
            
            # Aligned face
            aligned_face = self.recognizer.alignCrop(img_bgr, faces[0])
            # Extract
            feature = self.recognizer.feature(aligned_face)
            return {"descriptor": feature[0].tolist(), "status": "success"}

        except Exception as e:
            print(f"BIOMETRIC_LOG: Identity extraction failed: {e}")
            return {"error": f"AI Engine Failure: {str(e)}"}

    def verify(self, stored_descriptor: list, captured_image_base64: str, tolerance: float = 0.363):
        """ SFace Cosine Similarity Match """
        result = self.extract_descriptor(captured_image_base64)
        if "error" in result: return result
        
        featStored = np.array([stored_descriptor], dtype=np.float32)
        featNew = np.array([result["descriptor"]], dtype=np.float32)
        score = self.recognizer.match(featStored, featNew, cv2.FR_COSINE)
        
        # SFace threshold (standard) is ~0.363
        return {
            "valid": bool(score > tolerance),
            "similarity": float(score),
            "status": "success"
        }

biometric_engine = BiometricEngine()
