import os
import cv2
import numpy as np
import face_recognition
import json
import logging
from flask import Flask, request, jsonify
from flask_cors import CORS

# Configure logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

app = Flask(__name__)
CORS(app)

# Load Haar Cascade as a fast/robust fallback for detection
haar_cascade_path = cv2.data.haarcascades + 'haarcascade_frontalface_default.xml'
face_cascade = cv2.CascadeClassifier(haar_cascade_path)

def detect_face_robust(rgb_image):
    """
    Tries to detect faces using multiple methods:
    1. face_recognition (HOG-based)
    2. OpenCV Haar Cascade (fallback)
    Returns list of (top, right, bottom, left) tuples.
    """
    # Method 1: HOG (Standard in face_recognition)
    locations = face_recognition.face_locations(rgb_image, model="hog")
    if locations:
        logger.info(f"Detected {len(locations)} face(s) using HOG.")
        return locations

    # Method 2: Haar Cascade (Traditional but robust in some lighting)
    gray = cv2.cvtColor(rgb_image, cv2.COLOR_RGB2GRAY)
    faces = face_cascade.detectMultiScale(gray, scaleFactor=1.1, minNeighbors=5, minSize=(30, 30))
    
    if len(faces) > 0:
        logger.info(f"Detected {len(faces)} face(s) using Haar Cascade (HOG failed).")
        # Convert (x, y, w, h) to (top, right, bottom, left)
        converted_locations = []
        for (x, y, w, h) in faces:
            converted_locations.append((y, x + w, y + h, x))
        return converted_locations

    logger.warning("No face detected by any method.")
    return []

@app.route('/extract_embedding', methods=['POST'])
def extract_embedding():
    """Extracts a 128-d face embedding from an uploaded image."""
    if 'file' not in request.files:
        return jsonify({"error": "No file provided"}), 400
    
    try:
        file = request.files['file']
        img_bytes = file.read()
        nparr = np.frombuffer(img_bytes, np.uint8)
        image = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
        
        if image is None:
            logger.error("Failed to decode image.")
            return jsonify({"error": "Invalid image data"}), 400
            
        rgb_image = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)

        # Use robust detection
        face_locations = detect_face_robust(rgb_image)
        
        if not face_locations:
            return jsonify({"error": "No face detected"}), 400
        
        # Get encodings for detected locations
        face_encodings = face_recognition.face_encodings(rgb_image, face_locations)
        
        if not face_encodings:
            return jsonify({"error": "Could not extract face encoding"}), 400
        
        # Return the first face embedding as a list
        embedding = face_encodings[0].tolist()
        logger.info("Successfully extracted embedding.")
        return jsonify({"embedding": embedding})
        
    except Exception as e:
        logger.exception("Error in extract_embedding")
        return jsonify({"error": str(e)}), 500

@app.route('/verify_face', methods=['POST'])
def verify_face():
    """Verifies an uploaded image against a target embedding."""
    if 'file' not in request.files or 'target_embedding' not in request.form:
        return jsonify({"error": "Missing file or target embedding"}), 400
    
    try:
        raw_target = request.form['target_embedding']
        logger.info(f"Verifying face. Target embedding length: {len(raw_target)}")
        
        # Use json.loads instead of eval for safety and robustness
        try:
            target_embedding = np.array(json.loads(raw_target))
        except:
            # Fallback for Java's [0.1, 0.2] format if JSON fails (eval is last resort)
            target_embedding = np.array(eval(raw_target))
        
        file = request.files['file']
        img_bytes = file.read()
        nparr = np.frombuffer(img_bytes, np.uint8)
        image = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
        
        if image is None:
            return jsonify({"error": "Invalid image data"}), 400
            
        rgb_image = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)

        # Use robust detection
        face_locations = detect_face_robust(rgb_image)
        face_encodings = face_recognition.face_encodings(rgb_image, face_locations)

        if not face_encodings:
            logger.warning("Verify request: No face encodings found.")
            return jsonify({"status": "no_face", "match": False}), 200

        # Check if any detected face matches the target embedding
        # We compare the first one or we could compare all
        matches = face_recognition.compare_faces([target_embedding], face_encodings[0], tolerance=0.6)
        distance = face_recognition.face_distance([target_embedding], face_encodings[0])

        logger.info(f"Verification result: {matches[0]} (Distance: {distance[0]})")

        return jsonify({
            "status": "success",
            "match": bool(matches[0]),
            "distance": float(distance[0])
        })
    except Exception as e:
        logger.exception("Error in verify_face")
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    logger.info("Starting AI Face Service on port 5050...")
    app.run(host='0.0.0.0', port=5050, debug=True)

