# Doctor-Patient Appointment System Integration

## Overview
This integration adapts the doctor-patient appointment functionality from the NestJS repository (https://github.com/MarcusFranklin-GIT/doctor-patient-api) into the existing Spring Boot Medical IoT System.

## Key Features Implemented

### 1. **Doctor Availability Management**
Doctors can set their available time slots for patient appointments.

**Endpoint**: `POST /api/doctor/set-availability`
**Headers**: `X-User-Id: doctor_micheal`
**Request Body**:
```json
{
  "daysOfWeek": ["MONDAY", "WEDNESDAY"],
  "startTime": "10:00:00",
  "endTime": "18:00:00"
}
```

**Response**:
```json
{
  "message": "Availability set successfully for 2 days",
  "slots": [
    {
      "id": 1,
      "doctorId": "doctor_micheal",
      "dayOfWeek": "MONDAY",
      "startTime": "10:00:00",
      "endTime": "18:00:00"
    }
  ]
}
```

### 2. **View Available Doctors**
Patients can view all available doctors in the system.

**Endpoint**: `GET /api/patient/all-doctors`

**Response**:
```json
[
  {
    "id": 1,
    "username": "doctor_micheal",
    "department": "CARDIOLOGY",
    "role": "DOCTOR"
  }
]
```

### 3. **View Doctor's Available Slots**
Patients can view available time slots for a specific doctor.

**Endpoint**: `GET /api/patient/all-doctors/{doctorId}/slots`

**Example**: `GET /api/patient/all-doctors/doctor_micheal/slots`

**Response**:
```json
[
  {
    "id": 1,
    "doctorId": "doctor_micheal",
    "fromTime": "2025-06-29T09:00:00",
    "toTime": "2025-06-29T17:00:00",
    "status": "AVAILABLE"
  }
]
```

### 4. **Book an Appointment**
Patients can book appointments with doctors by selecting an available slot.

**Endpoint**: `POST /api/patient/book-appointment/{slotId}`
**Headers**: `X-User-Id: patient_alpha`

**Example**: `POST /api/patient/book-appointment/1`

**Response**:
```json
{
  "message": "Appointment booked successfully",
  "appointment": {
    "appointmentId": 1,
    "doctorName": "doctor_micheal",
    "doctorDepartment": "CARDIOLOGY",
    "patientName": "patient_alpha",
    "fromTime": "2025-06-29T09:00:00",
    "toTime": "2025-06-29T17:00:00",
    "status": "SCHEDULED"
  }
}
```

### 5. **View Patient Appointments**
Patients can view all their appointments.

**Endpoint**: `GET /api/patient/appointments`
**Headers**: `X-User-Id: patient_alpha`

### 6. **View Doctor Appointments**
Doctors can view all their appointments.

**Endpoint**: `GET /api/doctor/appointments`
**Headers**: `X-User-Id: doctor_micheal`

### 7. **Cancel Appointment**
Both patients and doctors can cancel appointments.

**Endpoint**: `POST /api/patient/appointments/{appointmentId}/cancel`
**Headers**: `X-User-Id: patient_alpha`

### 8. **Complete Appointment**
Doctors can mark appointments as completed.

**Endpoint**: `POST /api/doctor/appointments/{appointmentId}/complete`
**Headers**: `X-User-Id: doctor_micheal`

## Database Schema

### New Tables Created

#### `doctor_availability`
| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT | Primary key |
| doctor_id | VARCHAR | Username of the doctor |
| from_time | DATETIME | Start time of availability |
| to_time | DATETIME | End time of availability |
| status | VARCHAR | AVAILABLE, BOOKED, CANCELLED |
| created_at | DATETIME | Creation timestamp |

#### `appointments`
| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT | Primary key |
| doctor_id | VARCHAR | Username of the doctor |
| patient_id | VARCHAR | Username of the patient |
| slot_id | BIGINT | References doctor_availability.id |
| status | VARCHAR | SCHEDULED, COMPLETED, CANCELLED |
| notes | TEXT | Optional appointment notes |
| appointment_time | DATETIME | Scheduled appointment time |
| created_at | DATETIME | Creation timestamp |

## Integration with Existing Consent System

The appointment system works seamlessly with the existing consent management:

1. **Automatic Consent Request**: When a patient books an appointment, the system can automatically create a consent request for the doctor to access the patient's medical data.

2. **Consent-Based Access**: Doctors can only view detailed patient vitals if consent is granted, even if an appointment is booked.

3. **Appointment History**: Both consent and appointment history are tracked separately but can be linked for audit purposes.

## Key Differences from NestJS Repository

| Feature | NestJS (Original) | Spring Boot (Adapted) |
|---------|-------------------|----------------------|
| Database | MongoDB | MySQL (JPA) |
| Authentication | JWT Guards | Basic Auth + Headers |
| Language | TypeScript | Java |
| Framework | NestJS | Spring Boot |
| Schema | Mongoose Schemas | JPA Entities |
| Validation | class-validator | Manual validation |

## Quick Start Guide

### Starting the System
Run the launcher to start all services:
```bash
run_all.bat
```

This starts:
- Backend (Port 8080)
- Frontend (Port 5173)
- Analytics (Port 4242)

### Testing the Appointment Flow

#### Step 1: Doctor Sets Availability
1. Login as `doctor_micheal` / `<your-password>`
2. Click "📅 Appointments" tab
3. Set availability (e.g., tomorrow 9 AM - 5 PM)
4. Click "Set Availability"

#### Step 2: Patient Books Appointment
1. Logout and login as `patient_alpha` / `<your-password>`
2. Click "📅 Appointments" tab
3. Click on a doctor card to view slots
4. Click "Book Now" on a preferred slot

#### Step 3: Doctor Manages Appointment
1. Login as `doctor_micheal` / `<your-password>`
2. Go to "📅 Appointments" tab
3. View booked appointments
4. Click "Mark as Completed" after consultation

## Testing the Integration

### 1. Set Doctor Availability
```bash
curl -X POST http://localhost:8080/api/doctor/set-availability \
  -H "Content-Type: application/json" \
  -H "X-User-Id: doctor_micheal" \
  -d '{
    "daysOfWeek": ["MONDAY"],
    "startTime": "10:00:00",
    "endTime": "17:00:00"
  }'
```

### 2. View Available Doctors
```bash
curl http://localhost:8080/api/patient/all-doctors
```

### 3. View Doctor Slots
```bash
curl http://localhost:8080/api/patient/all-doctors/doctor_micheal/slots
```

### 4. Book Appointment
```bash
curl -X POST http://localhost:8080/api/patient/book-appointment/1 \
  -H "X-User-Id: patient_alpha"
```

## Future Enhancements

1. **Email Notifications**: Send email confirmations when appointments are booked/cancelled
2. **Reminder System**: Automated reminders before appointments
3. **Video Consultation**: Integration with telemedicine platforms
4. **Appointment Notes**: Allow doctors to add notes during/after appointments
5. **Recurring Appointments**: Support for recurring appointment schedules
6. **Waitlist Management**: Automatic notification when slots become available

## Security Considerations

- **Authentication**: Currently using header-based user identification for simplicity. Should be replaced with JWT tokens in production.
- **Authorization**: Verify that users can only access their own appointments and data.
- **Data Validation**: All time ranges are validated to prevent invalid bookings.
- **Concurrent Booking Prevention**: Transaction management prevents double-booking of slots.

## Files Created/Modified

### New Files
- `model/DoctorAvailability.java` - Entity for doctor time slots
- `model/Appointment.java` - Entity for appointments
- `repository/DoctorAvailabilityRepository.java` - Repository for availability
- `repository/AppointmentRepository.java` - Repository for appointments
- `dto/AvailabilityDto.java` - DTO for availability requests
- `service/DoctorAvailabilityService.java` - Business logic for availability
- `service/AppointmentService.java` - Business logic for appointments
- `controller/DoctorController.java` - Doctor-facing endpoints
- `controller/PatientAppointmentController.java` - Patient-facing endpoints

### Modified Files
- `config/SecurityConfig.java` - Added new endpoint permissions

## Conclusion

This integration successfully adapts the NestJS doctor-patient appointment system into the existing Spring Boot Medical IoT System, maintaining compatibility with the current architecture while adding powerful new functionality for managing doctor-patient interactions.
