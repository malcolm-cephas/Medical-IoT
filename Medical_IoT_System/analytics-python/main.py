from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import pandas as pd
import numpy as np
import random

app = FastAPI()

class HealthData(BaseModel):
    patientId: str
    heartRate: int
    spo2: int
    temperature: float

@app.get("/")
def read_root():
    return {"message": "Medical IoT Analytics Service"}

from abe_engine import abe
from pydantic import BaseModel
from typing import List

class EncryptRequest(BaseModel):
    message: str
    policy_attributes: List[str]

@app.post("/encrypt")
def encrypt_data(req: EncryptRequest):
    try:
        print(f"Encrypting data with policy: {req.policy_attributes}")
        ciphertext = abe.encrypt(req.message, req.policy_attributes)
        return ciphertext
    except Exception as e:
        print(f"Encryption Error: {e}")
        return {"error": str(e)}

@app.post("/analyze")
def analyze_health(data: HealthData):
    # Dummy logic for risk calculation
    risk_score = 0
    anomalies = []

    if data.heartRate > 100 or data.heartRate < 60:
        risk_score += 30
        anomalies.append("Abnormal Heart Rate")
    
    if data.spo2 < 95:
        risk_score += 50
        anomalies.append("Low Oxygen Saturation")
    
    if data.temperature > 37.5:
        risk_score += 20
        anomalies.append("Fever")
        
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
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
