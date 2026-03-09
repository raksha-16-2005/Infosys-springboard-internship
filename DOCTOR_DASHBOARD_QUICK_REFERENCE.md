# Doctor Dashboard - Consent Integration Quick Reference

## What Was Updated

The React Doctor Dashboard has been enhanced with **consent-based access control** for patient medical records.

---

## Key Features

### 1. **Consent Status Display**
- Each patient in the patient list shows their consent status
- **Green checkmark (✓ Active)**: Doctor has consent to view records
- **Red lock (🔒 No Consent)**: Patient hasn't granted access

### 2. **Safe Record Access**
- Records only load when patient has active consent
- If access is denied, a clear message explains why:
  ```
  ⚠️ Access Denied
  This patient has not granted you consent to view their 
  medical records yet. They can grant access from their 
  account settings.
  ```

### 3. **Medical Records Categories**
Records are organized by type with color-coded badges:
- **PRESCRIPTION** - Prescribed medications
- **TEST_REPORT** - Lab and diagnostic test results
- **DIAGNOSIS** - Doctor's diagnosis notes
- **DISCHARGE_SUMMARY** - Hospital discharge information
- **VACCINATION** - Vaccination records
- **OTHER** - Miscellaneous records

### 4. **Download Files**
- Each record has a **Download** button
- Click to save patient's medical records locally
- Works for any file type attached to records

### 5. **Add Medical Records**
Doctors can add new records when consent exists:
1. Select **Record Category** from dropdown
2. Add optional **Notes** (observations, findings, etc.)
3. Attach a **File** (PDFs, images, etc.)
4. Click **Upload Record**

---

## Navigation

### Patients Tab Features
1. **Patient List** (Left column)
   - Shows all patients
   - Displays consent status next to each name
   - Click **View** to see patient details

2. **Patient Details** (Right column)
   - Patient information
   - Consent status badge
   - Medical records (if consent exists)
   - Upload new records (if consent exists)
   - Add prescriptions (if consent exists)

---

## Consent Status Indicators

| Status | Icon | Color | Meaning |
|--------|------|-------|---------|
| **Active** | ✓ | Green | Can view & manage records |
| **No Consent** | 🔒 | Red | Cannot access records |
| **Denied Access** | ⚠️ | Orange-Red | Attempted access without consent |

---

## Workflow Example

### Scenario: Dr. needs to add a test report for Patient John Doe

1. Go to **Patients** tab
2. See John Doe with **✓ Active** consent status
3. Click **View** next to John Doe's name
4. Patient details load with existing medical records
5. Scroll to "Add Medical Record" section
6. Select **TEST_REPORT** from Category dropdown
7. Type notes: "Blood test results - all normal"
8. Click file picker and select test_results.pdf
9. Click **Upload Record**
10. Record appears in the Medical Records list above
11. Download button available for the record

### Scenario: Dr. tries to view Patient Jane Smith's records (no consent)

1. Go to **Patients** tab
2. See Jane Smith with **🔒 No Consent** status
3. Click **View** next to Jane Smith's name
4. Patient details load, but shows:
   ```
   ⚠️ Access Denied
   This patient has not granted you consent...
   ```
5. **Add Medical Record** section is hidden
6. **Add Prescription** section is hidden
7. Doctor cannot add records without patient's consent

---

## Technical Details

### State Variables
```javascript
patientConsents[userId]     // Tracks consent status per patient
recordCategory              // Currently selected record category
accessDeniedPatient         // Tracks which patient denied access
```

### Functions
- `fetchPatientConsent()` - Gets consent status for a patient
- `fetchPatientRecords()` - Fetches records with 403 error handling
- `downloadRecord()` - Downloads a patient's medical record file
- `addMedicalRecord()` - Uploads new record with validation

### Error Handling
- **No Consent**: Shows friendly alert instead of error
- **File Missing**: Prevents upload, shows message "Please select a file"
- **Network Error**: Displays error message
- **Upload Success**: Shows "Medical record uploaded successfully!"

---

## API Endpoints Used

| Method | Endpoint | Purpose |
|--------|----------|---------|
| `GET` | `/api/doctor/patient/{id}/consent` | Check patient consent status |
| `GET` | `/api/doctor/patients/{id}/records` | Fetch patient medical records |
| `POST` | `/api/medical-records/upload` | Upload new medical record |
| `GET` | `/api/medical-records/{id}/download` | Download record file |

---

## Tips & Best Practices

✅ **Do:**
- Always check patient consent status before attempting to view records
- Use appropriate record categories for organization
- Add descriptive notes with uploaded records
- Use the download feature to keep local backups if needed

❌ **Don't:**
- Try to force access to patient records without consent
- Upload records without proper categorization
- Leave notes blank - always document what you're uploading

---

## Testing the Feature

### Quick Test Steps:
1. Navigate to **Patients** section
2. Check that consent status shows for all patients
3. Click **View** on a patient with active consent
4. Verify records load and download buttons appear
5. Click **Download** on a record
6. Try uploading a new record with category selection
7. Click **View** on a patient with no consent
8. Verify "Access Denied" message appears
9. Confirm upload form is hidden for no-consent patients

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Records not loading | Check if patient has granted consent |
| Upload button disabled | Ensure file is selected and consent exists |
| Download doesn't work | Check file format and browser permissions |
| Consent status not updating | Refresh page or re-select patient |
| No consent message stays | Patient needs to grant consent in their portal |

---

## Support

For issues or questions about the consent-based access feature:
1. Check that backend consent APIs are implemented
2. Verify patient has granted consent
3. Check browser console for error messages
4. Review API response status codes
5. Ensure JWT token is valid and has DOCTOR role

