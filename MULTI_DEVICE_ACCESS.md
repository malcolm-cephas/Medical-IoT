# Secure Multi-Device Access Configuration

This document outlines the changes made to enable secure multi-device access for the Medical IoT Monitoring System.

## Architecture Changes

### 1. Network Interface Configuration
- **Spring Boot Backend**: Configured to listen on `0.0.0.0` (all interfaces) instead of `localhost`. This allows devices on the same Wi-Fi network to reach the API via the host machine's local IP address.
- **Frontend (Vite)**: Configured with `server.host: '0.0.0.0'` and `server.port: 5173`.
- **Python Analytics**: Set to listen on `0.0.0.0` on port `4242`.

### 2. Dynamic API Connectivity
- Created `frontend-dashboard/src/config.js` to dynamically resolve the backend and analytics IP addresses based on the current URL (`window.location.hostname`).
- Removed all hardcoded `localhost` references in the React components.

### 3. Real-Time Synchronization (WebSockets)
- Implemented **Spring WebSocket (STOMP)** for instant data propagation.
- **Topics**:
  - `/topic/vitals/{patientId}`: Real-time vitals for specific patient views.
  - `/topic/ward`: Aggregated updates for the nurse station/doctor monitoring list.
  - `/topic/alerts`: Instant medical and security alerts across all authorized devices.

## Demonstration Output
- **URL**: `http://local:5173`
- **Configuration**:
  - Backend: Listening on `0.0.0.0:8080`
  - Frontend: Listening on `0.0.0.0:5173`
  - Real-time Sync: WebSockets (STOMP)

## Security Justification

- **Authentication**: All devices must authenticate via login. Basic authentication is required for every API call.
- **CORS Management**: Securely configured to allow the frontend to communicate with the backend across different devices while maintaining credential integrity.
- **Authorization**: Role-based access control (RBAC) remains active. A device logged in as a 'Patient' cannot access 'Doctor' topics or endpoints.
- **Compliance**: Encrypted communication and CP-ABE based data protection remain enforced. Network access expands the *reach* but does not decrease the *security* of the data.

## Steps to Enable Multi-Device Access

1.  **Configure Local Hostname**:
    - For `medisecure` to work, you must add your local IP to your `hosts` file.
    - Path: `C:\Windows\System32\drivers\etc\hosts`
    - Line: `127.0.0.1  medisecure` (on host)
    - Line: `<your-ip>  medisecure` (on other devices)
2.  **Start the Services**:
    - Run `run_all.bat`.
3.  **Access from other devices**:
    - Open a browser on your phone/tablet/other laptop on the same Wi-Fi.
    - Navigate to `http://medisecure:5173`.
    - Log in with authorized credentials.

## Testing Procedure

1.  **Simultaneous Access**: Open the dashboard on a PC and a mobile phone. Log in with different roles.
2.  **Real-Time Sync**: Observe that vital updates (from `mock_data_generator.py`) appear on both devices simultaneously without page refreshes.
3.  **Alert Propagation**: Trigger a critical alert (or wait for the generator to simulate one) and verify notification appearances on all dashboards.
4.  **Security Check**: Attempt to access the API directly from an unauthorized device; it should return a `401 Unauthorized` or `403 Forbidden` error.

## Demonstration Output

### Real-Time Synchronization
- **Scenario**: A patient is being monitored.
- **Action**: `mock_data_generator.py` sends a new heart rate record.
- **Result**: Both the doctor's tablet and the nurse's station monitor show the update within milliseconds via the STOMP WebSocket topic `/topic/vitals/123`.

### Multi-Device Login
- **Scenario**: Admin accesses the system from a laptop, while a Doctor accesses it from a smartphone.
- **Action**: Admin checks the `/api/admin/sessions/active` endpoint.
- **Result**: The endpoint returns both active sessions, showing simultaneous authorized access.

### Alert Propagation
- **Scenario**: Analytics service detects a risk score > 50.
- **Action**: Analytics service broadcasts to `/topic/alerts`.
- **Result**: All connected dashboards receive a browser notification: "🚨 MEDICAL ALERT: Patient 123: Low Oxygen Saturation".
