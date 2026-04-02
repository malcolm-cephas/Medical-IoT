from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import List, Optional
import numpy as np
import uvicorn
import base64
import os
import io
import cv2
import requests
from PIL import Image
from models.heart_failure import HeartFailurePredictor

from ecdh_engine import ecdh
from abe_engine import abe

hf_predictor = HeartFailurePredictor("data/heart_failure_dataset.csv")

app = FastAPI()
app.add_middleware(CORSMiddleware, allow_origins=["*"], allow_credentials=True, allow_methods=["*"], allow_headers=["*"])

class HeartFailureReq(BaseModel):
    age: float
    anaemia: int
    creatinine_phosphokinase: float
    diabetes: int
    ejection_fraction: float
    high_blood_pressure: int
    platelets: float
    serum_creatinine: float
    serum_sodium: float
    sex: int
    smoking: int
    time: float

@app.get("/public-key")
def get_pub_key(): return {"public_key": abe.get_public_key()}

@app.post("/encrypt-image")
def encrypt_img(request: dict):
    try:
        image_data = base64.b64decode(request["image"])
        return ecdh.encrypt_image_data(image_data)
    except Exception as e: raise HTTPException(status_code=500, detail=str(e))

@app.post("/decrypt-image")
def decrypt_img(request: dict):
    try:
        enc_data = base64.b64decode(request["encrypted_data"])
        decrypted = ecdh.decrypt_image_data(enc_data)
        return {"decrypted_image": decrypted}
    except Exception as e: raise HTTPException(status_code=500, detail=str(e))

@app.post("/prediction/heart-failure")
def predict_heart_failure(req: HeartFailureReq):
    res = hf_predictor.predict(req.dict())
    if "error" in res: raise HTTPException(status_code=400, detail=res["error"])
    return res

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=4242)
