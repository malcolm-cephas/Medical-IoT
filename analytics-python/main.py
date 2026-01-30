from fastapi import FastAPI, HTTPException
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
    message: str
    policy_attributes: List[str]

class EncrectImageRequest(BaseModel):
    image_base64: str

class DecryptImageRequest(BaseModel):
    encrypted_base64: str

# --- Endpoints ---

@app.get("/")
def read_root():
    return {"message": "Medical IoT Analytics Service"}

@app.post("/encrypt")
def encrypt_data(req: EncryptRequest):
    try:
        print(f"Encrypting data with policy: {req.policy_attributes}")
        ciphertext = abe.encrypt(req.message, req.policy_attributes)
        return ciphertext
    except Exception as e:
        print(f"Encryption Error: {e}")
        return {"error": str(e)}

@app.post("/encrypt-image")
def encrypt_image_endpoint(req: EncrectImageRequest):
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
    # Dummy logic for risk calculation
    risk_score = 0
    anomalies = []

    # Use personalized thresholds if provided, else defaults
    max_hr = data.max_heart_rate if data.max_heart_rate else 100
    min_spo2 = data.min_spo2 if data.min_spo2 else 95

    if data.heartRate > max_hr or data.heartRate < 60:
        risk_score += 30
        anomalies.append(f"Abnormal Heart Rate (>{max_hr} or <60)")
    
    if data.spo2 < min_spo2:
        risk_score += 50
        anomalies.append(f"Low Oxygen Saturation (<{min_spo2})")
    
    if data.temperature > 37.5:
        risk_score += 20
        anomalies.append("Fever")
        
    # --- Advanced Analytics ---
    # 1. Fall Detection
    if data.accelerometer_z < 0.5: # Freefall detected
        risk_score += 100
        anomalies.append("FALL DETECTED")
    
    # 2. ECG Arrhythmia Detection (Simplified)
    if data.ecg_readings:
        variance = np.var(data.ecg_readings) if len(data.ecg_readings) > 0 else 0
        if variance > 500: # Arbitrary threshold for "jumps"
            risk_score += 40
            anomalies.append("Possible Arrhythmia (ECG Variance)")

    risk_level = "LOW"
    if risk_score > 50:
        risk_level = "HIGH"
    elif risk_score > 20:
        risk_level = "MEDIUM"

    return {
        "patientId": data.patientId,
        "risk_score": risk_score,
        "risk_level": risk_level,
        "anomalies": anomalies
    }

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
