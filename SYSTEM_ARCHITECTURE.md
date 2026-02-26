# Medical IoT System Architecture & Engineering Design

## 1. System Overview (ECE Perspective)

This project implements a complete **End-to-End IoT Healthcare System**, demonstrating core Electronics and Communication Engineering (ECE) principles including:
- **Sensor Interfacing & Signal Acquisition**: Integration of biomedical sensors (MAX30102, AD8232, DHT22).
- **Embedded Systems Programming**: Firmware development for ESP32/Arduino microcontroller.
- **Wireless Communication Protocols**: Real-time data transmission over WiFi (802.11) using REST/HTTP.
- **Secure Data Transmission**: Implementation of Attribute-Based Encryption (ABE) and Elliptic Curve Diffie-Hellman (ECDH) for secure channel establishment.
- **Digital Signal Processing (DSP)**: Basic filtering and anomaly detection on physiological signals.

---

## 2. Hardware Layer (The "Edge")

The Edge Layer consists of the patient monitoring node, designed around the **Arduino Uno R4 WiFi** (powered by the Renesas RA4M1 32-bit ARM Cortex-M4 and an ESP32-S3 module for connectivity).

### 2.1 Sensor Suite
| Sensor | Interface | Purpose | Signal Characteristics |
| :--- | :--- | :--- | :--- |
| **MAX30102** | I2C (SDA/SCL) | Pulse Oximetry (SpO2) & Heart Rate | PPG Signal (Photoplethysmogram) Analysis |
| **AD8232** | Analog (A0) | ECG (Electrocardiogram) | Bio-potential signal (0-3.3V), requiring amplification and filtering |
| **DHT22** | Digital (Pin 4) | Ambient Temp & Humidity | Environmental monitoring for patient comfort |
| **MPU6050** | I2C (SDA/SCL) | Accelerometer/Gyroscope | Fall Detection (Inertial Measurement) |

### 2.2 Microcontroller Firmware Logic (`firmware/medical_iot_node.ino`)
The firmware implements a **Real-Time Loop** on the RA4M1 core:
1.  **Polling**: Sensors are polled at 100Hz (10ms intervals) for high-fidelity signal capture.
2.  **Signal Conditioning**:
    *   **Moving Average Filter**: Applied to Heart Rate readings to smooth out noise artifacts.
    *   **Thresholding**: SpO2 values < 90% trigger local alert flags.
3.  **Transmission**:
    *   Data is buffered and serialized into **JSON** format.
    *   Transmitted via **HTTP POST** over the ESP32-S3 WiFi bridge to the Backend Gateway every 5 seconds.

---

## 3. Communication Layer

The system uses a **Hybrid Communication Architecture 802.11 (WiFi)**:

### 3.1 Protocol Stack
*   **Physical Layer**: WiFi (802.11 b/g/n) 2.4GHz
*   **Transport Layer**: TCP/IP
*   **Application Layer**: HTTP/1.1 (RESTful API) & WebSocket (Secure Socket Layer)

### 3.2 Data Packet Structure
```json
{
  "device_id": "UNO_R4_WIFI_01",
  "patient_id": "patient_alpha",
  "timestamp": 167889233,
  "payload": {
    "ecg_raw": [512, 530, 510, ...], // Array of 100 samples
    "spo2": 98,
    "bpm": 72,
    "temp_c": 36.8
  },
  "signature": "SHA256_HASH_OF_PAYLOAD" // Integrity Check
}
```

### 3.3 Security Engineering (The "Secure" in "Secure IoT")
Unlike standard web apps, this system implements **Cryptographic Engineering**:
*   **CP-ABE (Ciphertext-Policy Attribute-Based Encryption)**: Encrypts data at source/gateway such that only users with specific *attributes* (Role: Doctor, Dept: Cardiology) can decrypt it.
*   **ECDH (Elliptic Curve Diffie-Hellman)**: Used for secure key exchange when transferring high-bandwidth data like medical images (X-Rays).

---

## 4. Signal Processing & Analytics (The "Intelligence")

The Backend and Analytics Engine (`analytics-python`) perform the heavy lifting that the microcontroller cannot:

1.  **Arrhythmia Detection**: Analysis of ECG inter-beat intervals (RR-intervals) to detect irregularities.
2.  **Fall Detection Algorithm**:
    *   Calculates `Total Acceleration Vector (SVM) = sqrt(ax^2 + ay^2 + az^2)`.
    *   If `SVM > Threshold` (Impact) AND `Orientation Change > 60 deg`, a **Fall Alert** is triggered.
3.  **Predictive Alerts**: Time-series analysis to predict vital sign trends.

---

## 5. Circuit Diagram (Conceptual)

```
[ Patient ]
    |
    | (Bio-Signals)
    v
+-----------------------+
|   Analog Front End    |  <-- AD8232 (Instr. Amp + Filter)
+----------+------------+
           | (0-3.3V Analog)
           v
+-----------------------+       +----------------+
| Microcontroller (MCU) | <---- | MAX30102 (I2C) |
|  Arduino Uno R4 WiFi  |       +----------------+
+----------+------------+
           |
           | (WiFi via ESP32-S3 Bridge)
           v
    [ Access Point ]
           |
           v
      [ Gateway Server ]
```
