# Medical Records API Documentation

## Overview
Complete medical-record management system with file upload, versioning, soft delete, and comprehensive filtering capabilities.

## Features
✅ Upload medical records (file + notes)  
✅ Categorize records (PRESCRIPTION, TEST_REPORT, DIAGNOSIS, DISCHARGE_SUMMARY, VACCINATION, OTHER)  
✅ Patient record update with versioning  
✅ Soft delete (admin only)  
✅ Filter by category, date range, or both  
✅ Audit logging for all operations  
✅ Role-based access control (PATIENT, DOCTOR, ADMIN)  
✅ Doctor-patient consent validation  

---

## Endpoints

### 1. Upload Medical Record
**POST** `/api/medical-records/upload`

**Parameters:**
- `patientId` (UUID, required) - Patient ID
- `file` (MultipartFile, required) - Medical record file (max 10MB)
- `category` (RecordCategory, required) - Record category
- `notes` (String, optional) - Additional notes

**Category Values:**
- `PRESCRIPTION`
- `TEST_REPORT`
- `DIAGNOSIS`
- `DISCHARGE_SUMMARY`
- `VACCINATION`
- `OTHER`

**Response:**
```json
{
  "success": true,
  "message": "Medical record uploaded successfully",
  "data": {
    "id": "uuid",
    "patientId": "uuid",
    "uploadedBy": "uuid",
    "fileName": "report.pdf",
    "fileType": "application/pdf",
    "category": "TEST_REPORT",
    "fileUrl": "patient-uuid/record-uuid.pdf",
    "notes": "Blood test results",
    "versionNumber": 1,
    "isActive": true,
    "uploadDate": "2026-03-03T10:30:00",
    "lastModifiedDate": "2026-03-03T10:30:00"
  },
  "error": null
}
```

**Roles:** PATIENT, DOCTOR, ADMIN

---

### 2. Get All Patient Records
**GET** `/api/medical-records/patient/{patientId}`

**Response:** List of medical records sorted by upload date (descending)

**Roles:** PATIENT (own records), DOCTOR (with consent), ADMIN

---

### 3. Get Records by Category
**GET** `/api/medical-records/patient/{patientId}/category/{category}`

**Example:** `/api/medical-records/patient/123e4567-e89b-12d3-a456-426614174000/category/PRESCRIPTION`

**Response:** Filtered list sorted by upload date (descending)

**Roles:** PATIENT, DOCTOR (with consent), ADMIN

---

### 4. Get Records by Date Range
**GET** `/api/medical-records/patient/{patientId}/date-range?startDate={start}&endDate={end}`

**Parameters:**
- `startDate` (ISO DateTime, required) - Start date
- `endDate` (ISO DateTime, required) - End date

**Example:** `/api/medical-records/patient/{patientId}/date-range?startDate=2026-01-01T00:00:00&endDate=2026-12-31T23:59:59`

**Response:** Filtered list sorted by upload date (descending)

**Roles:** PATIENT, DOCTOR (with consent), ADMIN

---

### 5. Filter by Category AND Date Range
**GET** `/api/medical-records/patient/{patientId}/filter?category={cat}&startDate={start}&endDate={end}`

**Parameters:**
- `category` (RecordCategory, required)
- `startDate` (ISO DateTime, required)
- `endDate` (ISO DateTime, required)

**Example:** `/api/medical-records/patient/{patientId}/filter?category=PRESCRIPTION&startDate=2026-01-01T00:00:00&endDate=2026-12-31T23:59:59`

**Roles:** PATIENT, DOCTOR (with consent), ADMIN

---

### 6. Get Single Record Details
**GET** `/api/medical-records/{recordId}`

**Response:** Single medical record with full details

**Roles:** PATIENT, DOCTOR (with consent), ADMIN

---

### 7. Update Medical Record (Versioning)
**PUT** `/api/medical-records/{recordId}`

**Parameters:**
- `file` (MultipartFile, optional) - New file version
- `notes` (String, optional) - Updated notes

**Note:** Creates a new version. Old version becomes inactive but remains in database.

**Response:** New version of the record with incremented `versionNumber`

**Roles:** PATIENT, DOCTOR (with consent), ADMIN

---

### 8. Delete Medical Record (Soft Delete)
**DELETE** `/api/medical-records/{recordId}`

**Note:** Only soft delete - sets `isDeleted=true`. Record remains in database.

**Response:**
```json
{
  "success": true,
  "message": "Record deleted successfully",
  "data": null,
  "error": null
}
```

**Roles:** ADMIN only

---

## Error Responses

### 400 Bad Request
```json
{
  "success": false,
  "message": null,
  "data": null,
  "error": "patientId cannot be null"
}
```

### 403 Forbidden
```json
{
  "success": false,
  "message": null,
  "data": null,
  "error": "Patients can only access their own records"
}
```

### 404 Not Found
```json
{
  "success": false,
  "message": null,
  "data": null,
  "error": "Medical record not found with id: {uuid}"
}
```

### 500 Internal Server Error
```json
{
  "success": false,
  "message": null,
  "data": null,
  "error": "An unexpected error occurred: ..."
}
```

---

## Access Control Rules

### PATIENT Role
- ✅ Can upload records for themselves
- ✅ Can view their own records
- ✅ Can update their own records
- ❌ Cannot access other patients' records
- ❌ Cannot delete records

### DOCTOR Role
- ✅ Can upload records for patients (with consent)
- ✅ Can view patient records (with active consent)
- ✅ Can update patient records (with consent)
- ❌ Cannot delete records
- ❌ Blocked if consent is revoked

### ADMIN Role
- ✅ Full access to all operations
- ✅ Can delete records (soft delete)
- ✅ Can access any patient's records

---

## Versioning System

When a record is updated:
1. Old record: `isActive` → `false`
2. New record created with:
   - New UUID
   - Incremented `versionNumber`
   - `isActive` → `true`
   - Same `patientId` and `category`
   - Updated file/notes
3. Old version preserved in database

**Example:**
- Version 1: `versionNumber=1, isActive=true`
- *Update occurs*
- Version 1: `versionNumber=1, isActive=false`
- Version 2: `versionNumber=2, isActive=true`

---

## File Storage

**Location:** `uploads/medical-records/{patientId}/{recordId}.{ext}`

**Max Size:** 10MB per file

**Supported Types:** All file types (PDF, images, documents, etc.)

---

## Audit Logging

All operations are logged with:
- Record ID
- User performing action
- User role
- Action type (VIEW, UPLOAD, UPDATE, DELETE)
- Timestamp

---

## Database Schema

### MedicalRecord Entity
```sql
CREATE TABLE medical_records (
    id BINARY(16) PRIMARY KEY,
    patient_id BINARY(16) NOT NULL,
    uploaded_by BINARY(16) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(255) NOT NULL,
    category VARCHAR(50) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    notes VARCHAR(2000),
    version_number INT NOT NULL,
    is_active BOOLEAN NOT NULL,
    is_deleted BOOLEAN NOT NULL,
    upload_date DATETIME,
    last_modified_date DATETIME,
    INDEX idx_patient_id (patient_id)
);
```

---

## Integration Notes

### Compatibility
✅ Does NOT break existing appointment/prescription flows  
✅ Uses separate `com.medvault` package  
✅ Existing `com.example.demo.model.MedicalRecord` remains intact  
✅ No changes to existing controllers/services  

### Security
- JWT authentication required
- Role-based authorization via Spring Security
- Doctor-patient consent validation
- Audit trail for compliance

### Performance
- Indexed queries on `patientId`
- Date range queries optimized
- Category filtering efficient
- File storage: Local file system (configurable)

---

## Testing Examples

### cURL: Upload Record
```bash
curl -X POST http://localhost:8081/api/medical-records/upload \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "patientId=123e4567-e89b-12d3-a456-426614174000" \
  -F "file=@/path/to/file.pdf" \
  -F "category=TEST_REPORT" \
  -F "notes=Lab results from March 2026"
```

### cURL: Get Patient Records
```bash
curl -X GET http://localhost:8081/api/medical-records/patient/123e4567-e89b-12d3-a456-426614174000 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### cURL: Filter by Category
```bash
curl -X GET "http://localhost:8081/api/medical-records/patient/123e4567-e89b-12d3-a456-426614174000/category/PRESCRIPTION" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### cURL: Update Record
```bash
curl -X PUT http://localhost:8081/api/medical-records/{recordId} \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@/path/to/updated-file.pdf" \
  -F "notes=Updated results"
```

---

## Summary

This implementation provides:
1. ✅ **File Upload** with notes
2. ✅ **6 Categories** (expandable)
3. ✅ **Soft Delete** with `isDeleted` flag
4. ✅ **Versioning** with `versionNumber` and `isActive`
5. ✅ **DTOs** for clean API responses
6. ✅ **Repository Queries** for filtering + sorting
7. ✅ **Role-Based Access Control**
8. ✅ **Error Handling** with custom exceptions
9. ✅ **Audit Logging**
10. ✅ **Non-Breaking** - existing flows untouched

**Package:** `com.medvault`  
**Base URL:** `/api/medical-records`  
**Port:** 8081
