import React, { useEffect, useState } from 'react';
import axios from 'axios';

export default function PatientDashboard() {
  const [activeTab, setActiveTab] = useState('profile');
  const [profile, setProfile] = useState(null);
  const [appointments, setAppointments] = useState([]);
  const [prescriptions, setPrescriptions] = useState([]);
  const [doctors, setDoctors] = useState([]);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');

  const [formData, setFormData] = useState({
    fullName: '',
    age: '',
    gender: '',
    bloodGroup: '',
    phone: '',
    address: '',
    emergencyContact: '',
    medicalHistory: ''
  });

  const [appointmentForm, setAppointmentForm] = useState({
    doctorId: '',
    appointmentDate: '',
    notes: ''
  });

  const token = localStorage.getItem('token');

  useEffect(() => {
    fetchProfile();
    fetchAppointments();
    fetchPrescriptions();
    fetchDoctors();
  }, []);

  const fetchProfile = async () => {
    try {
      const res = await axios.get('/api/patient/profile', {
        headers: { Authorization: `Bearer ${token}` }
      });
      setProfile(res.data);
      if (res.data) {
        setFormData({
          fullName: res.data.fullName || '',
          age: res.data.age || '',
          gender: res.data.gender || '',
          bloodGroup: res.data.bloodGroup || '',
          phone: res.data.phone || '',
          address: res.data.address || '',
          emergencyContact: res.data.emergencyContact || '',
          medicalHistory: res.data.medicalHistory || ''
        });
      }
    } catch (e) {
      console.log('Profile fetch error');
    }
  };

  const fetchAppointments = async () => {
    try {
      const res = await axios.get('/api/patient/appointments', {
        headers: { Authorization: `Bearer ${token}` }
      });
      setAppointments(res.data);
    } catch (e) {
      console.log('Appointments fetch error');
    }
  };

  const fetchPrescriptions = async () => {
    try {
      const res = await axios.get('/api/patient/prescriptions', {
        headers: { Authorization: `Bearer ${token}` }
      });
      setPrescriptions(res.data);
    } catch (e) {
      console.log('Prescriptions fetch error');
    }
  };

  const fetchDoctors = async () => {
    try {
      const res = await axios.get('/api/doctors');
      setDoctors(res.data);
    } catch (e) {
      console.log('Doctors fetch error');
    }
  };

  const saveProfile = async () => {
    setLoading(true);
    setMessage('');
    try {
      await axios.put('/api/patient/profile', formData, {
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

  const bookAppointment = async () => {
    if (!appointmentForm.doctorId || !appointmentForm.appointmentDate) {
      setMessage('Please fill all required fields');
      return;
    }
    setLoading(true);
    setMessage('');
    try {
      await axios.post('/api/patient/appointments', appointmentForm, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setMessage('Appointment booked successfully!');
      setAppointmentForm({ doctorId: '', appointmentDate: '', notes: '' });
      setTimeout(() => setMessage(''), 3000);
      fetchAppointments();
    } catch (err) {
      setMessage('Error booking appointment');
    } finally {
      setLoading(false);
    }
  };

  const handleProfileChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleAppointmentChange = (e) => {
    const { name, value } = e.target;
    setAppointmentForm(prev => ({ ...prev, [name]: value }));
  };

  return (
    <div style={{ display: 'flex', minHeight: '100vh', backgroundColor: '#f5f7fa' }}>
      <aside style={{ width: 220, padding: '20px', borderRight: '1px solid #ddd', backgroundColor: 'white' }}>
        <h3 style={{ marginTop: 0 }}>Patient Dashboard</h3>
        <nav style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
          <button
            onClick={() => setActiveTab('profile')}
            className={activeTab === 'profile' ? 'btn btn-primary' : 'btn btn-outline'}
            style={{ textAlign: 'left' }}
          >
            Profile
          </button>
          <button
            onClick={() => setActiveTab('book')}
            className={activeTab === 'book' ? 'btn btn-primary' : 'btn btn-outline'}
            style={{ textAlign: 'left' }}
          >
            Book Appointment
          </button>
          <button
            onClick={() => setActiveTab('appointments')}
            className={activeTab === 'appointments' ? 'btn btn-primary' : 'btn btn-outline'}
            style={{ textAlign: 'left' }}
          >
            My Appointments
          </button>
          <button
            onClick={() => setActiveTab('prescriptions')}
            className={activeTab === 'prescriptions' ? 'btn btn-primary' : 'btn btn-outline'}
            style={{ textAlign: 'left' }}
          >
            Prescriptions
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
            <h2>Patient Profile</h2>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '15px', maxWidth: '600px' }}>
              <div>
                <label>Full Name</label>
                <input type="text" name="fullName" value={formData.fullName} onChange={handleProfileChange} placeholder="Full Name" />
              </div>
              <div>
                <label>Age</label>
                <input type="number" name="age" value={formData.age} onChange={handleProfileChange} placeholder="Age" />
              </div>
              <div>
                <label>Gender</label>
                <select name="gender" value={formData.gender} onChange={handleProfileChange}>
                  <option value="">Select Gender</option>
                  <option value="Male">Male</option>
                  <option value="Female">Female</option>
                  <option value="Other">Other</option>
                </select>
              </div>
              <div>
                <label>Blood Group</label>
                <select name="bloodGroup" value={formData.bloodGroup} onChange={handleProfileChange}>
                  <option value="">Select Blood Group</option>
                  <option value="O+">O+</option>
                  <option value="O-">O-</option>
                  <option value="A+">A+</option>
                  <option value="A-">A-</option>
                  <option value="B+">B+</option>
                  <option value="B-">B-</option>
                  <option value="AB+">AB+</option>
                  <option value="AB-">AB-</option>
                </select>
              </div>
              <div>
                <label>Phone</label>
                <input type="tel" name="phone" value={formData.phone} onChange={handleProfileChange} placeholder="Phone" />
              </div>
              <div>
                <label>Address</label>
                <input type="text" name="address" value={formData.address} onChange={handleProfileChange} placeholder="Address" />
              </div>
              <div style={{ gridColumn: '1 / -1' }}>
                <label>Emergency Contact</label>
                <input type="tel" name="emergencyContact" value={formData.emergencyContact} onChange={handleProfileChange} placeholder="Emergency Contact" />
              </div>
              <div style={{ gridColumn: '1 / -1' }}>
                <label>Medical History</label>
                <textarea name="medicalHistory" value={formData.medicalHistory} onChange={handleProfileChange} placeholder="Any relevant medical history" rows="4" style={{ width: '100%' }}></textarea>
              </div>
            </div>
            <button className="btn btn-primary" onClick={saveProfile} disabled={loading} style={{ marginTop: '20px' }}>
              {loading ? 'Saving...' : 'Save Profile'}
            </button>
          </div>
        )}

        {activeTab === 'book' && (
          <div>
            <h2>Book Appointment</h2>
            <div style={{ maxWidth: '500px' }}>
              <div style={{ marginBottom: '15px' }}>
                <label>Select Doctor</label>
                <select name="doctorId" value={appointmentForm.doctorId} onChange={handleAppointmentChange}>
                  <option value="">Choose a doctor</option>
                  {doctors.map(doc => (
                    <option key={doc.id} value={doc.id}>
                      {doc.fullName} - {doc.specialization}
                    </option>
                  ))}
                </select>
              </div>
              <div style={{ marginBottom: '15px' }}>
                <label>Appointment Date & Time</label>
                <input type="datetime-local" name="appointmentDate" value={appointmentForm.appointmentDate} onChange={handleAppointmentChange} />
              </div>
              <div style={{ marginBottom: '15px' }}>
                <label>Notes (optional)</label>
                <textarea name="notes" value={appointmentForm.notes} onChange={handleAppointmentChange} placeholder="Any notes about your condition..." rows="3" style={{ width: '100%' }}></textarea>
              </div>
              <button className="btn btn-primary" onClick={bookAppointment} disabled={loading}>
                {loading ? 'Booking...' : 'Book Appointment'}
              </button>
            </div>
          </div>
        )}

        {activeTab === 'appointments' && (
          <div>
            <h2>My Appointments</h2>
            {appointments.length === 0 ? (
              <p style={{ color: '#666' }}>No appointments yet</p>
            ) : (
              <div style={{ display: 'grid', gap: '15px' }}>
                {appointments.map(apt => (
                  <div key={apt.id} style={{ padding: '15px', border: '1px solid #ddd', borderRadius: '6px', backgroundColor: 'white' }}>
                    <h4 style={{ marginTop: 0 }}>Dr. {apt.doctor?.fullName}</h4>
                    <p><strong>Specialization:</strong> {apt.doctor?.specialization}</p>
                    <p><strong>Date & Time:</strong> {new Date(apt.appointmentDate).toLocaleString()}</p>
                    <p><strong>Status:</strong> <span style={{ padding: '4px 8px', borderRadius: '4px', backgroundColor: apt.status === 'APPROVED' ? '#e8f5e9' : apt.status === 'PENDING' ? '#fff3e0' : '#e0e0e0', color: apt.status === 'APPROVED' ? '#2e7d32' : apt.status === 'PENDING' ? '#e65100' : '#424242' }}>{apt.status}</span></p>
                    {apt.notes && <p><strong>Notes:</strong> {apt.notes}</p>}
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {activeTab === 'prescriptions' && (
          <div>
            <h2>Prescriptions</h2>
            {prescriptions.length === 0 ? (
              <p style={{ color: '#666' }}>No prescriptions yet</p>
            ) : (
              <div style={{ display: 'grid', gap: '15px' }}>
                {prescriptions.map(presc => (
                  <div key={presc.id} style={{ padding: '15px', border: '1px solid #ddd', borderRadius: '6px', backgroundColor: 'white' }}>
                    <h4 style={{ marginTop: 0 }}>From: Dr. {presc.appointment?.doctor?.fullName}</h4>
                    <p><strong>Medicines:</strong> {presc.medicines}</p>
                    <p><strong>Dosage:</strong> {presc.dosageInstructions}</p>
                    <p><strong>Date:</strong> {new Date(presc.createdAt).toLocaleDateString()}</p>
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
