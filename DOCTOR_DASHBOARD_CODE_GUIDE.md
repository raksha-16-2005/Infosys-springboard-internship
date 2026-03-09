# Doctor Dashboard - Code Implementation Guide

## Overview of Changes

Updated `DoctorDashboardNew.jsx` component with consent-based access control and enhanced medical records management.

---

## 1. New Imports Added

```javascript
// Icon additions to support consent UI
import {
  FiDownload,      // For download buttons
  FiAlertCircle,   // For access denied warnings
  FiLock           // For consent status indicators
} from 'react-icons/fi';
```

---

## 2. State Variables Added

```javascript
// Track consent status for each patient
const [patientConsents, setPatientConsents] = useState({});
// e.g., { userId: { granted: true, grantedAt: "...", ... } }

// Record category for new uploads
const [recordCategory, setRecordCategory] = useState('DIAGNOSIS');

// Track which patient denied access
const [accessDeniedPatient, setAccessDeniedPatient] = useState(null);
```

---

## 3. New Functions

### A. Fetch Patient Consent
```javascript
const fetchPatientConsent = async (patientUserId) => {
  try {
    const res = await axios.get(
      `/api/doctor/patient/${patientUserId}/consent`, 
      { headers: authHeader }
    );
    setPatientConsents((prev) => ({
      ...prev,
      [patientUserId]: res.data
    }));
    return res.data;
  } catch (err) {
    // Graceful fallback if endpoint not available
    setPatientConsents((prev) => ({
      ...prev,
      [patientUserId]: { granted: false, reason: 'No consent' }
    }));
    return null;
  }
};
```

### B. Download Medical Record
```javascript
const downloadRecord = async (record) => {
  try {
    const response = await axios.get(
      `/api/medical-records/${record.id}/download`,
      { 
        headers: authHeader,
        responseType: 'blob'  // Important: get file as binary
      }
    );
    
    // Create download link and trigger download
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', record.fileName || 'record.pdf');
    document.body.appendChild(link);
    link.click();
    link.parentNode.removeChild(link);
    
  } catch (err) {
    setMessage('Failed to download record');
    console.error('Download error', err);
  }
};
```

### C. Updated Add Medical Record with Consent Check
```javascript
const addMedicalRecord = async () => {
  if (!selectedPatient) return;
  
  // CHECK CONSENT BEFORE UPLOAD
  const consent = patientConsents[selectedPatient.userId];
  if (!consent?.granted) {
    setMessage('Cannot add records: Patient has not granted consent');
    return;
  }

  // VALIDATE FILE
  if (!recordFile) {
    setMessage('Please select a file to upload');
    return;
  }

  // PREPARE DATA WITH CATEGORY
  const data = new FormData();
  data.append('category', recordCategory);  // NEW: Add category
  if (recordNotes) data.append('notes', recordNotes);
  data.append('file', recordFile);
  
  setLoading(true);
  try {
    // Use new medical records endpoint
    const res = await axios.post(
      `/api/medical-records/upload?patientId=${selectedPatient.userId}`,
      data,
      { headers: { ...authHeader, 'Content-Type': 'multipart/form-data' } }
    );
    
    setPatientRecords((prev) => [res.data.data, ...prev]);
    setRecordNotes('');
    setRecordFile(null);
    setRecordCategory('DIAGNOSIS');
    setMessage('Medical record uploaded successfully!');
  } catch (err) {
    const errorMsg = err.response?.data?.message || 'Failed to upload medical record';
    setMessage(errorMsg);
  } finally {
    setLoading(false);
  }
};
```

---

## 4. Updated useEffect Hook

```javascript
useEffect(() => {
  if (selectedPatient?.userId) {
    // Fetch both records AND consent status
    fetchPatientRecords(selectedPatient.userId);
    fetchPatientConsent(selectedPatient.userId);  // NEW
    setRecordNotes('');
    setRecordFile(null);
    setRecordCategory('DIAGNOSIS');  // NEW
    setSelectedPatientAppointmentId('');
  }
}, [selectedPatient]);
```

---

## 5. Updated Record Fetching with Error Handling

```javascript
const fetchPatientRecords = async (patientUserId) => {
  setAccessDeniedPatient(null);
  try {
    const res = await axios.get(
      `/api/doctor/patients/${patientUserId}/records`, 
      { headers: authHeader }
    );
    setPatientRecords(res.data || []);
  } catch (err) {
    // GRACEFUL ERROR HANDLING
    if (err.response?.status === 403 && 
        err.response?.data?.message?.includes('consent')) {
      // Handle consent denial specifically
      setAccessDeniedPatient(patientUserId);
      setPatientRecords([]);
      setMessage('No active consent from this patient yet');
    } else {
      console.error('Record fetch error', err);
      setPatientRecords([]);
    }
  }
};
```

---

## 6. Patient List with Consent Status Badge

```jsx
{patients.map((patient) => {
  const consent = patientConsents[patient.userId];
  const hasConsent = consent?.granted;
  
  return (
    <tr key={patient.userId}>
      <td>{patient.fullName}</td>
      <td>{patient.age || '-'}</td>
      
      {/* NEW: Consent Status Column */}
      <td>
        {hasConsent ? (
          <span style={{ color: '#10b981', fontWeight: 'bold', 
                         display: 'flex', alignItems: 'center', gap: '4px' }}>
            <FiCheckCircle size={14} /> Active
          </span>
        ) : (
          <span style={{ color: '#ef4444', fontWeight: 'bold', 
                         display: 'flex', alignItems: 'center', gap: '4px' }}>
            <FiLock size={14} /> No Consent
          </span>
        )}
      </td>
      
      <td>
        <button className="ghost" onClick={() => setSelectedPatient(patient)}>
          View
        </button>
      </td>
    </tr>
  );
})}
```

---

## 7. Medical Records Display with Categories

```jsx
<div style={{ marginTop: '16px' }}>
  <h4 style={{ marginBottom: '12px', display: 'flex', alignItems: 'center', gap: '8px' }}>
    <FiFileText size={16} /> Medical Records
  </h4>
  
  {/* HANDLE ACCESS DENIED */}
  {accessDeniedPatient === selectedPatient.userId ? (
    <div style={{ 
      backgroundColor: '#fef2f2', 
      border: '1px solid #fecaca',
      borderRadius: '6px',
      padding: '12px',
      display: 'flex',
      alignItems: 'flex-start',
      gap: '8px'
    }}>
      <FiAlertCircle size={18} style={{ color: '#dc2626', marginTop: '2px' }} />
      <div>
        <p style={{ fontWeight: '500', color: '#991b1b', margin: '0 0 4px 0' }}>
          Access Denied
        </p>
        <p style={{ fontSize: '0.9em', color: '#7f1d1d', margin: '0' }}>
          This patient has not granted you consent to view their medical records yet.
          They can grant access from their account settings.
        </p>
      </div>
    </div>
  ) : patientRecords.length === 0 ? (
    <p className="muted">No records available yet.</p>
  ) : (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
      {patientRecords.map((record) => (
        <div key={record.id} style={{
          backgroundColor: '#f9fafb',
          border: '1px solid #e5e7eb',
          borderRadius: '6px',
          padding: '10px'
        }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', 
                       alignItems: 'start', marginBottom: '6px' }}>
            <div>
              <strong>{record.fileName || 'Record'}</strong>
              
              {/* CATEGORY BADGE */}
              {record.category && (
                <span style={{
                  display: 'inline-block',
                  marginLeft: '8px',
                  backgroundColor: '#dbeafe',
                  color: '#075985',
                  padding: '2px 6px',
                  borderRadius: '3px',
                  fontSize: '0.8em',
                  fontWeight: '500'
                }}>
                  {record.category}
                </span>
              )}
            </div>
            
            {/* DOWNLOAD BUTTON */}
            <button 
              onClick={() => downloadRecord(record)}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '4px',
                padding: '4px 8px',
                backgroundColor: '#f0f9ff',
                border: '1px solid #bfdbfe',
                color: '#0369a1',
                borderRadius: '4px',
                cursor: 'pointer'
              }}
            >
              <FiDownload size={12} /> Download
            </button>
          </div>
          
          {record.notes && (
            <p style={{ color: '#6b7280', margin: '6px 0 0 0' }}>
              {record.notes}
            </p>
          )}
          
          <p style={{ fontSize: '0.8em', color: '#9ca3af', margin: '4px 0 0 0' }}>
            Uploaded: {record.uploadDate ? 
              new Date(record.uploadDate).toLocaleDateString() : '-'}
          </p>
        </div>
      ))}
    </div>
  )}
</div>
```

---

## 8. Record Upload with Category Selection (Consent Gated)

```jsx
{/* Only render if consent is active */}
{patientConsents[selectedPatient.userId]?.granted && !accessDeniedPatient && (
  <div style={{ marginTop: '16px' }}>
    <h4 style={{ marginBottom: '12px' }}>Add Medical Record</h4>
    
    {/* CATEGORY DROPDOWN */}
    <label style={{ display: 'block', marginBottom: '8px' }}>
      <span style={{ fontWeight: '500', fontSize: '0.9em' }}>Record Category</span>
      <select
        value={recordCategory}
        onChange={(e) => setRecordCategory(e.target.value)}
        style={{ marginTop: '4px', padding: '6px', borderRadius: '4px' }}
      >
        <option value="PRESCRIPTION">Prescription</option>
        <option value="TEST_REPORT">Test Report</option>
        <option value="DIAGNOSIS">Diagnosis</option>
        <option value="DISCHARGE_SUMMARY">Discharge Summary</option>
        <option value="VACCINATION">Vaccination</option>
        <option value="OTHER">Other</option>
      </select>
    </label>
    
    {/* NOTES */}
    <label style={{ display: 'block', marginBottom: '8px' }}>
      <span style={{ fontWeight: '500', fontSize: '0.9em' }}>Notes (Optional)</span>
      <textarea
        value={recordNotes}
        onChange={(e) => setRecordNotes(e.target.value)}
        placeholder="Add diagnosis, notes, or observations"
        style={{ marginTop: '4px', minHeight: '60px' }}
      />
    </label>
    
    {/* FILE INPUT */}
    <label style={{ display: 'block', marginBottom: '8px' }}>
      <span style={{ fontWeight: '500', fontSize: '0.9em' }}>Select File</span>
      <input 
        type="file" 
        onChange={(e) => setRecordFile(e.target.files?.[0])}
        style={{ marginTop: '4px' }}
      />
    </label>
    
    {/* UPLOAD BUTTON - Disabled without file */}
    <button 
      className="primary" 
      onClick={addMedicalRecord}
      disabled={loading || !recordFile}
      style={{ opacity: loading || !recordFile ? 0.6 : 1 }}
    >
      {loading ? 'Uploading...' : 'Upload Record'}
    </button>
  </div>
)}
```

---

## Key Implementation Principles

1. **Consent-First Design**: Always check consent before showing upload/edit options
2. **Graceful Degradation**: If consent endpoint unavailable, assume no consent
3. **User-Friendly Errors**: Show helpful messages instead of technical errors
4. **Clear Visual Indicators**: Use colors and icons to quickly show consent status
5. **Preserved Structure**: No changes to existing dashboard sections
6. **Blocking Actions**: Prevent record uploads without consent or files
7. **Binary File Handling**: Use `responseType: 'blob'` for file downloads

---

## Testing Scenarios

### Scenario 1: Patient with Consent
- Records load ✓
- Download buttons visible ✓
- Upload form shown ✓
- Prescription section enabled ✓

### Scenario 2: Patient without Consent
- Access denied message shown ✓
- Records section empty ✓
- Upload form hidden ✓
- Prescription section disabled ✓

### Scenario 3: File Operations
- Download creates file ✓
- Upload validates file selection ✓
- Category properly sent to backend ✓
- Success message displays ✓

