# Immutable Audit Logging for Medical-Record Actions

**Status:** ✅ Complete Implementation

---

## Overview

Implemented a comprehensive immutable audit logging system for medical records that tracks all actions performed on records and consents. The system ensures:
- All actions are logged immutably (write-once, read-many)
- Historical traces are never removed, even when records are deleted or updated
- Full audit trail with pagination and filtering capabilities
- Transactional consistency for all operations

---

## 1. Core Enhancements

### ActionType Enum
**File:** `entity/ActionType.java`

Extended to support all required action types:
- `UPLOAD` - Record upload action
- `VIEW` - Record view action
- `UPDATE` - Record update action
- `DELETE` - Record deletion action
- `CONSENT_GRANTED` - Consent granted to doctor
- `CONSENT_REVOKED` - Consent revoked from doctor

### AuditLog Entity
**File:** `entity/AuditLog.java`

**Fields:**
- `id` (PK) - Unique audit log identifier
- `recordId` (FK, nullable) - Associated medical record ID (nullable for consent actions)
- `patientId` (FK, required) - Patient ID for whom the action was performed
- `performedBy` (FK, required) - User ID who performed the action
- `role` (required) - Role of the user who performed the action (PATIENT, DOCTOR, ADMIN)
- `actionType` (required) - Type of action performed (non-modifiable)
- `timestamp` (required, non-modifiable) - When the action occurred
- `details` (optional) - Additional context about the action

**Indexes:**
- `idx_record_id` - For querying by medical record
- `idx_patient_id` - For querying by patient
- `idx_timestamp` - For time-based range queries
- `idx_performed_by` - For querying by actor
- `idx_action_type` - For filtering by action type

**Immutability:**
- Timestamps are set in `@PrePersist` and marked as `updatable = false`
- All fields are effectively immutable once written
- Entity is write-once, append-only

---

## 2. Data Transfer Objects

### AuditLogResponse
**File:** `dto/AuditLogResponse.java`

DTO for returning individual audit log entries with all relevant fields:
- Complete audit log details
- Used for API responses
- Supports pagination

### PaginatedAuditLogResponse
**File:** `dto/PaginatedAuditLogResponse.java`

Wrapper DTO for paginated audit log results:
- `content` - List of AuditLogResponse objects
- `pageNumber` - Current page (0-indexed)
- `pageSize` - Number of items per page
- `totalElements` - Total number of audit logs
- `totalPages` - Total number of pages
- `isFirst` - Whether this is the first page
- `isLast` - Whether this is the last page

---

## 3. Repository Layer

### AuditLogRepository
**File:** `repository/AuditLogRepository.java`

Comprehensive query methods for audit log retrieval:

**Basic Queries:**
- `findByPatientIdOrderByTimestampDesc(patientId, pageable)` - All audit logs for a patient
- `findByRecordIdOrderByTimestampDesc(recordId, pageable)` - All audit logs for a record

**Filtered Queries:**
- `findByPatientIdAndActionTypeOrderByTimestampDesc(patientId, actionType, pageable)`
- `findByRecordIdAndActionTypeOrderByTimestampDesc(recordId, actionType, pageable)`
- `findByPatientIdAndPerformedByAndActionTypeOrderByTimestampDesc(patientId, performer, actionType, pageable)`

**Date Range Queries:**
- `findByPatientIdAndTimestampBetweenOrderByTimestampDesc(patientId, start, end, pageable)`
- `findByRecordIdAndTimestampBetweenOrderByTimestampDesc(recordId, start, end, pageable)`

**Advanced Filtering:**
```java
@Query("SELECT a FROM AuditLog a WHERE a.patientId = :patientId 
        AND (:recordId IS NULL OR a.recordId = :recordId) 
        AND (:actionType IS NULL OR a.actionType = :actionType) 
        AND a.timestamp BETWEEN :startTime AND :endTime 
        ORDER BY a.timestamp DESC")
Page<AuditLog> findAuditLogs(Long patientId, Long recordId, ActionType actionType, 
                             LocalDateTime startTime, LocalDateTime endTime, Pageable pageable)
```

**Non-Paginated Methods** (for admin use):
- `findByPatientIdOrderByTimestampDesc(patientId)` - All logs for patient (non-paginated)
- `findByRecordIdOrderByTimestampDesc(recordId)` - All logs for record (non-paginated)
- `countByPatientId(patientId)` - Total audit logs for patient
- `countByRecordId(recordId)` - Total audit logs for record

---

## 4. Service Layer

### AuditLogService
**File:** `service/AuditLogService.java`

Service for retrieving and filtering audit logs with proper authorization:

**Patient Audit History:**
```java
getPatientAuditLogs(patientId, page, size, sortBy, sortDirection)
  → Returns paginated audit logs for a patient
  → Authorization: Patient (own), Doctor (with consent), Admin
  → Default sort: timestamp DESC
```

**Record Audit History:**
```java
getRecordAuditLogs(recordId, page, size, sortBy, sortDirection)
  → Returns paginated audit logs for a specific record
  → Authorization: Patient (own record), Doctor (with consent), Admin
  → Default sort: timestamp DESC
```

**Filtered Queries:**
```java
getPatientAuditLogsByActionType(patientId, actionType, page, size, sortBy, sortDirection)
  → Filter logs by action type (UPLOAD, VIEW, UPDATE, DELETE, CONSENT_GRANTED, CONSENT_REVOKED)

getPatientAuditLogsByDateRange(patientId, startTime, endTime, page, size, sortBy, sortDirection)
  → Filter logs within a time range

getAuditLogs(patientId, recordId, actionType, startTime, endTime, page, size, sortBy, sortDirection)
  → Advanced filtering with all criteria
```

**Admin Methods:**
```java
getAllPatientAuditLogs(patientId)
  → Non-paginated retrieval of all audit logs for a patient (Admin only)

getAllAuditLogs(page, size, sortBy, sortDirection)
  → System-wide audit logs (Admin only)
```

**Key Features:**
- Pagination support with configurable page size
- Sorting by any field (default: timestamp DESC)
- Authorization checks at service level
- Consent validation for doctors
- Immutable data (read-only transactions)

---

### PatientConsentService
**File:** `service/PatientConsentService.java`

Service for managing patient-doctor consent with audit logging:

**Grant Consent:**
```java
grantConsent(patientId, doctorId, reason)
  → Grants or updates consent for a doctor
  → Logs CONSENT_GRANTED action
  → Authorization: Patient (own records), Admin
  → Transactional: Creates audit entry atomically
```

**Revoke Consent:**
```java
revokeConsent(patientId, doctorId)
  → Revokes consent by setting revokedAt timestamp
  → Logs CONSENT_REVOKED action
  → Authorization: Patient (own records), Admin
  → Immutable: Old consent records never deleted
```

**Check Consent:**
```java
hasActiveConsent(patientId, doctorId) → boolean
  → Checks if doctor has active, non-revoked consent

getConsent(patientId, doctorId) → PatientConsent
  → Retrieves consent details with full history
```

---

## 5. API Controllers

### AuditLogController
**File:** `controller/AuditLogController.java`

REST endpoints for audit log retrieval:

#### Patient Audit Logs
```
GET /api/audit-logs/patient/{patientId}
  Query Parameters:
    - page: Page number (default: 0)
    - size: Items per page (default: 10)
    - sortBy: Sort field (default: timestamp)
    - sortDirection: asc or desc (default: desc)
  
  Response: PaginatedAuditLogResponse
  Authorization: PATIENT, DOCTOR, ADMIN
```

#### Record Audit Logs
```
GET /api/audit-logs/record/{recordId}
  Query Parameters: Same as above
  Response: PaginatedAuditLogResponse
  Authorization: PATIENT, DOCTOR, ADMIN
```

#### Filtered by Action Type
```
GET /api/audit-logs/patient/{patientId}/action/{actionType}
  Query Parameters: page, size, sortBy, sortDirection
  Supported ActionTypes: UPLOAD, VIEW, UPDATE, DELETE, CONSENT_GRANTED, CONSENT_REVOKED
  Response: PaginatedAuditLogResponse
```

#### Date Range Filter
```
GET /api/audit-logs/patient/{patientId}/date-range
  Query Parameters:
    - startTime: ISO 8601 format (required)
    - endTime: ISO 8601 format (required)
    - page, size, sortBy, sortDirection
  
  Response: PaginatedAuditLogResponse
```

#### Advanced Filtering
```
GET /api/audit-logs/filter
  Query Parameters:
    - patientId: Required
    - recordId: Optional
    - actionType: Optional
    - startTime: Optional (ISO 8601)
    - endTime: Optional (ISO 8601)
    - page, size, sortBy, sortDirection
  
  Response: PaginatedAuditLogResponse
```

#### Admin: All Patient Logs (Non-Paginated)
```
GET /api/audit-logs/patient/{patientId}/all
  Response: List<AuditLogResponse>
  Authorization: ADMIN only
```

#### Admin: System Audit Logs
```
GET /api/audit-logs/system
  Query Parameters: page, size, sortBy, sortDirection
  Response: PaginatedAuditLogResponse
  Authorization: ADMIN only
```

### PatientConsentController
**File:** `controller/PatientConsentController.java`

```
POST /api/consent/grant
  Parameters:
    - patientId: Required
    - doctorId: Required
    - reason: Optional
  
  Response: PatientConsent
  Authorization: PATIENT, ADMIN
  Logs: CONSENT_GRANTED action

POST /api/consent/revoke
  Parameters:
    - patientId: Required
    - doctorId: Required
  
  Response: PatientConsent
  Authorization: PATIENT, ADMIN
  Logs: CONSENT_REVOKED action

GET /api/consent
  Parameters:
    - patientId: Required
    - doctorId: Required
  
  Response: PatientConsent (with grantedAt, revokedAt timestamps)

GET /api/consent/check
  Parameters:
    - patientId: Required
    - doctorId: Required
  
  Response: Boolean (true if active consent exists)
```

---

## 6. Enhanced Medical Record Service

### Updated Audit Logging
**File:** `service/MedicalRecordService.java`

All medical record operations now log with:
- `recordId` - The affected record
- `patientId` - The patient who owns the record
- `performedBy` - The actor (user ID)
- `role` - The actor's role
- `actionType` - Type of action
- `details` - Contextual details about the action

**Examples:**
```java
// Upload
"Uploaded file: report.pdf, Category: LAB_TEST"

// View
"Viewed record: report.pdf"

// Update
"Updated file: report_v2.pdf, Version: 2"

// Delete
"Deleted record: report.pdf"
```

**Transactional Consistency:**
- All audit logs are saved within the same transaction as the record operation
- If audit log fails, the entire transaction rolls back
- No orphaned records without audit trails

---

## 7. Data Flow and Immutability

### Upload Workflow
```
1. User uploads medical record
   ↓
2. File is stored in storage service
3. MedicalRecord entity is created
4. Record is saved to database
5. AUDIT_LOG entry is created with:
   - recordId (of newly created record)
   - patientId
   - ACTION: UPLOAD
   - details: filename, category
   ↓
   All operations committed atomically
```

### View Workflow
```
1. User requests to view record/records
2. Authorization checks performed
3. Record(s) are retrieved from database
4. AUDIT_LOG entry is created with:
   - recordId (null for bulk views)
   - patientId
   - ACTION: VIEW
   - details: query parameters
   ↓
   All operations committed atomically
```

### Delete Workflow
```
1. Admin requests to delete record
2. Record status is marked isDeleted = true
3. Record is updated in database (soft delete)
4. AUDIT_LOG entry is created with:
   - recordId
   - patientId
   - ACTION: DELETE
   - details: filename
5. Original record now has isDeleted = true
6. Audit log is immutable and traceable
   ↓
   All operations committed atomically
```

### Consent Grant Workflow
```
1. Patient/Admin requests to grant consent
2. PatientConsent entity is created/updated
3. Consent record is saved
4. AUDIT_LOG entry is created with:
   - recordId: null (consent action, not record-specific)
   - patientId
   - performedBy
   - ACTION: CONSENT_GRANTED
   - details: doctor ID, reason
   ↓
   All operations committed atomically
```

---

## 8. Key Features

### ✅ Immutable Audit Trail
- Timestamps are non-modifiable (`updatable = false`)
- All fields are effectively write-once
- No deletions allowed on audit logs
- Historical records preserved forever

### ✅ Comprehensive Coverage
- All medical record operations tracked
- All consent operations tracked
- Actor identity always recorded
- Role-based context captured

### ✅ Flexible Querying
- Pagination with configurable page size
- Multi-field sorting
- Date range filtering
- Action type filtering
- Combined filters (patient, record, action, date)

### ✅ Authorization
- Patients can view only their own audit logs
- Doctors can view patient logs with active consent
- Admins can view all audit logs
- Role enforcement at service level

### ✅ Transactional Safety
- All operations are @Transactional
- Audit logs created atomically with record changes
- No orphaned records or audit entries
- ACID guarantees maintained

### ✅ No Data Loss on Updates/Deletes
- Updates create new record versions, old versions stay in history
- Deletes use soft delete (isDeleted flag), record remains queryable
- Audit trail shows full version history
- Complete traceability maintained

---

## 9. Database Queries Examples

### Get all audit logs for a patient (with pagination)
```java
GET /api/audit-logs/patient/123?page=0&size=20&sortBy=timestamp&sortDirection=desc

Response:
{
  "data": {
    "content": [
      {
        "id": 1,
        "recordId": 456,
        "patientId": 123,
        "performedBy": 789,
        "role": "DOCTOR",
        "actionType": "VIEW",
        "timestamp": "2026-03-05T14:30:00",
        "details": "Viewed record: lab_report.pdf"
      },
      ...
    ],
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 150,
    "totalPages": 8,
    "isFirst": true,
    "isLast": false
  },
  "message": "Patient audit logs retrieved successfully"
}
```

### Get audit logs for specific record
```java
GET /api/audit-logs/record/456?page=0&size=10

Response:
{
  "data": {
    "content": [
      {
        "id": 5,
        "recordId": 456,
        "patientId": 123,
        "performedBy": 123,
        "role": "PATIENT",
        "actionType": "VIEW",
        "timestamp": "2026-03-05T16:20:00",
        "details": "Viewed record: lab_report.pdf"
      },
      {
        "id": 2,
        "recordId": 456,
        "patientId": 123,
        "performedBy": 123,
        "role": "PATIENT",
        "actionType": "UPLOAD",
        "timestamp": "2026-02-28T10:00:00",
        "details": "Uploaded file: lab_report.pdf, Category: LAB_TEST"
      }
    ],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 2,
    "totalPages": 1,
    "isFirst": true,
    "isLast": true
  }
}
```

### Filter by action type
```java
GET /api/audit-logs/patient/123/action/VIEW?page=0&size=25

Response:
{
  "data": {
    "content": [
      {
        "actionType": "VIEW",
        // ... other fields ...
      }
    ],
    "pageNumber": 0,
    "pageSize": 25,
    "totalElements": 45,
    "totalPages": 2,
    "isFirst": true,
    "isLast": false
  }
}
```

### Date range filter
```java
GET /api/audit-logs/patient/123/date-range?startTime=2026-02-01T00:00:00&endTime=2026-03-05T23:59:59&page=0&size=15

Response:
{
  "data": {
    "content": [
      // Only logs within the date range
    ],
    "pageNumber": 0,
    "pageSize": 15,
    "totalElements": 32,
    "totalPages": 3,
    "isFirst": true,
    "isLast": false
  }
}
```

---

## 10. Compilation Status

✅ All medvault code compiles successfully with no errors
✅ Integration with existing MedicalRecord service
✅ Proper authorization and authentication checks
✅ Full transactional consistency maintained

---

## 11. Next Steps (Optional Enhancements)

1. **Search Functionality** - Full-text search on details field
2. **Export** - Export audit logs as CSV/PDF
3. **Retention Policies** - Archive old logs to separate storage
4. **Real-time Notifications** - Alert on sensitive actions
5. **Analytics** - Dashboard showing audit log statistics
6. **Compliance Reports** - HIPAA/GDPR audit trail reports
