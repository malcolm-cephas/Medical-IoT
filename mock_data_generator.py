import requests
import random
import time
import json
from datetime import datetime
from colorama import init, Fore, Style

import socket

# Initialize colorama
init()

# Configuration
# Custom hostname for local network access
# Change to your PC's IP or "localhost" if needed
HOSTNAME = "localhost" 
API_URL = f"http://{HOSTNAME}:8080/api/sensor/upload"
NUM_PATIENTS = 35
PATIENTS = [f"patient_{i:03d}" for i in range(1, NUM_PATIENTS + 1)] + \
           ["patient_alpha", "patient_beta", "patient_gamma", "alpha", "beta", "gamma"]

def generate_vitals(patient_id):
    """Generates realistic but random vitals."""
    
    # Base values
    base_hr = 75
    base_spo2 = 98
    base_temp = 36.8
    base_humidity = 45.0  # Room humidity percentage
    
    # Add randomness
    heart_rate = int(base_hr + random.gauss(0, 10))
    spo2 = int(min(100, max(85, base_spo2 + random.gauss(0, 2))))
    temperature = round(base_temp + random.gauss(0, 0.4), 1)
    humidity = round(base_humidity + random.gauss(0, 10), 1)
    systolic_bp = int(120 + random.gauss(0, 10))
    diastolic_bp = int(80 + random.gauss(0, 5))

    # Critical Condition Simulation (5% chance)
    if random.random() < 0.05:
        heart_rate += random.randint(30, 50) # Tachycardia
        spo2 -= random.randint(5, 10)       # Hypoxia
    
    return {
        "patientId": patient_id,
        "heartRate": heart_rate,
        "spo2": spo2,
        "temperature": temperature,
        "humidity": humidity,
        "systolicBP": systolic_bp,
        "diastolicBP": diastolic_bp
    }

def send_data(data):
    try:
        # Using Basic Auth (admin/password) to correspond with backend security
        response = requests.post(API_URL, json=data, timeout=2, auth=('admin', 'password'))
        if response.status_code == 200:
            status_color = Fore.GREEN
            status_msg = "SUCCESS"
        else:
            status_color = Fore.RED
            status_msg = f"FAIL ({response.status_code}) - {response.text}"
    except requests.exceptions.RequestException as e:
        status_color = Fore.RED
        status_msg = "CONN ERR"
    
    # Print clean log with temperature and humidity
    time_str = datetime.now().strftime("%H:%M:%S")
    print(f"{Style.DIM}[{time_str}]{Style.RESET_ALL} "
          f"{Fore.CYAN}{data['patientId']}{Style.RESET_ALL} | "
          f"HR: {data['heartRate']:3d} | SpO2: {data['spo2']:3d}% | "
          f"Temp: {data['temperature']:4.1f}°C | Hum: {data['humidity']:4.1f}% | "
          f"{status_color}{status_msg}{Style.RESET_ALL}")

def main():
    print(f"{Fore.YELLOW}=== Medical IoT Mock Data Generator ==={Style.RESET_ALL}")
    print(f"Simulating devices for {len(PATIENTS)} patients...")
    print(f"Target: {API_URL}")
    print("Press Ctrl+C to stop.\n")
    
    try:
        while True:
            # Pick a random patient to update
            patient = random.choice(PATIENTS)
            vitals = generate_vitals(patient)
            send_data(vitals)
            
            # Rate limiting: 2-5 updates per second
            time.sleep(random.uniform(0.2, 0.5))
            
    except KeyboardInterrupt:
        print(f"\n{Fore.YELLOW}Generator stopped.{Style.RESET_ALL}")

if __name__ == "__main__":
    main()
