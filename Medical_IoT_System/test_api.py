import requests
import json
import time

url = "http://localhost:8080/api/sensor/upload"
data = {
    "patientId": "123",
    "heartRate": 105,
    "spo2": 94,
    "temperature": 38.0
}
headers = {"Content-Type": "application/json"}

print(f"Sending request to {url}...")
try:
    response = requests.post(url, json=data, headers=headers)
    print("Status Code:", response.status_code)
    print("Response:", response.text)
except Exception as e:
    print("Error:", e)

print("\nFetching History for Patient 123...")
try:
    response = requests.get("http://localhost:8080/api/sensor/history/123")
    print("Status Code:", response.status_code)
    print("History:", response.json())
except Exception as e:
    print("Error Fetching History:", e)
