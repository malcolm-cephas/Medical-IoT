import os
import cv2
import numpy as np
import face_recognition
from flask import Flask, request, jsonify
from flask_cors import CORS

app = Flask(__name__)
CORS(app)

@app.route('/extract_embedding', methods=['POST'])
def extract_embedding():
    """Extracts a 128-d face embedding from an uploaded image."""
    if 'file' not in request.files:
        return jsonify({"error": "No file provided"}), 400
    
    file = request.files['file']
    img_bytes = file.read()
    nparr = np.frombuffer(img_bytes, np.uint8)
    image = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
    rgb_image = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)

    face_encodings = face_recognition.face_encodings(rgb_image)
    
    if not face_encodings:
        return jsonify({"error": "No face detected"}), 400
    
    # Return the first face embedding as a list
    embedding = face_encodings[0].tolist()
    return jsonify({"embedding": embedding})

@app.route('/verify_face', methods=['POST'])
def verify_face():
    """Verifies an uploaded image against a target embedding."""
    if 'file' not in request.files or 'target_embedding' not in request.form:
        return jsonify({"error": "Missing file or target embedding"}), 400
    
    try:
        target_embedding = np.array(eval(request.form['target_embedding']))
        
        file = request.files['file']
        img_bytes = file.read()
        nparr = np.frombuffer(img_bytes, np.uint8)
        image = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
        rgb_image = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)

        face_locations = face_recognition.face_locations(rgb_image)
        face_encodings = face_recognition.face_encodings(rgb_image, face_locations)

        if not face_encodings:
            return jsonify({"status": "no_face", "match": False}), 200

        # Check if any detected face matches the target embedding
        matches = face_recognition.compare_faces([target_embedding], face_encodings[0], tolerance=0.6)
        distance = face_recognition.face_distance([target_embedding], face_encodings[0])

        return jsonify({
            "status": "success",
            "match": bool(matches[0]),
            "distance": float(distance[0])
        })
    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5050, debug=True)
