# MedVault API Reference - Consent & Audit Logging

Complete documentation for Patient Consent Management and Audit Logging features.

---

## 🔐 PATIENT CONSENT MANAGEMENT

### 1. Grant Consent for Doctor Access
Grant permission for a doctor to access your medical records.

```
POST /api/consent/grant
Authorization: Bearer {token}
Content-Type: application/x-www-form-urlencoded

Parameters:
- patientId: 1 (Long, required)
- doctorId: 5 (Long, required)
- reason: "Health checkup and consultation" (String, optional)

Response (200):
{
  "success": true,
  "data": {
    "id": 10,
    "patientId": 1,
    "doctorId": 5,
    "consentGranted": true,
    "grantedAt": "2024-03-06T15:30:00",
    "revokedAt": null,
    "consentReason": "Health checkup and consultation"
  },
  "message": "Consent granted successfully for doctor to access patient records"
}

Response (401 - Unauthorized):
{
  "success": false,
  "error": "Unauthorized",
  "message": "Only the patient or an admin can grant consent"
}
```

### 2. Revoke Consent for Doctor Access
Remove a doctor's permission to access your medical records.

```
POST /api/consent/revoke
Authorization: Bearer {token}
Content-Type: application/x-www-form-urlencoded

Parameters:
- patientId: 1 (Long, required)
- doctorId: 5 (Long, required)

Response (200):
{
  "success": true,
  "data": {
    "id": 10,
    "patientId": 1,
    "doctorId": 5,
    "consentGranted": true,
    "grantedAt": "2024-03-06T15:30:00",
    "revokedAt": "2024-03-07T10:15:00",
    "consentReason": "Health checkup and consultation"
  },
  "message": "Consent revoked successfully"
}

Response (404 - Not Found):
{
  "success": false,
  "error": "Not Found",
  "message": "Consent not found for patient 1 and doctor 5"
}
```

### 3. Get Consent Details
Retrieve the current consent status and details between a patient and doctor.

```
GET /api/consent?patientId=1&doctorId=5
Authorization: Bearer {token}

Response (200):
{
  "success": true,
  "data": {
    "id": 10,
    "patientId": 1,
    "doctorId": 5,
    "consentGranted": true,
    "grantedAt": "2024-03-06T15:30:00",
    "revokedAt": null,
    "consentReason": "Health checkup and consultation"
  },
  "message": "Consent details retrieved successfully"
}
```

### 4. Check Active Consent
Quick check to verify if a doctor has active access to patient records.

```
GET /api/consent/check?patientId=1&doctorId=5
Authorization: Bearer {token}

Response (200 - Has Consent):
{
  "success": true,
  "data": true,
  "message": "Consent status retrieved successfully"
}

Response (200 - No Consent):
{
  "success": true,
  "data": false,
  "message": "Consent status retrieved successfully"
}
```

**Roles Required**: PATIENT, DOCTOR, ADMIN (for grant/revoke: PATIENT or ADMIN only)

---

## 📋 AUDIT LOGGING & COMPLIANCE

### 1. Get Patient Audit Logs
Retrieve all access and modification events for a specific patient's records.

```
GET /api/audit-logs/patient/{patientId}
Authorization: Bearer {token}
Parameters:
  page: 0 (int, default: 0)
  size: 10 (int, default: 10)
  sortBy: "timestamp" (String, default: timestamp)
  sortDirection: "desc" (String, default: desc)

Response (200):
{
  "success": true,
  "data": {
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 25,
    "totalPages": 3,
    "auditLogs": [
      {
        "id": 101,
        "recordId": null,
        "patientId": 1,
        "performedBy": 5,
        "role": "DOCTOR",
        "actionType": "CONSENT_GRANTED",
        "timestamp": "2024-03-06T15:30:00",
        "details": "Granted consent to doctor ID: 5, Reason: Health checkup"
      },
      {
        "id": 100,
        "recordId": 45,
        "patientId": 1,
        "performedBy": 1,
        "role": "PATIENT",
        "actionType": "UPLOAD",
        "timestamp": "2024-03-05T10:15:00",
        "details": "Uploaded medical record"
      },
      {
        "id": 99,
        "recordId": 45,
        "patientId": 1,
        "performedBy": 5,
        "role": "DOCTOR",
        "actionType": "VIEW",
        "timestamp": "2024-03-04T09:20:00",
        "details": "Viewed medical record"
      }
    ]
  },
  "message": "Patient audit logs retrieved successfully"
}
```

### 2. Get Medical Record Audit Logs
View all activities (views, updates, deletes) on a specific medical record.

```
GET /api/audit-logs/record/{recordId}
Authorization: Bearer {token}
Parameters:
  page: 0 (int, default: 0)
  size: 10 (int, default: 10)
  sortBy: "timestamp" (String, default: timestamp)
  sortDirection: "desc" (String, default: desc)

Response (200):
{
  "success": true,
  "data": {
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 8,
    "totalPages": 1,
    "auditLogs": [
      {
        "id": 100,
        "recordId": 45,
        "patientId": 1,
        "performedBy": 1,
        "role": "PATIENT",
        "actionType": "UPLOAD",
        "timestamp": "2024-03-05T10:15:00",
        "details": "Uploaded medical record - Blood Test Report"
      },
      {
        "id": 99,
        "recordId": 45,
        "patientId": 1,
        "performedBy": 5,
        "role": "DOCTOR",
        "actionType": "VIEW",
        "timestamp": "2024-03-04T09:20:00",
        "details": "Viewed medical record"
      }
    ]
  },
  "message": "Record audit logs retrieved successfully"
}
```

### 3. Filter Audit Logs by Action Type
View specific types of actions (VIEW, UPLOAD, UPDATE, DELETE, CONSENT_GRANTED, CONSENT_REVOKED).

```
GET /api/audit-logs/patient/{patientId}/action/{actionType}
Authorization: Bearer {token}
Parameters:
  page: 0 (int, default: 0)
  size: 10 (int, default: 10)
  sortBy: "timestamp" (String, default: timestamp)
  sortDirection: "desc" (String, default: desc)

Example: GET /api/audit-logs/patient/1/action/VIEW

Response (200):
{
  "success": true,
  "data": {
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 5,
    "totalPages": 1,
    "auditLogs": [
      {
        "id": 99,
        "recordId": 45,
        "patientId": 1,
        "performedBy": 5,
        "role": "DOCTOR",
        "actionType": "VIEW",
        "timestamp": "2024-03-04T09:20:00",
        "details": "Viewed medical record"
      }
    ]
  },
  "message": "Patient audit logs filtered by action type retrieved successfully"
}

Supported Action Types:
- UPLOAD: Medical record uploaded
- VIEW: Medical record viewed
- UPDATE: Medical record modified
- DELETE: Medical record deleted
- CONSENT_GRANTED: Access permission granted to doctor
- CONSENT_REVOKED: Access permission revoked from doctor
```

### 4. Query Audit Logs by Date Range
Filter audit logs by a specific time period.

```
GET /api/audit-logs/patient/{patientId}/date-range
Authorization: Bearer {token}
Parameters:
  startTime: "2024-01-01T00:00:00" (DateTime ISO format, required)
  endTime: "2024-12-31T23:59:59" (DateTime ISO format, required)
  page: 0 (int, default: 0)
  size: 10 (int, default: 10)
  sortBy: "timestamp" (String, default: timestamp)
  sortDirection: "desc" (String, default: desc)

Response (200):
{
  "success": true,
  "data": {
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 3,
    "auditLogs": [
      {
        "id": 99,
        "recordId": 45,
        "patientId": 1,
        "performedBy": 5,
        "role": "DOCTOR",
        "actionType": "VIEW",
        "timestamp": "2024-03-04T09:20:00",
        "details": "Viewed medical record"
      }
    ]
  },
  "message": "Patient audit logs retrieved successfully"
}
```

### 5. Query Audit Logs Example (cURL)

```bash
# Get patient's audit logs
curl -X GET "http://localhost:8081/api/audit-logs/patient/1?page=0&size=10&sortBy=timestamp&sortDirection=desc" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json"

# Get logs for specific record
curl -X GET "http://localhost:8081/api/audit-logs/record/45" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Get only CONSENT_GRANTED events
curl -X GET "http://localhost:8081/api/audit-logs/patient/1/action/CONSENT_GRANTED?page=0&size=10" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Get logs within date range
curl -X GET "http://localhost:8081/api/audit-logs/patient/1/date-range?startTime=2024-01-01T00:00:00&endTime=2024-12-31T23:59:59" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Roles Required**: PATIENT, DOCTOR, ADMIN

---

## ⏰ AUTOMATED REMINDERS & NOTIFICATIONS

### Reminder Scheduler Rules

The system automatically sends reminders based on these rules:

#### 1. Appointment Reminders
- **Schedule**: Every hour
- **Trigger**: Appointments within next 24 hours
- **Status**: Needs `reminderSent` = false
- **Message**: "Reminder: You have an appointment at {date/time}"
- **Idempotency**: Checks if unread reminder already exists before creating

#### 2. Prescription Refill Reminders
- **Schedule**: Daily at 8:00 AM
- **Trigger**: Follow-up prescription dates within next 7 days
- **Message**: "Prescription Refill Reminder: Your follow-up prescription visit is on {date}"
- **Idempotency**: Prevents duplicate reminders for same prescription

#### 3. Health Checkup Reminders
- **Schedule**: Daily at 9:00 AM
- **Trigger**: Last completed appointment was more than 6 months ago
- **Frequency Rule**: Maximum one reminder per 30 days per patient
- **Message**: "Health Checkup Reminder: It's been more than 6 months since your last checkup..."
- **Idempotency**: Prevents spam with 30-day cooldown

#### 4. Notification Cleanup
- **Schedule**: Daily at 2:00 AM
- **Action**: Deletes all notifications older than 90 days
- **Purpose**: Database cleanup and performance optimization
- **Idempotency**: Can be run multiple times safely

---

## 🧪 Integration Examples

### Example: Patient Grants Consent to Doctor

```bash
# Step 1: Patient grants consent to doctor
curl -X POST "http://localhost:8081/api/consent/grant" \
  -H "Authorization: Bearer PATIENT_TOKEN" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "patientId=1&doctorId=5&reason=Quarterly checkup"

# Step 2: Audit log is automatically created
# Later, view audit logs to verify consent was granted
curl -X GET "http://localhost:8081/api/audit-logs/patient/1?page=0&size=10" \
  -H "Authorization: Bearer PATIENT_TOKEN"

# Step 3: Doctor checks if they have consent
curl -X GET "http://localhost:8081/api/consent/check?patientId=1&doctorId=5" \
  -H "Authorization: Bearer DOCTOR_TOKEN"
```

### Example: Patient Revokes Doctor Access

```bash
# Step 1: Patient revokes consent
curl -X POST "http://localhost:8081/api/consent/revoke" \
  -H "Authorization: Bearer PATIENT_TOKEN" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "patientId=1&doctorId=5"

# Step 2: Check audit logs show revocation
curl -X GET "http://localhost:8081/api/audit-logs/patient/1/action/CONSENT_REVOKED" \
  -H "Authorization: Bearer PATIENT_TOKEN"

# Step 3: Doctor can no longer access records (consent check returns false)
curl -X GET "http://localhost:8081/api/consent/check?patientId=1&doctorId=5" \
  -H "Authorization: Bearer DOCTOR_TOKEN"
```

---

**Last Updated**: March 2024
**API Version**: 1.0
**Backend**: Spring Boot 3.2.2 with JPA
**Database**: MySQL
**Status**: Production Ready
