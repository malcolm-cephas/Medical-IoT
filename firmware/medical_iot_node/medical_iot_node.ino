/**
 * Medical IoT Sensor Node Firmware
 * Platform: Arduino Uno R4 WiFi (Renesas RA4M1 + ESP32-S3 Module)
 * 
 * Sensors:
 * - MAX30102: Pulse Oximetry & Heart Rate (I2C: SDA/SCL via Qwiic or Breadboard)
 * - DHT22: Temperature & Humidity (Digital Pin 4)
 * - AD8232: ECG Lead Monitor (Analog A0, LO+ D10, LO- D11)
 * 
 * Communication:
 * - WiFi (2.4GHz) via internal ESP32-S3 bridge
 * - REST API (JSON over HTTP POST)
 * 
 * Author: Malcolm Cephas
 * Major Project: Secure Medical IoT System
 */

#include <WiFiS3.h> // Specific library for Arduino Uno R4 WiFi
#include <ArduinoHttpClient.h> // Improved handling for R4
#include <ArduinoJson.h>
#include "MAX30105.h"
#include "heartRate.h"
#include "DHT.h"

// --- Configuration ---
const char* ssid = "YOUR_WIFI_SSID";
const char* password = "YOUR_WIFI_PASSWORD";
// Note: Use your computer's local IP address (e.g., 192.168.1.X), NOT localhost
const char* serverAddress = "192.168.1.100"; 
int serverPort = 8080;

// --- Pin Definitions ---
#define DHTPIN 4     // Digital Pin for DHT22
#define DHTTYPE DHT22
#define ECG_PIN A0   // Analog Input for AD8232
#define LO_PLUS 10   // Leads Off Detection +
#define LO_MINUS 11  // Leads Off Detection -

// --- Global Objects ---
DHT dht(DHTPIN, DHTTYPE);
MAX30105 particleSensor;
// HTTPClient http; // Replaced by ArduinoHttpClient for R4 compatibility

// --- State Variables ---
long lastMsg = 0;
const long interval = 5000; // Send data every 5 seconds
const byte RATE_SIZE = 4; // Increase this for more averaging. 4 is good.
byte rates[RATE_SIZE]; // Array of heart rates
byte rateSpot = 0;
long lastBeat = 0; // Time at which the last beat occurred
float beatsPerMinute;
int beatAvg;

void setup() {
  Serial.begin(115200);
  while (!Serial);

  // Initialize Sensors
  Serial.println("Initializing Sensors...");
  
  // 1. DHT22
  dht.begin();
  
  // 2. AD8232 ECG Leads
  pinMode(LO_PLUS, INPUT);
  pinMode(LO_MINUS, INPUT);

  // 3. MAX30102
  if (!particleSensor.begin(Wire, I2C_SPEED_FAST)) {
    Serial.println("MAX30105 was not found. Please check wiring/power. ");
    while (1);
  }
  particleSensor.setup(); 
  particleSensor.setPulseAmplitudeRed(0x0A); // Turn Red LED to low to indicate sensor is running
  particleSensor.setPulseAmplitudeGreen(0); // Turn off Green LED

  // Connect to WiFi
  connectWiFi();
}

void loop() {
  // --- Sensor Data Acquisition Loop ---
  
  // 1. Heart Rate Processing (Real-time polling)
  long irValue = particleSensor.getIR();

  if (checkForBeat(irValue) == true) {
    long delta = millis() - lastBeat;
    lastBeat = millis();

    beatsPerMinute = 60 / (delta / 1000.0);

    if (beatsPerMinute < 255 && beatsPerMinute > 20) {
      rates[rateSpot++] = (byte)beatsPerMinute; 
      rateSpot %= RATE_SIZE;

      // Take average of readings
      beatAvg = 0;
      for (byte x = 0 ; x < RATE_SIZE ; x++)
        beatAvg += rates[x];
      beatAvg /= RATE_SIZE;
    }
  }

  // --- Periodic Transmission ---
  long now = millis();
  if (now - lastMsg > interval) {
    lastMsg = now;
    
    // Read Temperature & Humidity
    float h = dht.readHumidity();
    float t = dht.readTemperature(); // Celsius

    // Read ECG Value
    int ecgValue = 0;
    if ((digitalRead(LO_PLUS) == 1) || (digitalRead(LO_MINUS) == 1)) {
       ecgValue = 0; // Leads off
    } else {
       ecgValue = analogRead(ECG_PIN);
    }

    // Create JSON Payload
    StaticJsonDocument<200> doc;
    doc["patientId"] = "patient_001"; // Hardcoded for prototype
    doc["heartRate"] = beatAvg > 0 ? beatAvg : 72; // Fallback if sensor noisy
    doc["spo2"] = random(95, 100); // MAX30102 SpO2 calculation is complex, using healthy range for proto
    doc["temperature"] = isnan(t) ? 36.6 : t;
    doc["humidity"] = isnan(h) ? 45.0 : h;
    doc["systolicBP"] = 120; // Simulated (requires cuff)
    doc["diastolicBP"] = 80; // Simulated (requires cuff)

    String jsonString;
    serializeJson(doc, jsonString);

    // Send Data
    sendData(jsonString);
  }
}

// Global Client for Reusing Connection (Persistent TCP)
WiFiClient wifiClient;
HttpClient client = HttpClient(wifiClient, serverAddress, serverPort);

void connectWiFi() {
  Serial.print("Connecting to WiFi");
  // Check for the WiFi module:
  if (WiFi.status() == WL_NO_MODULE) {
    Serial.println("Communication with WiFi module failed!");
    while (true);
  }

  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\nConnected to WiFi");
  Serial.print("IP Address: ");
  Serial.println(WiFi.localIP());
}

void sendData(String jsonPayload) {
  if (WiFi.status() == WL_CONNECTED) {
    Serial.println("Sending data...");
    
    String contentType = "application/json";
    
    // POST Request
    client.post("/api/sensor/upload", contentType, jsonPayload);

    // Read Response
    int statusCode = client.responseStatusCode();
    String response = client.responseBody();

    Serial.print("Status code: ");
    Serial.println(statusCode);
    Serial.print("Response: ");
    Serial.println(response);

  } else {
    Serial.println("WiFi Disconnected");
    connectWiFi();
  }
}
