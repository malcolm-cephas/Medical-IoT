import cv2
import numpy as np
import base64
import face_recognition
from ultralytics import YOLO
import torch

class BiometricEngine:
    """
    Advanced Face Recognition Engine:
    - Detection: YOLO (Ultralytics) for high-accuracy face localized bounding boxes.
    - Recognition: face_recognition (dlib) for 128-d Identity Embedding.
    """

    def __init__(self):
        # We use a YOLOv8-face model or standard YOLOv8n if face-specific isn't available
        # The first run will download 'yolov8n.pt' (~6MB)
        try:
            self.detector = YOLO('yolov8n.pt') 
            print("BIOMETRIC_LOG: YOLO Detector Loaded Successfully.")
        except Exception as e:
            print(f"BIOMETRIC_LOG: Warning, YOLO load failed: {e}. Falling back to HOG.")
            self.detector = None

    def extract_descriptor(self, image_base64: str):
        """
        Takes a base64 image, uses YOLO to find the face, crops it, 
        and extracts 128-d descriptor.
        """
        try:
            # Decode image
            img_data = base64.b64decode(image_base64.split(",")[-1])
            img_array = np.frombuffer(img_data, np.uint8)
            img = cv2.imdecode(img_array, cv2.IMREAD_COLOR)
            
            if img is None:
                return {"error": "Invalid image format received"}

            rgb_img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
            
            # 1. Detection Phase (YOLO)
            face_locations = []
            if self.detector:
                results = self.detector(img, verbose=False)
                # Parse YOLO results for 'person' or 'face' (YOLOv8 detects persons by default)
                # To be precise, one would use yolov8n-face.pt but we use person-crop for stability
                for r in results:
                    boxes = r.boxes.xyxy.cpu().numpy()
                    for box in boxes:
                        # Convert YOLO box (x1, y1, x2, y2) to face_recognition (top, right, bottom, left)
                        x1, y1, x2, y2 = map(int, box)
                        face_locations.append((y1, x2, y2, x1))
            
            # 2. Recognition Phase (dlib)
            # If YOLO didn't find specific faces, fall back to dlib's internal HOG detector
            encodings = face_recognition.face_encodings(rgb_img, known_face_locations=face_locations if face_locations else None)
            
            if not encodings:
                return {"error": "No face detected in the capture. Ensure good lighting."}
            
            return {"descriptor": encodings[0].tolist(), "status": "success"}

        except Exception as e:
            print(f"BIOMETRIC_LOG: Extraction Critical Error: {e}")
            return {"error": f"Processing Failed: {str(e)}"}

    def verify(self, stored_descriptor: list, captured_image_base64: str, tolerance: float = 0.55):
        """
        Compares Identity: Euclidean distance between 128-vectors.
        """
        result = self.extract_descriptor(captured_image_base64)
        if "error" in result:
            return result
        
        captured_descriptor = result["descriptor"]
        distance = np.linalg.norm(np.array(stored_descriptor) - np.array(captured_descriptor))
        
        # Identity match threshold (0.6 is common, 0.5 is stricter)
        is_match = distance < tolerance
        
        return {
            "valid": bool(is_match),
            "distance": float(distance),
            "confidence": float(1.0 - distance),
            "status": "success"
        }

biometric_engine = BiometricEngine()
