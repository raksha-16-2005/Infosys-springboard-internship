import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { motion } from 'framer-motion';
import { FiCalendar, FiFileText, FiActivity, FiArrowRight, FiClock } from 'react-icons/fi';
import PrescriptionView from './PrescriptionView';
import PatientCalendar from './PatientCalendar';
import '../styles/dashboard.css';

import { fetchUserProfile, updateUserProfile, clearUserMessages } from '../features/user/userSlice';
import {
  fetchPatientAppointments,
  fetchUpcomingAppointments,
  fetchPatientPrescriptions,
  fetchPatientFollowUps,
} from '../features/appointment/appointmentSlice';

export default function PatientDashboard() {
  const dispatch = useDispatch();

  // Redux State
  const { profile, loading: userLoading, successMessage: userSuccess, error: userError } = useSelector((state) => state.user);
  const {
    appointments,
    upcomingAppointments,
    prescriptions,
    followUps,
    loading: aptLoading
  } = useSelector((state) => state.appointment);

  const [activeTab, setActiveTab] = useState('overview');
  const [selectedAppointment, setSelectedAppointment] = useState(null);
  const [selectedPrescription, setSelectedPrescription] = useState(null);
  const [message, setMessage] = useState('');

  // Local form state initialized from Redux profile
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

  useEffect(() => {
    dispatch(fetchUserProfile());
    dispatch(fetchPatientAppointments());
    dispatch(fetchUpcomingAppointments());
    dispatch(fetchPatientPrescriptions());
    dispatch(fetchPatientFollowUps());
  }, [dispatch]);

  // Sync profile to local form state when profile loads
  useEffect(() => {
    if (profile) {
      setFormData({
        fullName: profile.fullName || '',
        age: profile.age || '',
        gender: profile.gender || '',
        bloodGroup: profile.bloodGroup || '',
        phone: profile.phone || '',
        address: profile.address || '',
        emergencyContact: profile.emergencyContact || '',
        medicalHistory: profile.medicalHistory || ''
      });
    }
  }, [profile]);

  // Handle Redux messages
  useEffect(() => {
    if (userSuccess) {
      setMessage(userSuccess);
      const timer = setTimeout(() => {
        setMessage('');
        dispatch(clearUserMessages());
      }, 3000);
      return () => clearTimeout(timer);
    }
    if (userError) {
      setMessage(typeof userError === 'string' ? userError : 'Error updating profile');
      const timer = setTimeout(() => {
        setMessage('');
        dispatch(clearUserMessages());
      }, 3000);
      return () => clearTimeout(timer);
    }
  }, [userSuccess, userError, dispatch]);

  const saveProfile = async () => {
    dispatch(updateUserProfile(formData));
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
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
    visible: {
      opacity: 1,
      transition: { staggerChildren: 0.1 }
    }
  };

  const itemVariants = {
    hidden: { y: 20, opacity: 0 },
    visible: {
      y: 0,
      opacity: 1,
      transition: { type: 'spring', stiffness: 100 }
    }
  };

  const isLoading = userLoading || aptLoading;

  return (
    <div className="dashboard-container">
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ duration: 0.5 }}
        className="dashboard-header"
      >
        <div className="header-content">
          <h1>Patient Dashboard</h1>
          <p className="text-muted">Welcome, {profile?.fullName || 'Patient'}</p>
        </div>
      </motion.div>

      {message && (
        <motion.div
          initial={{ y: -20, opacity: 0 }}
          animate={{ y: 0, opacity: 1 }}
          className={`alert alert-${message.toLowerCase().includes('success') ? 'success' : 'error'}`}
        >
          {message}
        </motion.div>
      )}

      <div className="tabs">
        {['overview', 'calendar', 'appointments', 'prescriptions', 'profile'].map((tab) => (
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
                <FiCalendar size={24} color="#7c3aed" />
              </div>
              <div className="stat-content">
                <p className="stat-label">Upcoming</p>
                <h3 className="stat-value">{upcomingAppointments.length}</h3>
              </div>
            </motion.div>

            <motion.div variants={itemVariants} className="stat-card">
              <div className="stat-icon" style={{ backgroundColor: '#dcfce7' }}>
                <FiActivity size={24} color="#059669" />
              </div>
              <div className="stat-content">
                <p className="stat-label">Total Visits</p>
                <h3 className="stat-value">{appointments.length}</h3>
              </div>
            </motion.div>

            <motion.div variants={itemVariants} className="stat-card">
              <div className="stat-icon" style={{ backgroundColor: '#fce7f3' }}>
                <FiArrowRight size={24} color="#be185d" />
              </div>
              <div className="stat-content">
                <p className="stat-label">Follow-ups</p>
                <h3 className="stat-value">{followUps.length}</h3>
              </div>
            </motion.div>

            <motion.div variants={itemVariants} className="stat-card">
              <div className="stat-icon" style={{ backgroundColor: '#fef3c7' }}>
                <FiFileText size={24} color="#d97706" />
              </div>
              <div className="stat-content">
                <p className="stat-label">Prescriptions</p>
                <h3 className="stat-value">{prescriptions.length}</h3>
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
                <h3 className="card-title">Upcoming Appointments</h3>
              </div>
              {upcomingAppointments.length > 0 ? (
                <div className="appointments-list">
                  {upcomingAppointments.slice(0, 3).map((apt) => (
                    <div key={apt.id} className="appointment-item">
                      <div className="appointment-doctor">
                        <div className="doctor-avatar">{apt.doctor?.fullName?.charAt(0)}</div>
                        <div className="doctor-info">
                          <p className="font-semibold">{apt.doctor?.fullName}</p>
                          <p className="text-sm text-muted">{apt.doctor?.specialization}</p>
                        </div>
                      </div>
                      <div className="appointment-time">
                        <FiClock size={16} />
                        <span>{formatDate(apt.appointmentDate)}</span>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <p className="text-muted text-center py-4">No upcoming appointments</p>
              )}
            </motion.div>

            <motion.div variants={itemVariants} className="card">
              <div className="card-header">
                <h3 className="card-title">Recent Prescriptions</h3>
              </div>
              {prescriptions.length > 0 ? (
                <div className="prescriptions-list">
                  {prescriptions.slice(0, 3).map((rx) => (
                    <div
                      key={rx.id}
                      className="prescription-item"
                      onClick={() => setSelectedPrescription(rx)}
                      style={{ cursor: 'pointer' }}
                    >
                      <div>
                        <p className="font-semibold">{rx.diagnosis}</p>
                        <p className="text-sm text-muted">Follow-up: {rx.followUpDate || 'Not set'}</p>
                      </div>
                      <FiArrowRight size={16} />
                    </div>
                  ))}
                </div>
              ) : (
                <p className="text-muted text-center py-4">No prescriptions yet</p>
              )}
            </motion.div>
          </motion.div>
        </>
      )}

      {activeTab === 'calendar' && (
        <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }}>
          <PatientCalendar appointments={appointments} />
        </motion.div>
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
                    <th>Doctor</th>
                    <th>Specialization</th>
                    <th>Date & Time</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {appointments.map((apt) => (
                    <tr key={apt.id} onClick={() => setSelectedAppointment(apt)} style={{ cursor: 'pointer' }}>
                      <td>{apt.doctor?.fullName}</td>
                      <td>{apt.doctor?.specialization}</td>
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

      {activeTab === 'prescriptions' && (
        <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }}>
          {prescriptions.length > 0 ? (
            <div className="grid grid-2">
              {prescriptions.map((rx) => (
                <div
                  key={rx.id}
                  className="card"
                  onClick={() => setSelectedPrescription(rx)}
                  style={{ cursor: 'pointer' }}
                >
                  <div className="card-body">
                    <h4 className="font-semibold">{rx.diagnosis}</h4>
                    <p className="text-sm text-muted mt-2">Follow-up: {rx.followUpDate || 'Not set'}</p>
                    {rx.notes && <p className="text-sm mt-2">{rx.notes.substring(0, 100)}...</p>}
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="card text-center py-8">
              <p className="text-muted">No prescriptions available</p>
            </div>
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
              <label className="form-label">Age</label>
              <input
                type="number"
                name="age"
                value={formData.age}
                onChange={handleInputChange}
                className="form-input"
              />
            </div>
            <div className="form-group">
              <label className="form-label">Gender</label>
              <select name="gender" value={formData.gender} onChange={handleInputChange} className="form-input">
                <option value="">Select Gender</option>
                <option value="Male">Male</option>
                <option value="Female">Female</option>
                <option value="Other">Other</option>
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">Blood Group</label>
              <input
                type="text"
                name="bloodGroup"
                value={formData.bloodGroup}
                onChange={handleInputChange}
                className="form-input"
                placeholder="e.g., O+"
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
            <div className="form-group">
              <label className="form-label">Address</label>
              <input
                type="text"
                name="address"
                value={formData.address}
                onChange={handleInputChange}
                className="form-input"
              />
            </div>
            <div className="form-group col-2">
              <label className="form-label">Medical History</label>
              <textarea
                name="medicalHistory"
                value={formData.medicalHistory}
                onChange={handleInputChange}
                className="form-textarea"
              />
            </div>
          </div>
          <button
            onClick={saveProfile}
            disabled={isLoading}
            className="btn btn-primary btn-block mt-4"
          >
            {isLoading ? 'Saving...' : 'Save Profile'}
          </button>
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
