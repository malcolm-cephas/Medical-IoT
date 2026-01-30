import requests
import time
import random
import sys

# Configuration
API_URL = "http://localhost:8080/api/sensor/upload"

PATIENT_PROFILES = {
    "patient_alpha": {
        "name": "Athlete (Healthy)",
        "hr_range": (50, 75),
        "spo2_range": (97, 100),
        "bp_sys_range": (110, 120),
        "bp_dia_range": (70, 80)
    },
    "patient_beta": {
        "name": "Hypertensive (High BP)",
        "hr_range": (70, 90),
        "spo2_range": (95, 99),
        "bp_sys_range": (140, 160), # High BP
        "bp_dia_range": (90, 100)
    },
    "patient_gamma": {
        "name": "Respiratory Issue (Lower SpO2)",
        "hr_range": (85, 110), # Tachycardic
        "spo2_range": (90, 96), # Lower Oxygen
        "bp_sys_range": (115, 135),
        "bp_dia_range": (75, 85)
    }
}

def generate_vitals(patient_id):
    profile = PATIENT_PROFILES.get(patient_id, {
        "hr_range": (60, 100),
        "spo2_range": (95, 100),
        "bp_sys_range": (110, 130),
        "bp_dia_range": (70, 85)
    })
    
    return {
        "heartRate": random.randint(*profile["hr_range"]),
        "spo2": random.randint(*profile["spo2_range"]),
        "temperature": round(random.uniform(36.5, 37.5), 1),
        "systolicBP": random.randint(*profile["bp_sys_range"]),
        "diastolicBP": random.randint(*profile["bp_dia_range"])
    }

def simulate():
    print(f"Starting Mock Sensor Simulation")
    print(f"Target URL: {API_URL}")
    print("="*40)
    for pid, profile in PATIENT_PROFILES.items():
        print(f" - {pid}: {profile.get('name', 'Standard')}")
    print("="*40)
    print("Press Ctrl+C to stop.")
    
    while True:
        for patient in PATIENT_PROFILES.keys():
            try:
                data = generate_vitals(patient)
                data["patientId"] = patient
                
                # Occasional spike logic (1 in 50 chance) remains for randomness
                if random.randint(1, 50) == 1:
                    data["heartRate"] += 20
                    print(f"[!] MOMENTARY SPIKE for {patient}")

                response = requests.post(API_URL, json=data)
                if response.status_code == 200:
                    print(f"[{patient}] Uploaded: HR={data['heartRate']} SpO2={data['spo2']}%")
                else:
                    print(f"[{patient}] Failed: {response.status_code} - {response.text}")
            
            except Exception as e:
                print(f"Error connecting to backend: {e}")
                time.sleep(5) 

        time.sleep(2) 

if __name__ == "__main__":
    try:
        try:
            import requests
        except ImportError:
            print("Installing requests library...")
            import subprocess
            subprocess.check_call([sys.executable, "-m", "pip", "install", "requests"])
            import requests

        simulate()
    except KeyboardInterrupt:
        print("\nSimulation stopped.")
