import React, { useEffect, useMemo, useState } from 'react';
import axios from 'axios';
import { motion } from 'framer-motion';
import Calendar from 'react-calendar';
import {
  FiHome,
  FiCalendar,
  FiFileText,
  FiSettings,
  FiLogOut,
  FiUser,
  FiClock,
  FiMapPin,
  FiPhone,
  FiMail,
  FiSearch,
  FiMenu,
  FiX,
  FiDownload,
  FiCheckCircle,
  FiXCircle,
  FiAlertCircle
} from 'react-icons/fi';
import { FaStethoscope, FaCalendarAlt, FaHeartbeat, FaHospital } from 'react-icons/fa';
import PrescriptionView from './PrescriptionView';
import 'react-calendar/dist/Calendar.css';
import '../styles/patient.css';

const navItems = [
  { key: 'dashboard', label: 'Dashboard', icon: FiHome },
  { key: 'bookAppointment', label: 'Book Appointment', icon: FaCalendarAlt },
  { key: 'appointments', label: 'My Appointments', icon: FiFileText },
  { key: 'feedback', label: 'Feedback', icon: FiCheckCircle },
  { key: 'medicalRecords', label: 'Medical Records', icon: FaStethoscope },
  { key: 'calendar', label: 'Calendar', icon: FiCalendar },
  { key: 'profile', label: 'Edit Profile', icon: FiSettings }
];

export default function PatientDashboard() {
  const token = localStorage.getItem('token');
  const [activeSection, setActiveSection] = useState('dashboard');
  const [profile, setProfile] = useState(null);
  const [appointments, setAppointments] = useState([]);
  const [doctors, setDoctors] = useState([]);
  const [filteredDoctors, setFilteredDoctors] = useState([]);
  const [medicalRecords, setMedicalRecords] = useState([]);
  const [prescriptions, setPrescriptions] = useState([]);
  const [feedbackList, setFeedbackList] = useState([]);
  const [selectedDate, setSelectedDate] = useState(new Date());
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);
  const [profileImage, setProfileImage] = useState(null);
  const [profilePreview, setProfilePreview] = useState('');
  const [selectedPrescription, setSelectedPrescription] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [specializationFilter, setSpecializationFilter] = useState('All');
  const [selectedDoctor, setSelectedDoctor] = useState(null);
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);
  const [avatarTilt, setAvatarTilt] = useState({ x: 0, y: 0 });
  const [bookingForm, setBookingForm] = useState({
    appointmentDate: '',
    timeSlot: '',
    symptoms: '',
    notes: ''
  });
  const [feedbackForm, setFeedbackForm] = useState({
    appointmentId: '',
    rating: 5,
    comments: ''
  });
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

  const authHeader = useMemo(() => ({ Authorization: `Bearer ${token}` }), [token]);

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    window.location.assign('/');
  };

  useEffect(() => {
    fetchProfile();
    fetchAppointments();
    fetchDoctors();
    fetchMedicalRecords();
    fetchPrescriptions();
    fetchFeedback();
  }, []);

  const fetchProfile = async () => {
    try {
      const res = await axios.get('/api/patient/profile', { headers: authHeader });
      setProfile(res.data);
      setFormData(res.data);
    } catch (e) {
      console.error('Error fetching profile:', e);
    }
  };

  const fetchAppointments = async () => {
    try {
      const res = await axios.get('/api/patient/appointments', { headers: authHeader });
      setAppointments(res.data || []);
    } catch (e) {
      console.error('Error fetching appointments:', e);
    }
  };

  const fetchDoctors = async () => {
    try {
      const res = await axios.get('/api/doctors');
      setDoctors(res.data || []);
      setFilteredDoctors(res.data || []);
    } catch (e) {
      console.error('Error fetching doctors:', e);
    }
  };

  const fetchMedicalRecords = async () => {
    try {
      const res = await axios.get('/api/patient/medical-records', { headers: authHeader });
      setMedicalRecords(res.data || []);
    } catch (e) {
      console.error('Error fetching medical records:', e);
    }
  };

  const fetchPrescriptions = async () => {
    try {
      const res = await axios.get('/api/patient/prescriptions', { headers: authHeader });
      setPrescriptions(res.data || []);
    } catch (e) {
      console.error('Error fetching prescriptions:', e);
    }
  };

  const fetchFeedback = async () => {
    try {
      const res = await axios.get('/api/patient/feedback', { headers: authHeader });
      setFeedbackList(res.data || []);
    } catch (e) {
      console.error('Error fetching feedback:', e);
    }
  };

  const handleInputChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleBookingChange = (e) => {
    setBookingForm({ ...bookingForm, [e.target.name]: e.target.value });
  };

  const saveProfile = async () => {
    setLoading(true);
    setMessage('');
    try {
      await axios.put('/api/patient/profile', formData, { headers: authHeader });
      setMessage('Profile updated successfully!');
      fetchProfile();
      
      const user = JSON.parse(localStorage.getItem('user') || '{}');
      user.displayName = formData.fullName;
      localStorage.setItem('user', JSON.stringify(user));
      window.dispatchEvent(new Event('storage'));
    } catch (e) {
      setMessage('Error updating profile');
      console.error(e);
    }
    setLoading(false);
  };

  const uploadProfileImage = async () => {
    if (!profileImage) return;
    const formDataObj = new FormData();
    formDataObj.append('file', profileImage);
    try {
      const res = await axios.post('/api/patient/profile/image', formDataObj, {
        headers: { ...authHeader, 'Content-Type': 'multipart/form-data' }
      });
      setMessage('Profile image uploaded!');
      setProfilePreview('');
      setProfileImage(null);
      fetchProfile();
    } catch (e) {
      setMessage('Error uploading image');
      console.error(e);
    }
  };

  const handleImageChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setProfileImage(file);
      setProfilePreview(URL.createObjectURL(file));
    }
  };

  const cancelAppointment = async (appointmentId) => {
    if (!window.confirm('Are you sure you want to cancel this appointment?')) return;
    try {
      await axios.put(`/api/patient/appointments/${appointmentId}/cancel`, {}, { headers: authHeader });
      setMessage('Appointment cancelled successfully');
      fetchAppointments();
    } catch (e) {
      setMessage('Error cancelling appointment');
      console.error(e);
    }
  };

  const submitFeedback = async () => {
    if (!feedbackForm.appointmentId) {
      setMessage('Please select a completed appointment');
      return;
    }
    if (feedbackForm.rating < 1 || feedbackForm.rating > 5) {
      setMessage('Rating must be between 1 and 5');
      return;
    }

    setLoading(true);
    try {
      await axios.post('/api/patient/feedback', {
        appointmentId: Number(feedbackForm.appointmentId),
        rating: Number(feedbackForm.rating),
        comments: feedbackForm.comments || ''
      }, { headers: authHeader });

      setMessage('Feedback submitted successfully');
      setFeedbackForm({ appointmentId: '', rating: 5, comments: '' });
      fetchFeedback();
    } catch (e) {
      setMessage(e.response?.data || 'Error submitting feedback');
      console.error(e);
    }
    setLoading(false);
  };

  const convertTo24Hour = (timeStr) => {
    // If already in HH:MM format, return as is
    if (/^\d{2}:\d{2}$/.test(timeStr)) {
      return timeStr;
    }
    
    // Handle formats like "7pm to 8pm" or "7pm-8pm" - extract first time
    const match = timeStr.match(/(\d+)\s*(am|pm)/i);
    if (match) {
      let hour = parseInt(match[1]);
      const isPM = match[2].toLowerCase() === 'pm';
      
      if (isPM && hour !== 12) hour += 12;
      if (!isPM && hour === 12) hour = 0;
      
      return `${hour.toString().padStart(2, '0')}:00`;
    }
    
    // Default to 9 AM if can't parse
    return '09:00';
  };

  const bookAppointment = async () => {
    if (!selectedDoctor || !bookingForm.appointmentDate || !bookingForm.timeSlot) {
      setMessage('Please fill in all required fields');
      return;
    }

    setLoading(true);
    try {
      const time24 = convertTo24Hour(bookingForm.timeSlot);
      const dateTime = `${bookingForm.appointmentDate}T${time24}:00`;
      const requestData = {
        doctorId: selectedDoctor.userId,
        appointmentDate: dateTime,
        symptoms: bookingForm.symptoms || '',
        notes: bookingForm.notes || ''
      };
      
      console.log('Booking appointment with data:', requestData);
      
      const response = await axios.post('/api/patient/appointments', requestData, { 
        headers: authHeader 
      });
      
      console.log('Appointment response:', response.data);
      setMessage('Appointment booked successfully!');
      setSelectedDoctor(null);
      setBookingForm({ appointmentDate: '', timeSlot: '', symptoms: '', notes: '' });
      fetchAppointments();
      setActiveSection('appointments');
    } catch (e) {
      console.error('Error booking appointment:', e);
      console.error('Error response:', e.response?.data);
      console.error('Request data:', {
        doctorId: selectedDoctor?.userId,
        selectedDoctor: selectedDoctor
      });
      setMessage(e.response?.data || 'Error booking appointment');
    }
    setLoading(false);
  };

  useEffect(() => {
    let filtered = doctors;
    
    if (specializationFilter !== 'All') {
      filtered = filtered.filter(d => d.specialization === specializationFilter);
    }
    
    if (searchQuery) {
      filtered = filtered.filter(d =>
        d.fullName?.toLowerCase().includes(searchQuery.toLowerCase()) ||
        d.specialization?.toLowerCase().includes(searchQuery.toLowerCase())
      );
    }
    
    setFilteredDoctors(filtered);
  }, [searchQuery, specializationFilter, doctors]);

  const specializations = useMemo(() => {
    const specs = new Set(doctors.map(d => d.specialization).filter(Boolean));
    return ['All', ...Array.from(specs)];
  }, [doctors]);

  const badgeClass = (status) => {
    const normalized = status?.toLowerCase().replace(/_/g, '-');
    return `badge badge-${normalized}`;
  };

  const upcomingAppointments = useMemo(() => {
    return appointments
      .filter(a => new Date(a.appointmentDate) >= new Date() && a.status !== 'CANCELLED')
      .sort((a, b) => new Date(a.appointmentDate) - new Date(b.appointmentDate))
      .slice(0, 5);
  }, [appointments]);

  const tileClassName = ({ date, view }) => {
    if (view === 'month') {
      const hasAppointment = appointments.some(a => {
        const apptDate = new Date(a.appointmentDate);
        return apptDate.toDateString() === date.toDateString();
      });
      if (hasAppointment) {
        const appt = appointments.find(a => {
          const apptDate = new Date(a.appointmentDate);
          return apptDate.toDateString() === date.toDateString();
        });
        if (appt?.status === 'SCHEDULED') return 'scheduled-date';
        if (appt?.status === 'ACCEPTED') return 'accepted-date';
        if (appt?.status === 'REJECTED') return 'rejected-date';
        if (appt?.status === 'RESCHEDULED') return 'rescheduled-date';
        if (appt?.status === 'COMPLETED') return 'completed-date';
      }
    }
    return null;
  };

  const appointmentsOnSelectedDate = useMemo(() => {
    return appointments.filter(a => {
      const apptDate = new Date(a.appointmentDate);
      return apptDate.toDateString() === selectedDate.toDateString();
    });
  }, [appointments, selectedDate]);

  const feedbackByAppointment = useMemo(() => {
    const map = new Map();
    feedbackList.forEach(item => {
      if (item.appointmentId) {
        map.set(item.appointmentId, item);
      }
    });
    return map;
  }, [feedbackList]);

  const completedAppointments = useMemo(() => {
    return appointments.filter(a => a.status === 'COMPLETED');
  }, [appointments]);

  const handleSectionChange = (section) => {
    setActiveSection(section);
    setIsSidebarOpen(false);
  };

  const handleDashboardMouseMove = (event) => {
    const xCenter = window.innerWidth / 2;
    const yCenter = window.innerHeight / 2;
    const xShift = ((event.clientX - xCenter) / xCenter) * 8;
    const yShift = ((event.clientY - yCenter) / yCenter) * 8;
    setAvatarTilt({ x: xShift, y: yShift });
  };

  return (
    <motion.div
      className="patient-shell"
      initial={{ opacity: 0, y: 12 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.4, ease: 'easeOut' }}
      onMouseMove={handleDashboardMouseMove}
      onMouseLeave={() => setAvatarTilt({ x: 0, y: 0 })}
    >
      <div className="patient-bg-clouds" aria-hidden="true">
        <span className="patient-cloud cloud-a" />
        <span className="patient-cloud cloud-b" />
        <span className="patient-cloud cloud-c" />
      </div>

      <button
        type="button"
        className="patient-mobile-toggle"
        aria-label={isSidebarOpen ? 'Close navigation' : 'Open navigation'}
        onClick={() => setIsSidebarOpen((prev) => !prev)}
      >
        {isSidebarOpen ? <FiX /> : <FiMenu />}
      </button>

      <div className={`patient-sidebar ${isSidebarOpen ? 'open' : ''}`}>
        <div className="sidebar-header">
          <FaHeartbeat className="sidebar-logo" />
          <h2>Patient Portal</h2>
        </div>
        <nav className="sidebar-nav">
          {navItems.map(item => (
            <motion.button
              key={item.key}
              className={`nav-item ${activeSection === item.key ? 'active' : ''}`}
              onClick={() => handleSectionChange(item.key)}
              whileHover={{ x: 4 }}
              whileTap={{ scale: 0.98 }}
            >
              <item.icon className="nav-icon" />
              <span>{item.label}</span>
            </motion.button>
          ))}
          <motion.button className="nav-item logout" onClick={handleLogout} whileHover={{ x: 4 }} whileTap={{ scale: 0.98 }}>
            <FiLogOut className="nav-icon" />
            <span>Logout</span>
          </motion.button>
        </nav>

        <div className="patient-cursor-avatar">
          <div className="patient-avatar-initials">PT</div>
          <div
            className="patient-avatar-head"
            style={{ transform: `translate(${avatarTilt.x}px, ${avatarTilt.y}px)` }}
          >
            <span className="patient-eye left"><i style={{ transform: `translate(${avatarTilt.x * 0.2}px, ${avatarTilt.y * 0.2}px)` }} /></span>
            <span className="patient-eye right"><i style={{ transform: `translate(${avatarTilt.x * 0.2}px, ${avatarTilt.y * 0.2}px)` }} /></span>
          </div>
        </div>
      </div>

      <motion.div
        className="patient-content"
        initial={{ opacity: 0, y: 10 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.35, delay: 0.1 }}
      >
        <div className="patient-header">
          <h1>{navItems.find(n => n.key === activeSection)?.label || 'Dashboard'}</h1>
          {profile && (
            <div className="user-info">
              {profile.profileImagePath && (
                <img src={`/${profile.profileImagePath}`} alt="Profile" className="user-avatar" />
              )}
              <span>Welcome, {profile.fullName || 'Patient'}</span>
            </div>
          )}
        </div>

        {message && (
          <motion.div
            initial={{ opacity: 0, y: -20 }}
            animate={{ opacity: 1, y: 0 }}
            className={`alert ${message.includes('Error') ? 'alert-error' : 'alert-success'}`}
          >
            {message}
            <button onClick={() => setMessage('')}><FiX /></button>
          </motion.div>
        )}

        {/* Dashboard Overview */}
        {activeSection === 'dashboard' && (
          <div className="patient-dashboard">
            {/* Stats Cards */}
            <div className="stats-grid">
              <motion.div className="stat-card" whileHover={{ scale: 1.02 }}>
                <FiCalendar className="stat-icon" />
                <div className="stat-content">
                  <h3>{upcomingAppointments.length}</h3>
                  <p>Upcoming Appointments</p>
                </div>
              </motion.div>
              <motion.div className="stat-card" whileHover={{ scale: 1.02 }}>
                <FiFileText className="stat-icon" />
                <div className="stat-content">
                  <h3>{appointments.length}</h3>
                  <p>Total Appointments</p>
                </div>
              </motion.div>
              <motion.div className="stat-card" whileHover={{ scale: 1.02 }}>
                <FaStethoscope className="stat-icon" />
                <div className="stat-content">
                  <h3>{prescriptions.length}</h3>
                  <p>Prescriptions</p>
                </div>
              </motion.div>
              <motion.div className="stat-card" whileHover={{ scale: 1.02 }}>
                <FiFileText className="stat-icon" />
                <div className="stat-content">
                  <h3>{medicalRecords.length}</h3>
                  <p>Medical Records</p>
                </div>
              </motion.div>
            </div>

            {/* Upcoming Appointments */}
            <motion.div
              className="dashboard-section"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
            >
              <h2>Upcoming Appointments</h2>
              {upcomingAppointments.length === 0 ? (
                <p className="no-data">No upcoming appointments</p>
              ) : (
                <div className="appointment-list">
                  {upcomingAppointments.map(appt => (
                    <div key={appt.id} className="appointment-item">
                      <div className="appointment-header">
                        <div>
                          <h4>Dr. {appt.doctor?.fullName || appt.doctorName || 'Unknown'}</h4>
                          <p className="specialty">{appt.doctor?.specialization || appt.doctorSpecialization || 'General'}</p>
                        </div>
                        <span className={badgeClass(appt.status)}>{appt.status}</span>
                      </div>
                      <div className="appointment-details">
                        <span><FiClock /> {new Date(appt.appointmentDate).toLocaleString()}</span>
                        {appt.doctorRemarks && (
                          <p className="remarks"><strong>Doctor's Remarks:</strong> {appt.doctorRemarks}</p>
                        )}
                        {appt.rescheduledDate && (
                          <p className="rescheduled">
                            <FiAlertCircle /> <strong>Rescheduled to:</strong> {new Date(appt.rescheduledDate).toLocaleString()}
                          </p>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </motion.div>

            {/* Quick Actions */}
            <div className="quick-actions">
              <button
                className="action-btn primary"
                onClick={() => setActiveSection('bookAppointment')}
              >
                <FaCalendarAlt /> Book New Appointment
              </button>
              <button
                className="action-btn secondary"
                onClick={() => setActiveSection('medicalRecords')}
              >
                <FaStethoscope /> View Medical Records
              </button>
            </div>
          </div>
        )}

        {/* Book Appointment */}
        {activeSection === 'bookAppointment' && (
          <div className="book-appointment-section">
            {!selectedDoctor ? (
              <>
                <div className="search-filters">
                  <div className="search-box">
                    <FiSearch className="search-icon" />
                    <input
                      type="text"
                      placeholder="Search doctors by name or specialization..."
                      value={searchQuery}
                      onChange={(e) => setSearchQuery(e.target.value)}
                    />
                  </div>
                  <select
                    value={specializationFilter}
                    onChange={(e) => setSpecializationFilter(e.target.value)}
                    className="filter-select"
                  >
                    {specializations.map(spec => (
                      <option key={spec} value={spec}>{spec}</option>
                    ))}
                  </select>
                </div>

                <div className="doctors-grid">
                  {filteredDoctors.map(doctor => (
                    <motion.div
                      key={doctor.id}
                      className="doctor-card"
                      whileHover={{ scale: 1.02 }}
                      initial={{ opacity: 0 }}
                      animate={{ opacity: 1 }}
                    >
                      <div className="doctor-image">
                        {doctor.profileImagePath ? (
                          <img src={`/${doctor.profileImagePath}`} alt={doctor.fullName} />
                        ) : (
                          <div className="doctor-placeholder">
                            <FiUser size={50} />
                          </div>
                        )}
                      </div>
                      <div className="doctor-info">
                        <h3>{doctor.fullName}</h3>
                        <p className="specialization">{doctor.specialization}</p>
                        <p className="qualification">{doctor.qualification}</p>
                        <div className="doctor-details">
                          <span><FaHospital /> {doctor.hospitalName || 'N/A'}</span>
                          <span><FiClock /> {doctor.experienceYears} years exp.</span>
                          <span className="fee">₹{doctor.consultationFee || 'N/A'}</span>
                        </div>
                        {doctor.bio && <p className="bio">{doctor.bio}</p>}
                        <button
                          className="btn-primary full-width"
                          onClick={() => setSelectedDoctor(doctor)}
                        >
                          Book Appointment
                        </button>
                      </div>
                    </motion.div>
                  ))}
                </div>
              </>
            ) : (
              <div className="booking-form">
                <button className="back-btn" onClick={() => setSelectedDoctor(null)}>
                  ← Back to Doctors
                </button>

                <div className="selected-doctor-info">
                  <h2>Book Appointment with Dr. {selectedDoctor.fullName}</h2>
                  <p>{selectedDoctor.specialization}</p>
                  <p className="fee">Consultation Fee: ₹{selectedDoctor.consultationFee}</p>
                </div>

                <div className="form-grid">
                  <div className="form-group">
                    <label>Select Date *</label>
                    <input
                      type="date"
                      name="appointmentDate"
                      value={bookingForm.appointmentDate}
                      onChange={handleBookingChange}
                      min={new Date().toISOString().split('T')[0]}
                    />
                  </div>

                  <div className="form-group">
                    <label>Select Time Slot *</label>
                    <select
                      name="timeSlot"
                      value={bookingForm.timeSlot}
                      onChange={handleBookingChange}
                    >
                      <option value="">Choose time</option>
                      {selectedDoctor.availableSlots?.split(',').map((slot, idx) => (
                        <option key={idx} value={slot.trim()}>{slot.trim()}</option>
                      ))}
                      {!selectedDoctor.availableSlots && (
                        <>
                          <option value="09:00">09:00 AM</option>
                          <option value="10:00">10:00 AM</option>
                          <option value="11:00">11:00 AM</option>
                          <option value="14:00">02:00 PM</option>
                          <option value="15:00">03:00 PM</option>
                          <option value="16:00">04:00 PM</option>
                        </>
                      )}
                    </select>
                  </div>

                  <div className="form-group col-2">
                    <label>Symptoms</label>
                    <textarea
                      name="symptoms"
                      value={bookingForm.symptoms}
                      onChange={handleBookingChange}
                      placeholder="Describe your symptoms..."
                      rows="3"
                    />
                  </div>

                  <div className="form-group col-2">
                    <label>Additional Notes</label>
                    <textarea
                      name="notes"
                      value={bookingForm.notes}
                      onChange={handleBookingChange}
                      placeholder="Any additional information..."
                      rows="3"
                    />
                  </div>
                </div>

                <div className="form-actions">
                  <button
                    className="btn-primary"
                    onClick={bookAppointment}
                    disabled={loading}
                  >
                    {loading ? 'Booking...' : 'Confirm Booking'}
                  </button>
                  <button
                    className="btn-secondary"
                    onClick={() => setSelectedDoctor(null)}
                  >
                    Cancel
                  </button>
                </div>
              </div>
            )}
          </div>
        )}

        {/* My Appointments */}
        {activeSection === 'appointments' && (
          <div className="appointments-section">
            {appointments.length === 0 ? (
              <div className="no-data">
                <p>No appointments yet</p>
                <button className="btn-primary" onClick={() => setActiveSection('bookAppointment')}>
                  Book Your First Appointment
                </button>
              </div>
            ) : (
              <div className="appointments-table-container">
                <table className="appointments-table">
                  <thead>
                    <tr>
                      <th>Doctor</th>
                      <th>Specialization</th>
                      <th>Date & Time</th>
                      <th>Status</th>
                      <th>Remarks</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {appointments.map(appt => (
                      <tr key={appt.id}>
                        <td>{appt.doctor?.fullName}</td>
                        <td>{appt.doctor?.specialization}</td>
                        <td>
                          <div className="date-info">
                            <FiClock /> {new Date(appt.appointmentDate).toLocaleString()}
                            {appt.rescheduledDate && (
                              <div className="rescheduled-tag">
                                <FiAlertCircle /> Rescheduled: {new Date(appt.rescheduledDate).toLocaleString()}
                              </div>
                            )}
                          </div>
                        </td>
                        <td>
                          <span className={badgeClass(appt.status)}>{appt.status}</span>
                        </td>
                        <td>
                          {appt.doctorRemarks ? (
                            <span className="remarks-text">{appt.doctorRemarks}</span>
                          ) : (
                            <span className="no-remarks">-</span>
                          )}
                        </td>
                        <td>
                          {appt.status === 'SCHEDULED' || appt.status === 'ACCEPTED' ? (
                            <button
                              className="btn-danger small"
                              onClick={() => cancelAppointment(appt.id)}
                            >
                              Cancel
                            </button>
                          ) : appt.status === 'COMPLETED' ? (
                            feedbackByAppointment.has(appt.id) ? (
                              <span className="action-disabled">Feedback Submitted</span>
                            ) : (
                              <button
                                className="btn-primary small"
                                onClick={() => {
                                  setFeedbackForm(prev => ({ ...prev, appointmentId: String(appt.id) }));
                                  setActiveSection('feedback');
                                }}
                              >
                                Give Feedback
                              </button>
                            )
                          ) : (
                            <span className="action-disabled">-</span>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}

        {activeSection === 'feedback' && (
          <div className="feedback-section">
            <div className="feedback-form-card">
              <h3>Share Your Consultation Feedback</h3>
              <div className="form-grid">
                <div className="form-group col-2">
                  <label>Completed Appointment</label>
                  <select
                    value={feedbackForm.appointmentId}
                    onChange={(e) => setFeedbackForm({ ...feedbackForm, appointmentId: e.target.value })}
                  >
                    <option value="">Select appointment</option>
                    {completedAppointments.map(appt => (
                      <option key={appt.id} value={appt.id}>
                        #{appt.id} - {appt.doctor?.fullName} ({new Date(appt.appointmentDate).toLocaleString()})
                      </option>
                    ))}
                  </select>
                </div>

                <div className="form-group">
                  <label>Rating (1-5)</label>
                  <select
                    value={feedbackForm.rating}
                    onChange={(e) => setFeedbackForm({ ...feedbackForm, rating: Number(e.target.value) })}
                  >
                    <option value={5}>5 - Excellent</option>
                    <option value={4}>4 - Good</option>
                    <option value={3}>3 - Average</option>
                    <option value={2}>2 - Poor</option>
                    <option value={1}>1 - Very Poor</option>
                  </select>
                </div>

                <div className="form-group col-2">
                  <label>Comments</label>
                  <textarea
                    rows="4"
                    value={feedbackForm.comments}
                    onChange={(e) => setFeedbackForm({ ...feedbackForm, comments: e.target.value })}
                    placeholder="Share your experience with the consultation"
                  />
                </div>
              </div>

              <button className="btn-primary" onClick={submitFeedback} disabled={loading}>
                {loading ? 'Submitting...' : 'Submit Feedback'}
              </button>
            </div>

            <div className="feedback-list-card">
              <h3>My Submitted Feedback</h3>
              {feedbackList.length === 0 ? (
                <p className="no-data">No feedback submitted yet</p>
              ) : (
                <div className="feedback-items">
                  {feedbackList.map(item => (
                    <div key={item.id} className="feedback-item">
                      <div className="feedback-item-header">
                        <strong>{item.doctorName || 'Doctor'}</strong>
                        <span className="feedback-rating">Rating: {item.rating}/5</span>
                      </div>
                      {item.comments && <p>{item.comments}</p>}
                      <small>{new Date(item.createdAt).toLocaleString()}</small>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        )}

        {/* Medical Records */}
        {activeSection === 'medicalRecords' && (
          <div className="medical-records-section">
            <div className="records-grid">
              <div className="records-list">
                <h3>Medical Records</h3>
                {medicalRecords.length === 0 ? (
                  <p className="no-data">No medical records available</p>
                ) : (
                  medicalRecords.map(record => (
                    <div key={record.id} className="record-item">
                      <div className="record-header">
                        <FiFileText className="record-icon" />
                        <div>
                          <h4>{record.recordType}</h4>
                          <p>{new Date(record.recordDate).toLocaleDateString()}</p>
                        </div>
                      </div>
                      <p className="record-doctor">By: {record.doctorName || 'N/A'}</p>
                      {record.notes && <p className="record-notes">{record.notes}</p>}
                      {record.filePath && (
                        <a href={`/${record.filePath}`} download className="download-link">
                          <FiDownload /> Download Report
                        </a>
                      )}
                    </div>
                  ))
                )}
              </div>

              <div className="prescriptions-list">
                <h3>Prescriptions</h3>
                {prescriptions.length === 0 ? (
                  <p className="no-data">No prescriptions available</p>
                ) : (
                  prescriptions.map(presc => (
                    <div key={presc.id} className="prescription-item">
                      <div className="prescription-header">
                        <h4>{presc.appointment?.doctor?.fullName}</h4>
                        <p>{new Date(presc.createdAt).toLocaleDateString()}</p>
                      </div>
                      <p className="diagnosis"><strong>Diagnosis:</strong> {presc.diagnosis}</p>
                      <button
                        className="btn-secondary small"
                        onClick={() => setSelectedPrescription(presc)}
                      >
                        View Details
                      </button>
                    </div>
                  ))
                )}
              </div>
            </div>
          </div>
        )}

        {/* Calendar */}
        {activeSection === 'calendar' && (
          <div className="calendar-section">
            <div className="calendar-container">
              <Calendar
                onChange={setSelectedDate}
                value={selectedDate}
                tileClassName={tileClassName}
                className="patient-calendar"
              />
              <div className="calendar-legend">
                <div className="legend-item">
                  <span className="legend-dot scheduled"></span> Scheduled
                </div>
                <div className="legend-item">
                  <span className="legend-dot accepted"></span> Accepted
                </div>
                <div className="legend-item">
                  <span className="legend-dot rejected"></span> Rejected
                </div>
                <div className="legend-item">
                  <span className="legend-dot rescheduled"></span> Rescheduled
                </div>
                <div className="legend-item">
                  <span className="legend-dot completed"></span> Completed
                </div>
              </div>
            </div>

            <div className="selected-date-appointments">
              <h3>Appointments on {selectedDate.toLocaleDateString()}</h3>
              {appointmentsOnSelectedDate.length === 0 ? (
                <p className="no-data">No appointments on this date</p>
              ) : (
                appointmentsOnSelectedDate.map(appt => (
                  <div key={appt.id} className="appointment-detail">
                    <div className="appointment-time">
                      <FiClock />
                      <span>{new Date(appt.appointmentDate).toLocaleTimeString()}</span>
                    </div>
                    <h4>{appt.doctor?.fullName}</h4>
                    <p>{appt.doctor?.specialization}</p>
                    <span className={badgeClass(appt.status)}>{appt.status}</span>
                    {appt.doctorRemarks && (
                      <p className="remarks"><strong>Remarks:</strong> {appt.doctorRemarks}</p>
                    )}
                  </div>
                ))
              )}
            </div>
          </div>
        )}

        {/* Edit Profile */}
        {activeSection === 'profile' && (
          <div className="profile-section">
            <div className="profile-image-section">
              <div className="profile-image-preview">
                {profilePreview || profile?.profileImagePath ? (
                  <img
                    src={profilePreview || `/${profile.profileImagePath}`}
                    alt="Profile"
                  />
                ) : (
                  <div className="image-placeholder">
                    <FiUser size={80} />
                  </div>
                )}
              </div>
              <div className="image-upload-controls">
                <input
                  type="file"
                  accept="image/*"
                  onChange={handleImageChange}
                  id="profileImageInput"
                  style={{ display: 'none' }}
                />
                <label htmlFor="profileImageInput" className="btn-secondary">
                  Choose Image
                </label>
                {profileImage && (
                  <button className="btn-primary" onClick={uploadProfileImage}>
                    Upload Image
                  </button>
                )}
              </div>
            </div>

            <div className="profile-form">
              <div className="form-grid">
                <div className="form-group">
                  <label>Full Name *</label>
                  <input
                    type="text"
                    name="fullName"
                    value={formData.fullName || ''}
                    onChange={handleInputChange}
                  />
                </div>

                <div className="form-group">
                  <label>Age</label>
                  <input
                    type="number"
                    name="age"
                    value={formData.age || ''}
                    onChange={handleInputChange}
                  />
                </div>

                <div className="form-group">
                  <label>Gender</label>
                  <select
                    name="gender"
                    value={formData.gender || ''}
                    onChange={handleInputChange}
                  >
                    <option value="">Select</option>
                    <option value="Male">Male</option>
                    <option value="Female">Female</option>
                    <option value="Other">Other</option>
                  </select>
                </div>

                <div className="form-group">
                  <label>Blood Group</label>
                  <input
                    type="text"
                    name="bloodGroup"
                    value={formData.bloodGroup || ''}
                    onChange={handleInputChange}
                    placeholder="e.g., O+"
                  />
                </div>

                <div className="form-group">
                  <label>Phone</label>
                  <input
                    type="tel"
                    name="phone"
                    value={formData.phone || ''}
                    onChange={handleInputChange}
                  />
                </div>

                <div className="form-group">
                  <label>Emergency Contact</label>
                  <input
                    type="tel"
                    name="emergencyContact"
                    value={formData.emergencyContact || ''}
                    onChange={handleInputChange}
                  />
                </div>

                <div className="form-group col-2">
                  <label>Address</label>
                  <textarea
                    name="address"
                    value={formData.address || ''}
                    onChange={handleInputChange}
                    rows="3"
                  />
                </div>

                <div className="form-group col-2">
                  <label>Medical History</label>
                  <textarea
                    name="medicalHistory"
                    value={formData.medicalHistory || ''}
                    onChange={handleInputChange}
                    rows="4"
                    placeholder="Allergies, chronic conditions, past surgeries, etc."
                  />
                </div>
              </div>

              <button
                className="btn-primary"
                onClick={saveProfile}
                disabled={loading}
              >
                {loading ? 'Saving...' : 'Save Profile'}
              </button>
            </div>
          </div>
        )}
      </motion.div>

      {selectedPrescription && (
        <PrescriptionView
          prescription={selectedPrescription}
          onClose={() => setSelectedPrescription(null)}
        />
      )}
    </motion.div>
  );
}
