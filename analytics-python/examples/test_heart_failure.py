import requests
import json

# API details
BASE_URL = "http://localhost:4444"

def test_heart_failure_prediction():
    # Example patient data from IoT sensors
    patient_data = {
        "age": 65.0,
        "anaemia": 1,
        "creatinine_phosphokinase": 160.0,
        "diabetes": 1,
        "ejection_fraction": 20.0,
        "high_blood_pressure": 0,
        "platelets": 327000.0,
        "serum_creatinine": 2.7,
        "serum_sodium": 116.0,
        "sex": 0,
        "smoking": 0,
        "time": 8.0
    }
    
    print(f"--- Sending Patient Data for Heart Failure Risk Assessment ---")
    print(json.dumps(patient_data, indent=2))
    
    try:
        response = requests.post(f"{BASE_URL}/prediction/heart-failure", json=patient_data)
        if response.status_code == 200:
            result = response.json()
            print(f"\n--- Prediction Results ---")
            print(f"Mortality Predicted: {'YES' if result['death_event_predicted'] else 'NO'}")
            print(f"Risk Level: {result['risk_level']}")
            print(f"Mortality Probability: {result['mortality_probability'] * 100}%")
        else:
            print(f"Error: {response.status_code} - {response.text}")
    except Exception as e:
        print(f"Connection failed: {e}")

if __name__ == "__main__":
    print("Testing analytic services (ensure main.py is running on port 4444)...")
    test_heart_failure_prediction()
