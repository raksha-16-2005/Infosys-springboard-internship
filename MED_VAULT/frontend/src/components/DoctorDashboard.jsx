import React, { useEffect, useState } from 'react';
import axios from 'axios';

export default function DoctorDashboard() {
  const [activeTab, setActiveTab] = useState('profile');
  const [profile, setProfile] = useState(null);
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');

  const [formData, setFormData] = useState({
    fullName: '',
    specialization: '',
    qualification: '',
    experienceYears: '',
    hospitalName: '',
    phone: '',
    bio: ''
  });

  const [prescriptionForms, setPrescriptionForms] = useState({});

  const token = localStorage.getItem('token');

  useEffect(() => {
    fetchProfile();
    fetchAppointments();
  }, []);

  const fetchProfile = async () => {
    try {
      const res = await axios.get('/api/doctor/profile', {
        headers: { Authorization: `Bearer ${token}` }
      });
      setProfile(res.data);
      if (res.data) {
        setFormData({
          fullName: res.data.fullName || '',
          specialization: res.data.specialization || '',
          qualification: res.data.qualification || '',
          experienceYears: res.data.experienceYears || '',
          hospitalName: res.data.hospitalName || '',
          phone: res.data.phone || '',
          bio: res.data.bio || ''
        });
      }
    } catch (e) {
      console.log('Profile fetch error');
    }
  };

  const fetchAppointments = async () => {
    try {
      const res = await axios.get('/api/doctor/appointments', {
        headers: { Authorization: `Bearer ${token}` }
      });
      setAppointments(res.data);
      const forms = {};
      res.data.forEach(apt => {
        forms[apt.id] = { medicines: '', dosageInstructions: '' };
      });
      setPrescriptionForms(forms);
    } catch (e) {
      console.log('Appointments fetch error');
    }
  };

  const saveProfile = async () => {
    setLoading(true);
    setMessage('');
    try {
      await axios.put('/api/doctor/profile', formData, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setMessage('Profile saved successfully!');
      setTimeout(() => setMessage(''), 3000);
      fetchProfile();
    } catch (err) {
      setMessage('Error saving profile');
    } finally {
      setLoading(false);
    }
  };

  const updateAppointmentStatus = async (appointmentId, newStatus) => {
    setLoading(true);
    setMessage('');
    try {
      await axios.put(`/api/doctor/appointments/${appointmentId}/status`, { status: newStatus }, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setMessage('Status updated successfully!');
      setTimeout(() => setMessage(''), 3000);
      fetchAppointments();
    } catch (err) {
      setMessage('Error updating status');
    } finally {
      setLoading(false);
    }
  };

  const addPrescription = async (appointmentId) => {
    const form = prescriptionForms[appointmentId];
    if (!form.medicines || !form.dosageInstructions) {
      setMessage('Please fill all prescription fields');
      return;
    }
    setLoading(true);
    setMessage('');
    try {
      await axios.post(`/api/doctor/appointments/${appointmentId}/prescriptions`, form, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setMessage('Prescription added successfully!');
      setTimeout(() => setMessage(''), 3000);
      setPrescriptionForms(prev => ({ ...prev, [appointmentId]: { medicines: '', dosageInstructions: '' } }));
      fetchAppointments();
    } catch (err) {
      setMessage('Error adding prescription');
    } finally {
      setLoading(false);
    }
  };

  const handleProfileChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handlePrescriptionChange = (appointmentId, e) => {
    const { name, value } = e.target;
    setPrescriptionForms(prev => ({
      ...prev,
      [appointmentId]: { ...prev[appointmentId], [name]: value }
    }));
  };

  return (
    <div style={{ display: 'flex', minHeight: '100vh', backgroundColor: '#f5f7fa' }}>
      <aside style={{ width: 220, padding: '20px', borderRight: '1px solid #ddd', backgroundColor: 'white' }}>
        <h3 style={{ marginTop: 0 }}>Doctor Dashboard</h3>
        <nav style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
          <button
            onClick={() => setActiveTab('profile')}
            className={activeTab === 'profile' ? 'btn btn-primary' : 'btn btn-outline'}
            style={{ textAlign: 'left' }}
          >
            Profile
          </button>
          <button
            onClick={() => setActiveTab('appointments')}
            className={activeTab === 'appointments' ? 'btn btn-primary' : 'btn btn-outline'}
            style={{ textAlign: 'left' }}
          >
            Appointments
          </button>
        </nav>
      </aside>

      <main style={{ flex: 1, padding: '30px' }}>
        {message && (
          <div style={{ padding: '10px 15px', marginBottom: '20px', backgroundColor: message.includes('Error') ? '#fee' : '#efe', border: `1px solid ${message.includes('Error') ? '#f99' : '#9f9'}`, borderRadius: '4px', color: message.includes('Error') ? '#c33' : '#3c3' }}>
            {message}
          </div>
        )}

        {activeTab === 'profile' && (
          <div>
            <h2>Doctor Profile</h2>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '15px', maxWidth: '600px' }}>
              <div>
                <label>Full Name</label>
                <input type="text" name="fullName" value={formData.fullName} onChange={handleProfileChange} placeholder="Full Name" />
              </div>
              <div>
                <label>Specialization</label>
                <input type="text" name="specialization" value={formData.specialization} onChange={handleProfileChange} placeholder="Specialization" />
              </div>
              <div>
                <label>Qualification</label>
                <input type="text" name="qualification" value={formData.qualification} onChange={handleProfileChange} placeholder="Qualification (e.g., MD, MBBS)" />
              </div>
              <div>
                <label>Experience (years)</label>
                <input type="number" name="experienceYears" value={formData.experienceYears} onChange={handleProfileChange} placeholder="Years of Experience" />
              </div>
              <div>
                <label>Hospital Name</label>
                <input type="text" name="hospitalName" value={formData.hospitalName} onChange={handleProfileChange} placeholder="Hospital Name" />
              </div>
              <div>
                <label>Phone</label>
                <input type="tel" name="phone" value={formData.phone} onChange={handleProfileChange} placeholder="Phone" />
              </div>
              <div style={{ gridColumn: '1 / -1' }}>
                <label>Bio</label>
                <textarea name="bio" value={formData.bio} onChange={handleProfileChange} placeholder="Professional bio..." rows="4" style={{ width: '100%' }}></textarea>
              </div>
            </div>
            <button className="btn btn-primary" onClick={saveProfile} disabled={loading} style={{ marginTop: '20px' }}>
              {loading ? 'Saving...' : 'Save Profile'}
            </button>
          </div>
        )}

        {activeTab === 'appointments' && (
          <div>
            <h2>Appointments</h2>
            {appointments.length === 0 ? (
              <p style={{ color: '#666' }}>No appointments yet</p>
            ) : (
              <div style={{ display: 'grid', gap: '20px' }}>
                {appointments.map(apt => (
                  <div key={apt.id} style={{ padding: '20px', border: '1px solid #ddd', borderRadius: '6px', backgroundColor: 'white' }}>
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' }}>
                      <div>
                        <h4 style={{ marginTop: 0 }}>Patient: {apt.patient?.fullName}</h4>
                        <p><strong>Age:</strong> {apt.patient?.age}</p>
                        <p><strong>Phone:</strong> {apt.patient?.phone}</p>
                        <p><strong>Date & Time:</strong> {new Date(apt.appointmentDate).toLocaleString()}</p>
                        <p><strong>Status:</strong> 
                          <select value={apt.status} onChange={(e) => updateAppointmentStatus(apt.id, e.target.value)} style={{ marginLeft: '10px' }} disabled={loading}>
                            <option value="PENDING">PENDING</option>
                            <option value="APPROVED">APPROVED</option>
                            <option value="COMPLETED">COMPLETED</option>
                            <option value="CANCELLED">CANCELLED</option>
                          </select>
                        </p>
                        {apt.notes && <p><strong>Patient Notes:</strong> {apt.notes}</p>}
                      </div>
                      <div>
                        <h4 style={{ marginTop: 0 }}>Add Prescription</h4>
                        <div style={{ marginBottom: '10px' }}>
                          <label>Medicines</label>
                          <textarea
                            name="medicines"
                            value={prescriptionForms[apt.id]?.medicines || ''}
                            onChange={(e) => handlePrescriptionChange(apt.id, e)}
                            placeholder="List of medicines..."
                            rows="2"
                            style={{ width: '100%' }}
                          ></textarea>
                        </div>
                        <div style={{ marginBottom: '10px' }}>
                          <label>Dosage Instructions</label>
                          <textarea
                            name="dosageInstructions"
                            value={prescriptionForms[apt.id]?.dosageInstructions || ''}
                            onChange={(e) => handlePrescriptionChange(apt.id, e)}
                            placeholder="Dosage and instructions..."
                            rows="2"
                            style={{ width: '100%' }}
                          ></textarea>
                        </div>
                        <button className="btn btn-primary" onClick={() => addPrescription(apt.id)} disabled={loading}>
                          {loading ? 'Adding...' : 'Add Prescription'}
                        </button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}
      </main>
    </div>
  );
}
