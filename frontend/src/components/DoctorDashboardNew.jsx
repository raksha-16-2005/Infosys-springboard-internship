import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { motion } from 'framer-motion';
import { FiUsers, FiClock, FiCheckSquare, FiFileText, FiEdit2 } from 'react-icons/fi';
import PrescriptionView from './PrescriptionView';
import '../styles/dashboard.css';

export default function DoctorDashboard() {
  const [activeTab, setActiveTab] = useState('overview');
  const [profile, setProfile] = useState(null);
  const [appointments, setAppointments] = useState([]);
  const [todayAppointments, setTodayAppointments] = useState([]);
  const [completedAppointments, setCompletedAppointments] = useState([]);
  const [selectedAppointment, setSelectedAppointment] = useState(null);
  const [selectedPrescription, setSelectedPrescription] = useState(null);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [showPrescriptionForm, setShowPrescriptionForm] = useState(false);

  const [formData, setFormData] = useState({
    fullName: '',
    specialization: '',
    qualification: '',
    experienceYears: '',
    hospitalName: '',
    phone: '',
    bio: ''
  });

  const [prescriptionForm, setPrescriptionForm] = useState({
    diagnosis: '',
    medicinesJson: '[]',
    testsRecommended: '',
    followUpDate: '',
    notes: ''
  });

  const token = localStorage.getItem('token');
  const storedUser = (() => { try { return JSON.parse(localStorage.getItem('user')||'null'); } catch (e) { return null; } })();
  const userRole = storedUser?.role || '';

  useEffect(() => {
    fetchProfile();
    fetchAppointments();
    fetchTodayAppointments();
  }, []);

  const fetchProfile = async () => {
    try {
      const res = await axios.get('/api/doctor/profile', {
        headers: { Authorization: `Bearer ${token}` }
      });
      setProfile(res.data);
      setFormData(res.data);
    } catch (e) {
      console.error('Profile fetch error', e);
    }
  };

  const fetchAppointments = async () => {
    try {
      const res = await axios.get('/api/doctor/appointments', {
        headers: { Authorization: `Bearer ${token}` }
      });
      setAppointments(res.data || []);
      setCompletedAppointments(res.data?.filter(a => a.status === 'COMPLETED') || []);
    } catch (e) {
      console.error('Appointments fetch error', e);
    }
  };

  const fetchTodayAppointments = async () => {
    try {
      const res = await axios.get('/api/doctor/appointments/today', {
        headers: { Authorization: `Bearer ${token}` }
      });
      setTodayAppointments(res.data || []);
    } catch (e) {
      console.error('Today appointments fetch error', e);
    }
  };

  const saveProfile = async () => {
    if (!userRole || !userRole.toUpperCase().includes('DOCTOR')) {
      setMessage('You must be logged in as a doctor to save this profile');
      return;
    }
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
      console.error('Save profile error:', err);
      const serverMsg = err?.response?.data || err.message || 'Error saving profile';
      setMessage(typeof serverMsg === 'string' ? serverMsg : JSON.stringify(serverMsg));
    } finally {
      setLoading(false);
    }
  };

  const updateAppointmentStatus = async (appointmentId, status) => {
    setLoading(true);
    try {
      await axios.put(
        `/api/doctor/appointments/${appointmentId}/status`,
        { status },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setMessage('Status updated successfully!');
      setTimeout(() => setMessage(''), 3000);
      fetchAppointments();
      fetchTodayAppointments();
      setSelectedAppointment(null);
    } catch (err) {
      setMessage('Error updating status');
    } finally {
      setLoading(false);
    }
  };

  const savePrescription = async () => {
    if (!selectedAppointment) return;
    setLoading(true);
    try {
      const res = await axios.post(
        `/api/doctor/appointments/${selectedAppointment.id}/prescriptions`,
        prescriptionForm,
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setMessage('Prescription saved successfully!');
      setSelectedPrescription(res.data);
      setShowPrescriptionForm(false);
      setPrescriptionForm({
        diagnosis: '',
        medicinesJson: '[]',
        testsRecommended: '',
        followUpDate: '',
        notes: ''
      });
      setTimeout(() => setMessage(''), 3000);
      fetchAppointments();
    } catch (err) {
      setMessage('Error saving prescription');
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: name === 'experienceYears' ? parseInt(value) || '' : value
    }));
  };

  const formatDate = (dateStr) => {
    const date = new Date(dateStr);
    return date.toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const containerVariants = {
    hidden: { opacity: 0 },
    visible: { opacity: 1, transition: { staggerChildren: 0.1 } }
  };

  const itemVariants = {
    hidden: { y: 20, opacity: 0 },
    visible: {
      y: 0,
      opacity: 1,
      transition: { type: 'spring', stiffness: 100 }
    }
  };

  return (
    <div className="dashboard-container">
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ duration: 0.5 }}
        className="dashboard-header"
      >
        <div className="header-content">
          <h1>Doctor Dashboard</h1>
          <p className="text-muted">Dr. {profile?.fullName || 'Welcome'}</p>
        </div>
      </motion.div>

      {message && (
        <motion.div
          initial={{ y: -20, opacity: 0 }}
          animate={{ y: 0, opacity: 1 }}
          className={`alert alert-${message.includes('success') ? 'success' : 'error'}`}
        >
          {message}
        </motion.div>
      )}

      <div className="tabs">
        {['overview', 'appointments', 'patients', 'profile'].map((tab) => (
          <button
            key={tab}
            className={`tab-btn ${activeTab === tab ? 'active' : ''}`}
            onClick={() => setActiveTab(tab)}
          >
            {tab.charAt(0).toUpperCase() + tab.slice(1)}
          </button>
        ))}
      </div>

      {activeTab === 'overview' && (
        <>
          <motion.div
            variants={containerVariants}
            initial="hidden"
            animate="visible"
            className="grid grid-4 mb-4"
          >
            <motion.div variants={itemVariants} className="stat-card">
              <div className="stat-icon" style={{ backgroundColor: '#ede9fe' }}>
                <FiClock size={24} color="#7c3aed" />
              </div>
              <div className="stat-content">
                <p className="stat-label">Today's</p>
                <h3 className="stat-value">{todayAppointments.length}</h3>
              </div>
            </motion.div>

            <motion.div variants={itemVariants} className="stat-card">
              <div className="stat-icon" style={{ backgroundColor: '#dcfce7' }}>
                <FiUsers size={24} color="#059669" />
              </div>
              <div className="stat-content">
                <p className="stat-label">Patients</p>
                <h3 className="stat-value">{new Set(appointments.map(a => a.patient?.id)).size}</h3>
              </div>
            </motion.div>

            <motion.div variants={itemVariants} className="stat-card">
              <div className="stat-icon" style={{ backgroundColor: '#dcfce7' }}>
                <FiCheckSquare size={24} color="#059669" />
              </div>
              <div className="stat-content">
                <p className="stat-label">Completed</p>
                <h3 className="stat-value">{completedAppointments.length}</h3>
              </div>
            </motion.div>

            <motion.div variants={itemVariants} className="stat-card">
              <div className="stat-icon" style={{ backgroundColor: '#fef3c7' }}>
                <FiFileText size={24} color="#d97706" />
              </div>
              <div className="stat-content">
                <p className="stat-label">Appointments</p>
                <h3 className="stat-value">{appointments.length}</h3>
              </div>
            </motion.div>
          </motion.div>

          <motion.div
            variants={containerVariants}
            initial="hidden"
            animate="visible"
            className="grid grid-2 mb-4"
          >
            <motion.div variants={itemVariants} className="card">
              <div className="card-header">
                <h3 className="card-title">Today's Appointments</h3>
              </div>
              {todayAppointments.length > 0 ? (
                <div className="appointments-list">
                  {todayAppointments.map((apt) => (
                    <div
                      key={apt.id}
                      className="appointment-item"
                      onClick={() => setSelectedAppointment(apt)}
                    >
                      <div className="appointment-doctor">
                        <div className="doctor-avatar">{apt.patient?.fullName?.charAt(0)}</div>
                        <div className="doctor-info">
                          <p className="font-semibold">{apt.patient?.fullName}</p>
                          <p className="text-sm text-muted">{apt.patient?.age} years</p>
                        </div>
                      </div>
                      <div className="appointment-time">
                        <FiClock size={16} />
                        <span>{formatDate(apt.appointmentDate)}</span>
                      </div>
                      <span className={`badge badge-${apt.status.toLowerCase()}`}>
                        {apt.status}
                      </span>
                    </div>
                  ))}
                </div>
              ) : (
                <p className="text-muted text-center py-4">No appointments today</p>
              )}
            </motion.div>

            <motion.div variants={itemVariants} className="card">
              <div className="card-header">
                <h3 className="card-title">Appointment Details</h3>
              </div>
              {selectedAppointment ? (
                <div className="appointment-details">
                  <div className="detail-item">
                    <strong>Patient:</strong>
                    <p>{selectedAppointment.patient?.fullName}</p>
                  </div>
                  <div className="detail-item">
                    <strong>Age:</strong>
                    <p>{selectedAppointment.patient?.age}</p>
                  </div>
                  <div className="detail-item">
                    <strong>Phone:</strong>
                    <p>{selectedAppointment.patient?.phone}</p>
                  </div>
                  {selectedAppointment.symptoms && (
                    <div className="detail-item">
                      <strong>Symptoms:</strong>
                      <p>{selectedAppointment.symptoms}</p>
                    </div>
                  )}
                  <div className="button-group">
                    <select
                      value={selectedAppointment.status}
                      onChange={(e) => updateAppointmentStatus(selectedAppointment.id, e.target.value)}
                      className="form-select"
                    >
                      <option value="SCHEDULED">Scheduled</option>
                      <option value="COMPLETED">Completed</option>
                      <option value="CANCELLED">Cancelled</option>
                      <option value="NO_SHOW">No Show</option>
                    </select>
                    <button
                      onClick={() => setShowPrescriptionForm(true)}
                      className="btn btn-primary"
                    >
                      <FiEdit2 /> Add Prescription
                    </button>
                  </div>
                </div>
              ) : (
                <p className="text-muted text-center py-4">Select an appointment</p>
              )}
            </motion.div>
          </motion.div>
        </>
      )}

      {activeTab === 'appointments' && (
        <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="card">
          <div className="card-header">
            <h3 className="card-title">All Appointments</h3>
          </div>
          {appointments.length > 0 ? (
            <div className="table-responsive">
              <table className="table">
                <thead>
                  <tr>
                    <th>Patient</th>
                    <th>Date & Time</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {appointments.map((apt) => (
                    <tr
                      key={apt.id}
                      onClick={() => setSelectedAppointment(apt)}
                      style={{ cursor: 'pointer' }}
                    >
                      <td>{apt.patient?.fullName}</td>
                      <td>{formatDate(apt.appointmentDate)}</td>
                      <td>
                        <span className={`badge badge-${apt.status.toLowerCase()}`}>
                          {apt.status}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <p className="text-muted text-center py-8">No appointments found</p>
          )}
        </motion.div>
      )}

      {activeTab === 'profile' && (
        <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="card">
          <div className="card-header">
            <h3 className="card-title">My Profile</h3>
          </div>
          <div className="grid grid-2">
            <div className="form-group">
              <label className="form-label">Full Name</label>
              <input
                type="text"
                name="fullName"
                value={formData.fullName}
                onChange={handleInputChange}
                className="form-input"
              />
            </div>
            <div className="form-group">
              <label className="form-label">Specialization</label>
              <input
                type="text"
                name="specialization"
                value={formData.specialization}
                onChange={handleInputChange}
                className="form-input"
              />
            </div>
            <div className="form-group">
              <label className="form-label">Qualification</label>
              <input
                type="text"
                name="qualification"
                value={formData.qualification}
                onChange={handleInputChange}
                className="form-input"
              />
            </div>
            <div className="form-group">
              <label className="form-label">Experience (years)</label>
              <input
                type="number"
                name="experienceYears"
                value={formData.experienceYears}
                onChange={handleInputChange}
                className="form-input"
              />
            </div>
            <div className="form-group">
              <label className="form-label">Hospital/Clinic</label>
              <input
                type="text"
                name="hospitalName"
                value={formData.hospitalName}
                onChange={handleInputChange}
                className="form-input"
              />
            </div>
            <div className="form-group">
              <label className="form-label">Phone</label>
              <input
                type="tel"
                name="phone"
                value={formData.phone}
                onChange={handleInputChange}
                className="form-input"
              />
            </div>
          </div>
          <div className="form-group">
            <label className="form-label">Bio</label>
            <textarea
              name="bio"
              value={formData.bio}
              onChange={handleInputChange}
              className="form-textarea"
            />
          </div>
          <button
            onClick={saveProfile}
            disabled={loading}
            className="btn btn-primary btn-block"
          >
            {loading ? 'Saving...' : 'Save Profile'}
          </button>
        </motion.div>
      )}

      {showPrescriptionForm && selectedAppointment && (
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          className="modal-overlay"
          onClick={() => setShowPrescriptionForm(false)}
        >
          <motion.div
            initial={{ scale: 0.9 }}
            animate={{ scale: 1 }}
            className="card"
            style={{ maxWidth: '500px' }}
            onClick={(e) => e.stopPropagation()}
          >
            <h3 className="card-title">Add Prescription</h3>
            <div className="form-group">
              <label className="form-label">Diagnosis</label>
              <textarea
                value={prescriptionForm.diagnosis}
                onChange={(e) =>
                  setPrescriptionForm({ ...prescriptionForm, diagnosis: e.target.value })
                }
                className="form-textarea"
              />
            </div>
            <div className="form-group">
              <label className="form-label">Tests Recommended</label>
              <input
                type="text"
                value={prescriptionForm.testsRecommended}
                onChange={(e) =>
                  setPrescriptionForm({ ...prescriptionForm, testsRecommended: e.target.value })
                }
                className="form-input"
                placeholder="Comma-separated"
              />
            </div>
            <div className="form-group">
              <label className="form-label">Follow-up Date</label>
              <input
                type="date"
                value={prescriptionForm.followUpDate}
                onChange={(e) =>
                  setPrescriptionForm({ ...prescriptionForm, followUpDate: e.target.value })
                }
                className="form-input"
              />
            </div>
            <div className="form-group">
              <label className="form-label">Additional Notes</label>
              <textarea
                value={prescriptionForm.notes}
                onChange={(e) => setPrescriptionForm({ ...prescriptionForm, notes: e.target.value })}
                className="form-textarea"
              />
            </div>
            <div style={{ display: 'flex', gap: '1rem' }}>
              <button
                onClick={savePrescription}
                disabled={loading}
                className="btn btn-primary btn-block"
              >
                {loading ? 'Saving...' : 'Save Prescription'}
              </button>
              <button
                onClick={() => setShowPrescriptionForm(false)}
                className="btn btn-secondary btn-block"
              >
                Cancel
              </button>
            </div>
          </motion.div>
        </motion.div>
      )}

      {selectedPrescription && (
        <PrescriptionView
          prescription={selectedPrescription}
          onClose={() => setSelectedPrescription(null)}
        />
      )}
    </div>
  );
}
