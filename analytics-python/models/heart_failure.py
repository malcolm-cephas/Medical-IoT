import numpy as np
import pandas as pd
from sklearn.linear_model import LogisticRegression
from sklearn.preprocessing import StandardScaler
from typing import Dict, Any

class HeartFailurePredictor:
    """
    Heart Failure Prediction Model based on clinical records.
    Features: 
        - age: Patient's age
        - anaemia: Decrease of red blood cells or hemoglobin (0/1)
        - creatinine_phosphokinase: Level of the CPK enzyme in the blood (mcg/L)
        - diabetes: If the patient has diabetes (0/1)
        - ejection_fraction: Percentage of blood leaving the heart at each contraction (%)
        - high_blood_pressure: If the patient has hypertension (0/1)
        - platelets: Platelets in the blood (kiloplatelets/mL)
        - serum_creatinine: Level of serum creatinine in the blood (mg/dL)
        - serum_sodium: Level of serum sodium in the blood (mEq/L)
        - sex: Woman or man (0/1)
        - smoking: If the patient smokes (0/1)
        - time: Follow-up period (days)
    """
    def __init__(self, data_path: str = None):
        self.model = LogisticRegression(max_iter=1000)
        self.scaler = StandardScaler()
        self.is_trained = False
        self.feature_columns = [
            'age', 'anaemia', 'creatinine_phosphokinase', 'diabetes',
            'ejection_fraction', 'high_blood_pressure', 'platelets',
            'serum_creatinine', 'serum_sodium', 'sex', 'smoking', 'time'
        ]
        
        if data_path:
            self.train_from_csv(data_path)

    def train_from_csv(self, csv_path: str):
        try:
            # Handle .xls extension in the filename as per the project
            df = pd.read_csv(csv_path)
            X = df[self.feature_columns]
            y = df['DEATH_EVENT']
            
            X_scaled = self.scaler.fit_transform(X)
            self.model.fit(X_scaled, y)
            self.is_trained = True
            print(f"AI_LOG: Heart Failure Prediction model trained successfully from {csv_path}.")
        except Exception as e:
            print(f"AI_LOG: Failed to train Heart Failure model: {e}")

    def predict(self, patient_data: Dict[str, Any]) -> Dict[str, Any]:
        if not self.is_trained:
            return {"error": "Model not trained. Please provide historical data for calibration."}
        
        try:
            # Prepare feature vector
            data_list = [patient_data.get(f, 0) for f in self.feature_columns]
            X_input = np.array([data_list])
            X_scaled = self.scaler.transform(X_input)
            
            # Predict and calculate probability
            prediction = int(self.model.predict(X_scaled)[0])
            probability = float(self.model.predict_proba(X_scaled)[0][1])
            
            risk_level = "High" if probability > 0.6 else "Moderate" if probability > 0.3 else "Low"
            
            return {
                "death_event_predicted": prediction,
                "mortality_probability": round(probability, 4),
                "risk_level": risk_level,
                "status": "success"
            }
        except Exception as e:
            return {"error": f"Prediction failed: {str(e)}"}

# Instantiate a default predictor (to be calibrated with real dataset path)
predictor = HeartFailurePredictor()
