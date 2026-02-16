# MedVault API Reference Guide

Complete REST API documentation for the Patient-Doctor Appointment and Prescription Management System.

---

## 🔐 Authentication

All endpoints except `/api/auth/**` and `/api/doctors` require JWT token in Authorization header:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Token Obtained From**:
```
POST /api/auth/login
Body: { "username": "doctor1", "password": "password123" }
Response: { "token": "...", "user": { "id": 1, "username": "doctor1", "role": "DOCTOR" } }
```

---

## 📋 DOCTOR ENDPOINTS

### 1. Get Doctor Profile
```
GET /api/doctor/profile
Authorization: Bearer {token}

Response (200):
{
  "id": 5,
  "username": "dr_smith",
  "fullName": "Dr. John Smith",
  "specialization": "Cardiologist",
  "qualification": "MD, Board Certified",
  "experienceYears": 10,
  "hospitalName": "City Medical Center",
  "phone": "555-0001",
  "bio": "Expert in heart disease treatment"
}
```

### 2. Update Doctor Profile
```
PUT /api/doctor/profile
Authorization: Bearer {token}
Content-Type: application/json

Request Body:
{
  "fullName": "Dr. Jane Smith",
  "specialization": "Cardiologist",
  "qualification": "MD, Board Certified",
  "experienceYears": 10,
  "hospitalName": "City Medical Center",
  "phone": "555-0001",
  "bio": "Expert in heart disease treatment"
}

Response (200):
{
  "id": 5,
  "username": "dr_smith",
  ... (updated profile)
}
```

### 3. Get All Appointments [Existing]
```
GET /api/doctor/appointments
Authorization: Bearer {token}

Response (200):
[
  {
    "id": 45,
    "patient": { "id": 10, "username": "john_doe" },
    "doctor": { "id": 5, "username": "dr_smith" },
    "appointmentDate": "2024-02-20T14:30:00",
    "status": "SCHEDULED",
    "symptoms": "High blood pressure",
    "consultationNotes": "",
    "notes": "Regular checkup",
    "createdAt": "2024-02-20T10:00:00"
  },
  ...
]
```

### 4. Get Today's Appointments [NEW]
```
GET /api/doctor/appointments/today
Authorization: Bearer {token}

Response (200):
[
  {
    "id": 45,
    "patient": { "id": 10, "username": "john_doe" },
    "appointmentDate": "2024-02-20T14:30:00",
    "status": "SCHEDULED",
    "symptoms": "High blood pressure",
    ...
  }
]

Note: Returns ONLY appointments scheduled for today
```

### 5. Get Appointments by Status [NEW]
```
GET /api/doctor/appointments/status/SCHEDULED
Authorization: Bearer {token}

Query Parameters:
- status: SCHEDULED, COMPLETED, CANCELLED, NO_SHOW

Response (200):
[
  {
    "id": 45,
    "patient": { "id": 10, "username": "john_doe" },
    "status": "SCHEDULED",
    ...
  }
]
```

### 6. Get Appointment Details [NEW]
```
GET /api/doctor/appointments/45
Authorization: Bearer {token}

Response (200):
{
  "id": 45,
  "patient": {
    "id": 10,
    "username": "john_doe",
    "fullName": "John Doe",
    "age": 35,
    "gender": "Male",
    "bloodGroup": "O+",
    "phone": "555-1234",
    "address": "123 Main St",
    "emergencyContact": "Jane Doe, 555-5678"
  },
  "doctor": { "id": 5, "username": "dr_smith" },
  "appointmentDate": "2024-02-20T14:30:00",
  "status": "SCHEDULED",
  "symptoms": "High blood pressure, dizziness",
  "consultationNotes": "Patient reports symptoms for 2 weeks",
  "notes": "Regular checkup recommended",
  "prescription": null,
  "createdAt": "2024-02-20T10:00:00"
}
```

### 7. Update Appointment Status [NEW]
```
PUT /api/doctor/appointments/45/status
Authorization: Bearer {token}
Content-Type: application/json

Request Body:
{
  "status": "COMPLETED"
}

Allowed Status Values:
- SCHEDULED
- COMPLETED
- CANCELLED
- NO_SHOW

Response (200):
{
  "id": 45,
  "status": "COMPLETED",
  ...
}

Response (404):
{
  "error": "Appointment not found"
}
```

### 8. Save Consultation Notes [NEW]
```
PUT /api/doctor/appointments/45/consultation
Authorization: Bearer {token}
Content-Type: application/json

Request Body:
{
  "symptoms": "High blood pressure, dizziness, fatigue",
  "consultationNotes": "Physical examination completed. BP reading 150/95. Recommended diet change and exercise. Prescribed Lisinopril 10mg daily.",
  "notes": "Follow-up in 2 weeks"
}

Response (200):
{
  "id": 45,
  "symptoms": "High blood pressure, dizziness, fatigue",
  "consultationNotes": "Physical examination completed...",
  "notes": "Follow-up in 2 weeks",
  ...
}
```

### 9. Create/Update Prescription [Existing - ENHANCED]
```
POST /api/doctor/appointments/45/prescriptions
Authorization: Bearer {token}
Content-Type: application/json

Request Body:
{
  "diagnosis": "Essential Hypertension",
  "medicinesJson": "[{\"name\": \"Lisinopril\", \"dosage\": \"10mg\", \"frequency\": \"Once daily\", \"duration\": \"30 days\"}, {\"name\": \"Atorvastatin\", \"dosage\": \"20mg\", \"frequency\": \"Once daily at night\", \"duration\": \"30 days\"}]",
  "testsRecommended": "Blood pressure monitoring, Lipid panel, Kidney function test",
  "followUpDate": "2024-03-10",
  "notes": "Continue current diet and exercise routine. Reduce salt intake. Monitor blood pressure daily."
}

Response (201):
{
  "id": 123,
  "appointmentId": 45,
  "diagnosis": "Essential Hypertension",
  "medicinesJson": "[{\"name\": \"Lisinopril\", ...}]",
  "testsRecommended": "Blood pressure monitoring...",
  "followUpDate": "2024-03-10",
  "notes": "Continue current diet...",
  "createdAt": "2024-02-20T15:30:00"
}
```

### 10. Get Prescription for Appointment [NEW]
```
GET /api/doctor/appointments/45/prescriptions
Authorization: Bearer {token}

Response (200):
{
  "id": 123,
  "appointmentId": 45,
  "diagnosis": "Essential Hypertension",
  "medicinesJson": "[{\"name\": \"Lisinopril\", ...}]",
  ...
}

Response (404):
{
  "error": "Prescription not found"
}
```

---

## 👤 PATIENT ENDPOINTS

### 1. Get Patient Profile
```
GET /api/patient/profile
Authorization: Bearer {token}

Response (200):
{
  "id": 10,
  "username": "john_doe",
  "fullName": "John Doe",
  "age": 35,
  "gender": "Male",
  "bloodGroup": "O+",
  "phone": "555-1234",
  "address": "123 Main St, City, State 12345",
  "emergencyContact": "Jane Doe (Wife), 555-5678",
  "medicalHistory": "Hypertension family history, No surgeries"
}
```

### 2. Update Patient Profile
```
PUT /api/patient/profile
Authorization: Bearer {token}
Content-Type: application/json

Request Body:
{
  "fullName": "John Doe",
  "age": 35,
  "gender": "Male",
  "bloodGroup": "O+",
  "phone": "555-1234",
  "address": "123 Main St, City, State 12345",
  "emergencyContact": "Jane Doe (Wife), 555-5678",
  "medicalHistory": "Hypertension family history"
}

Response (200):
{
  "id": 10,
  ... (updated profile)
}
```

### 3. Book Appointment [Existing - ENHANCED]
```
POST /api/patient/appointments
Authorization: Bearer {token}
Content-Type: application/json

Request Body:
{
  "doctorId": 5,
  "appointmentDate": "2024-02-20T14:30:00",
  "symptoms": "High blood pressure, occasional dizziness",
  "notes": "First visit to this doctor"
}

Response (201):
{
  "id": 45,
  "patient": { "id": 10, "username": "john_doe" },
  "doctor": { "id": 5, "username": "dr_smith" },
  "appointmentDate": "2024-02-20T14:30:00",
  "status": "SCHEDULED",
  "symptoms": "High blood pressure, occasional dizziness",
  "consultationNotes": "",
  "notes": "First visit to this doctor",
  "createdAt": "2024-02-20T11:00:00"
}
```

### 4. Get All Appointments [Existing]
```
GET /api/patient/appointments
Authorization: Bearer {token}

Response (200):
[
  {
    "id": 45,
    "doctor": { "id": 5, "username": "dr_smith", "fullName": "Dr. John Smith", "specialization": "Cardiologist" },
    "appointmentDate": "2024-02-20T14:30:00",
    "status": "SCHEDULED",
    "symptoms": "High blood pressure",
    "createdAt": "2024-02-20T10:00:00"
  },
  ...
]
```

### 5. Get Upcoming Appointments [NEW]
```
GET /api/patient/appointments/upcoming
Authorization: Bearer {token}

Response (200):
[
  {
    "id": 45,
    "doctor": { "id": 5, "username": "dr_smith" },
    "appointmentDate": "2024-02-25T10:00:00",
    "status": "SCHEDULED",
    ...
  }
]

Note: Returns ONLY appointments in the future (appointmentDate > now)
```

### 6. Get Appointment Details [NEW]
```
GET /api/patient/appointments/45
Authorization: Bearer {token}

Response (200):
{
  "id": 45,
  "doctor": {
    "id": 5,
    "username": "dr_smith",
    "fullName": "Dr. John Smith",
    "specialization": "Cardiologist",
    "qualification": "MD, Board Certified",
    "hospitalName": "City Medical Center"
  },
  "appointmentDate": "2024-02-20T14:30:00",
  "status": "COMPLETED",
  "symptoms": "High blood pressure, dizziness",
  "consultationNotes": "Physical exam completed. BP reading 150/95. Referred to medication.",
  "notes": "Regular checkup",
  "prescription": {
    "id": 123,
    "diagnosis": "Essential Hypertension",
    ...
  },
  "createdAt": "2024-02-20T10:00:00"
}
```

### 7. Get All Prescriptions [Existing]
```
GET /api/patient/prescriptions
Authorization: Bearer {token}

Response (200):
[
  {
    "id": 123,
    "appointmentId": 45,
    "diagnosis": "Essential Hypertension",
    "medicinesJson": "[{\"name\": \"Lisinopril\", ...}]",
    "testsRecommended": "Blood pressure monitoring",
    "followUpDate": "2024-03-10",
    "notes": "Continue current diet...",
    "createdAt": "2024-02-20T15:30:00"
  },
  ...
]
```

### 8. Get Single Prescription [NEW]
```
GET /api/patient/prescriptions/45
Authorization: Bearer {token}

Path Parameter:
- 45 = appointmentId

Response (200):
{
  "id": 123,
  "appointmentId": 45,
  "diagnosis": "Essential Hypertension",
  "medicinesJson": "[{\"name\": \"Lisinopril\", \"dosage\": \"10mg\", \"frequency\": \"Once daily\", \"duration\": \"30 days\"}, {\"name\": \"Atorvastatin\", \"dosage\": \"20mg\", \"frequency\": \"Once daily at night\", \"duration\": \"30 days\"}]",
  "testsRecommended": "Blood pressure monitoring, Lipid panel",
  "followUpDate": "2024-03-10",
  "notes": "Continue current routine...",
  "createdAt": "2024-02-20T15:30:00"
}

Response (404):
{
  "error": "Prescription not found for appointment"
}
```

### 9. Get Follow-up Prescriptions [NEW]
```
GET /api/patient/follow-ups
Authorization: Bearer {token}

Response (200):
[
  {
    "id": 123,
    "appointmentId": 45,
    "diagnosis": "Essential Hypertension",
    "medicinesJson": "[...]",
    "followUpDate": "2024-03-10",
    "notes": "Return for checkup",
    ...
  },
  {
    "id": 125,
    "appointmentId": 48,
    "diagnosis": "Type 2 Diabetes",
    "followUpDate": "2024-03-15",
    ...
  }
]

Note: Returns ONLY prescriptions where followUpDate > today
```

---

## 🏥 PUBLIC ENDPOINTS

### Get All Doctors (No Auth Required)
```
GET /api/doctors

Response (200):
[
  {
    "id": 5,
    "username": "dr_smith",
    "fullName": "Dr. John Smith",
    "specialization": "Cardiologist",
    "hospitalName": "City Medical Center",
    "phone": "555-0001"
  },
  ...
]
```

---

## 🔑 Authentication Endpoints

### 1. Register New User
```
POST /api/auth/register
Content-Type: application/json

Request Body:
{
  "username": "newdoctor",
  "password": "secure_password_123",
  "role": "DOCTOR"
}

Valid Roles:
- PATIENT
- DOCTOR
- ADMIN

Response (201):
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 15,
    "username": "newdoctor",
    "role": "DOCTOR"
  }
}

Response (400):
{
  "error": "Username already exists"
}
```

### 2. Login
```
POST /api/auth/login
Content-Type: application/json

Request Body:
{
  "username": "doctor1",
  "password": "password123"
}

Response (200):
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 5,
    "username": "doctor1",
    "role": "DOCTOR"
  }
}

Response (401):
{
  "error": "Invalid credentials"
}
```

### 3. Forgot Password
```
POST /api/auth/forgot-password
Content-Type: application/json

Request Body:
{
  "email": "doctor@example.com"
}

Response (200):
{
  "message": "Password reset email sent"
}

Note: Email must be registered user's email
```

### 4. Reset Password
```
POST /api/auth/reset-password
Content-Type: application/json

Request Body:
{
  "token": "reset_token_from_email",
  "newPassword": "new_secure_password_123"
}

Response (200):
{
  "message": "Password reset successful"
}
```

---

## 📊 Data Models

### Appointment Status Enum
```
SCHEDULED  - Appointment is booked and upcoming
COMPLETED  - Appointment has occurred
CANCELLED  - Appointment was cancelled by doctor/patient
NO_SHOW    - Patient did not show up to appointment
```

### Medicine JSON Structure
```json
{
  "name": "Amoxicillin",
  "dosage": "500mg",
  "frequency": "3x daily",
  "duration": "7 days"
}
```

### Appointment Object
```json
{
  "id": 45,
  "patient": { "id": 10, "username": "john_doe" },
  "doctor": { "id": 5, "username": "dr_smith" },
  "appointmentDate": "2024-02-20T14:30:00",
  "status": "SCHEDULED",
  "symptoms": "High blood pressure",
  "consultationNotes": "",
  "notes": "Regular checkup",
  "prescription": { "id": 123, ... },
  "createdAt": "2024-02-20T10:00:00"
}
```

### Prescription Object
```json
{
  "id": 123,
  "appointmentId": 45,
  "diagnosis": "Essential Hypertension",
  "medicinesJson": "[{\"name\": \"Lisinopril\", ...}]",
  "testsRecommended": "Blood pressure monitoring",
  "followUpDate": "2024-03-10",
  "notes": "Continue current routine",
  "createdAt": "2024-02-20T15:30:00"
}
```

---

## ❌ Error Responses

### 400 Bad Request
```json
{
  "error": "Invalid input data"
}
```

### 401 Unauthorized
```json
{
  "error": "Missing or invalid authentication token"
}
```

### 403 Forbidden
```json
{
  "error": "Access denied - insufficient permissions"
}
```

### 404 Not Found
```json
{
  "error": "Resource not found"
}
```

### 500 Server Error
```json
{
  "error": "Internal server error"
}
```

---

## 🧪 Testing with cURL

### Get Today's Appointments (Doctor)
```bash
curl -X GET http://localhost:8081/api/doctor/appointments/today \
  -H "Authorization: Bearer YOUR_DOCTOR_TOKEN"
```

### Create Prescription (Doctor)
```bash
curl -X POST http://localhost:8081/api/doctor/appointments/45/prescriptions \
  -H "Authorization: Bearer YOUR_DOCTOR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "diagnosis": "Hypertension",
    "medicinesJson": "[{\"name\": \"Lisinopril\", \"dosage\": \"10mg\", \"frequency\": \"Once daily\", \"duration\": \"30 days\"}]",
    "testsRecommended": "Blood pressure monitoring",
    "followUpDate": "2024-03-10",
    "notes": "Take medication as prescribed"
  }'
```

### Get Upcoming Appointments (Patient)
```bash
curl -X GET http://localhost:8081/api/patient/appointments/upcoming \
  -H "Authorization: Bearer YOUR_PATIENT_TOKEN"
```

### Get Prescription (Patient)
```bash
curl -X GET http://localhost:8081/api/patient/prescriptions/45 \
  -H "Authorization: Bearer YOUR_PATIENT_TOKEN"
```

---

## 📈 Response Codes

| Code | Meaning |
|------|---------|
| 200 | OK - Request successful |
| 201 | Created - Resource created successfully |
| 400 | Bad Request - Invalid parameters |
| 401 | Unauthorized - Missing/invalid token |
| 403 | Forbidden - No permission |
| 404 | Not Found - Resource doesn't exist |
| 500 | Server Error - Internal error |

---

## 🔄 Common Request/Response Patterns

### Doctor Workflow - Create Prescription
```
1. GET /api/doctor/appointments/today
   → Get list of today's appointments

2. GET /api/doctor/appointments/{appointmentId}
   → Get appointment details and patient info

3. PUT /api/doctor/appointments/{appointmentId}/consultation
   → Save consultation notes

4. POST /api/doctor/appointments/{appointmentId}/prescriptions
   → Create prescription

5. PUT /api/doctor/appointments/{appointmentId}/status
   → Mark appointment as COMPLETED
```

### Patient Workflow - View History
```
1. GET /api/patient/profile
   → Get patient info

2. GET /api/patient/appointments
   → Get all appointments

3. GET /api/patient/appointments/{appointmentId}
   → Get specific appointment with doctor details

4. GET /api/patient/prescriptions/{appointmentId}
   → View prescription for that appointment

5. GET /api/patient/follow-ups
   → Check upcoming follow-up appointments
```

---

**Last Updated**: 2024
**API Version**: 1.0
**Backend**: Spring Boot 3.x with JPA
**Status**: Production Ready
