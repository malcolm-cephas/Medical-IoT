from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import List, Optional
import pandas as pd
import numpy as np
import random
import uvicorn
import base64
from abe_engine import abe
from ecdh_engine import ecdh

app = FastAPI()

# Enable CORS for Frontend Access
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Allows all origins
    allow_credentials=True,
    allow_methods=["*"],  # Allows all methods
    allow_headers=["*"],  # Allows all headers
)

# --- Models ---

class HealthData(BaseModel):
    patientId: str
    heartRate: int
    spo2: int
    temperature: float
    ecg_readings: List[float] = [] 
    accelerometer_z: float = 1.0  
    # Personalized Thresholds (Optional)
    max_heart_rate: Optional[int] = 100
    min_spo2: Optional[int] = 95

class EncryptRequest(BaseModel):
    data: str
    policy: str

class EncryptImageRequest(BaseModel):
    image_base64: str

class DecryptImageRequest(BaseModel):
    encrypted_base64: str

# --- Endpoints ---

@app.get("/")
def read_root():
    return {"message": "Medical IoT Analytics Service"}

@app.get("/public-key")
def get_public_key():
    return {"public_key": abe.get_public_key()}

@app.post("/abe/encrypt")
def encrypt_data_abe(req: EncryptRequest):
    try:
        # Calls the updated ABE engine which supports policy strings (e.g. "Doctor AND Cardiology")
        ciphertext_package = abe.encrypt(req.data, req.policy)
        return {"ciphertext": json.dumps(ciphertext_package), "status": "success"}
    except Exception as e:
        print(f"ABE Encryption Error: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/encrypt-image")
def encrypt_image_endpoint(req: EncryptImageRequest):
    try:
        image_bytes = base64.b64decode(req.image_base64)
        result = ecdh.encrypt_image_data(image_bytes)
        return result
    except Exception as e:
        print(f"Image Encryption Error: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/decrypt-image")
def decrypt_image_endpoint(req: DecryptImageRequest):
    try:
        encrypted_bytes = base64.b64decode(req.encrypted_base64)
        decrypted_base64 = ecdh.decrypt_image_data(encrypted_bytes)
        return {"decrypted_image": decrypted_base64}
    except Exception as e:
        print(f"Image Decryption Error: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/analyze")
def analyze_health(data: HealthData):
    analysis = check_vitals(data)
    return analysis

def check_vitals(data: HealthData):
    risk_score = 0
    anomalies = []

    # Use personalized thresholds if provided, else defaults
    max_hr = data.max_heart_rate if data.max_heart_rate else 100
    min_spo2 = data.min_spo2 if data.min_spo2 else 95

    # Critical Checks
    if data.heartRate > max_hr or data.heartRate < 50:
        risk_score += 30
        anomalies.append(f"Abnormal Heart Rate ({data.heartRate} bpm)")
    
    if data.spo2 < min_spo2:
        risk_score += 50
        anomalies.append(f"Critical SpO2 Level ({data.spo2}%)")
    
    if data.temperature > 37.5:
        risk_score += 20
        anomalies.append(f"High Temperature ({data.temperature}°C)")

    # Fall Detection
    if data.accelerometer_z < 0.5: # Freefall detected
        risk_score += 100
        anomalies.append("FALL DETECTED")

    # ECG Arrhythmia Detection
    if data.ecg_readings:
        variance = np.var(data.ecg_readings) if len(data.ecg_readings) > 0 else 0
        if variance > 500:
            risk_score += 40
            anomalies.append("Irregular ECG Variance Detected")

    risk_level = "LOW"
    if risk_score >= 80:
        risk_level = "CRITICAL"
    elif risk_score >= 50:
        risk_level = "HIGH"
    elif risk_score >= 20:
        risk_level = "MEDIUM"

    return {
        "patientId": data.patientId,
        "risk_score": risk_score,
        "risk_level": risk_level,
        "anomalies": anomalies,
        "is_critical": risk_level == "CRITICAL" or risk_level == "HIGH"
    }

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=4242)
