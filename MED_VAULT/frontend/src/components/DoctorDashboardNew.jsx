import React, { useEffect, useMemo, useState } from 'react';
import axios from 'axios';
import { motion } from 'framer-motion';
import Calendar from 'react-calendar';
import {
  FiHome,
  FiCalendar,
  FiUsers,
  FiFileText,
  FiSettings,
  FiLogOut,
  FiCheckCircle,
  FiClock,
  FiXCircle
} from 'react-icons/fi';
import { FaStethoscope, FaRobot, FaCalendarAlt, FaHeartbeat } from 'react-icons/fa';
import PrescriptionView from './PrescriptionView';
import 'react-calendar/dist/Calendar.css';
import '../styles/doctor.css';

const navItems = [
  { key: 'dashboard', label: 'Dashboard', icon: FiHome },
  { key: 'appointments', label: 'Appointments', icon: FiFileText },
  { key: 'patients', label: 'Patients', icon: FiUsers },
  { key: 'calendar', label: 'Calendar', icon: FiCalendar },
  { key: 'profile', label: 'Edit Profile', icon: FiSettings }
];

export default function DoctorDashboard() {
  const token = localStorage.getItem('token');
  const [activeSection, setActiveSection] = useState('dashboard');
  const [profile, setProfile] = useState(null);
  const [appointments, setAppointments] = useState([]);
  const [patients, setPatients] = useState([]);
  const [selectedPatient, setSelectedPatient] = useState(null);
  const [patientRecords, setPatientRecords] = useState([]);
  const [selectedDate, setSelectedDate] = useState(new Date());
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);
  const [profileImage, setProfileImage] = useState(null);
  const [profilePreview, setProfilePreview] = useState('');
  const [showReschedule, setShowReschedule] = useState(false);
  const [rescheduleTarget, setRescheduleTarget] = useState(null);
  const [decisionRemarks, setDecisionRemarks] = useState('');
  const [rescheduleDate, setRescheduleDate] = useState('');
  const [recordNotes, setRecordNotes] = useState('');
  const [recordFile, setRecordFile] = useState(null);
  const [selectedAppointment, setSelectedAppointment] = useState(null);
  const [selectedPatientAppointmentId, setSelectedPatientAppointmentId] = useState('');
  const [selectedPrescription, setSelectedPrescription] = useState(null);
  const [showPrescriptionForm, setShowPrescriptionForm] = useState(false);
  const [prescriptionForm, setPrescriptionForm] = useState({
    diagnosis: '',
    medicinesJson: '[]',
    testsRecommended: '',
    followUpDate: '',
    notes: ''
  });
  const [formData, setFormData] = useState({
    fullName: '',
    specialization: '',
    qualification: '',
    experienceYears: '',
    consultationFee: '',
    availableSlots: '',
    hospitalName: '',
    phone: '',
    bio: ''
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
    fetchPatients();
  }, []);

  useEffect(() => {
    if (selectedPatient?.userId) {
      fetchPatientRecords(selectedPatient.userId);
      setRecordNotes('');
      setRecordFile(null);
      setSelectedPatientAppointmentId('');
    }
  }, [selectedPatient]);

  const isProfileComplete = useMemo(() => {
    if (!profile) return false;
    return Boolean(
      profile.fullName &&
      profile.qualification &&
      profile.specialization &&
      profile.experienceYears &&
      profile.consultationFee &&
      profile.availableSlots &&
      profile.bio
    );
  }, [profile]);

  useEffect(() => {
    if (!isProfileComplete) {
      setActiveSection('profile');
    }
  }, [isProfileComplete]);

  const fetchProfile = async () => {
    try {
      const res = await axios.get('/api/doctor/profile', { headers: authHeader });
      setProfile(res.data);
      setFormData({
        fullName: res.data.fullName || '',
        specialization: res.data.specialization || '',
        qualification: res.data.qualification || '',
        experienceYears: res.data.experienceYears || '',
        consultationFee: res.data.consultationFee || '',
        availableSlots: res.data.availableSlots || '',
        hospitalName: res.data.hospitalName || '',
        phone: res.data.phone || '',
        bio: res.data.bio || ''
      });
      if (res.data.profileImagePath) {
        setProfilePreview('/api/doctor/profile/image');
      }
    } catch (err) {
      console.error('Profile fetch error', err);
    }
  };

  const fetchAppointments = async () => {
    try {
      const res = await axios.get('/api/doctor/appointments', { headers: authHeader });
      setAppointments(res.data || []);
    } catch (err) {
      console.error('Appointments fetch error', err);
    }
  };

  const fetchPatients = async () => {
    try {
      const res = await axios.get('/api/doctor/patients', { headers: authHeader });
      setPatients(res.data || []);
    } catch (err) {
      console.error('Patients fetch error', err);
    }
  };

  const fetchPatientRecords = async (patientUserId) => {
    try {
      const res = await axios.get(`/api/doctor/patients/${patientUserId}/records`, { headers: authHeader });
      setPatientRecords(res.data || []);
    } catch (err) {
      console.error('Record fetch error', err);
    }
  };

  const saveProfile = async () => {
    setLoading(true);
    setMessage('');
    try {
      const res = await axios.put('/api/doctor/profile', {
        ...formData,
        experienceYears: formData.experienceYears ? parseInt(formData.experienceYears, 10) : null
      }, { headers: authHeader });
      setProfile(res.data);
      const storedUser = (() => {
        try { return JSON.parse(localStorage.getItem('user') || 'null'); } catch (e) { return null; }
      })();
      if (storedUser) {
        storedUser.displayName = res.data.fullName;
        localStorage.setItem('user', JSON.stringify(storedUser));
        window.dispatchEvent(new Event('userUpdated'));
      }
      setMessage('Profile saved successfully');
    } catch (err) {
      const serverMsg = err?.response?.data || 'Error saving profile';
      setMessage(typeof serverMsg === 'string' ? serverMsg : 'Error saving profile');
    } finally {
      setLoading(false);
    }
  };

  const uploadProfileImage = async () => {
    if (!profileImage) return;
    const data = new FormData();
    data.append('file', profileImage);
    try {
      const res = await axios.post('/api/doctor/profile/image', data, {
        headers: { ...authHeader, 'Content-Type': 'multipart/form-data' }
      });
      setProfile(res.data);
      setProfilePreview(`/api/doctor/profile/image?ts=${Date.now()}`);
    } catch (err) {
      setMessage('Failed to upload image');
    }
  };

  const handleDecision = async (appointmentId, action) => {
    setLoading(true);
    setMessage('');
    try {
      if (action === 'accept') {
        await axios.put(`/api/doctor/appointments/${appointmentId}/accept`, {}, { headers: authHeader });
      }
      if (action === 'reject') {
        await axios.put(
          `/api/doctor/appointments/${appointmentId}/reject`,
          { remarks: decisionRemarks },
          { headers: authHeader }
        );
      }
      if (action === 'reschedule') {
        if (!rescheduleDate) {
          setMessage('Please choose a new date and time');
          setLoading(false);
          return;
        }
        await axios.put(
          `/api/doctor/appointments/${appointmentId}/reschedule`,
          { appointmentDate: rescheduleDate, remarks: decisionRemarks },
          { headers: authHeader }
        );
      }
      setDecisionRemarks('');
      setRescheduleDate('');
      setShowReschedule(false);
      setRescheduleTarget(null);
      setMessage('Appointment updated');
      fetchAppointments();
    } catch (err) {
      setMessage('Failed to update appointment');
    } finally {
      setLoading(false);
    }
  };

  const addMedicalRecord = async () => {
    if (!selectedPatient) return;
    const data = new FormData();
    if (recordNotes) data.append('notes', recordNotes);
    if (recordFile) data.append('file', recordFile);
    try {
      const res = await axios.post(
        `/api/doctor/patients/${selectedPatient.userId}/records`,
        data,
        { headers: { ...authHeader, 'Content-Type': 'multipart/form-data' } }
      );
      setPatientRecords((prev) => [res.data, ...prev]);
      setRecordNotes('');
      setRecordFile(null);
    } catch (err) {
      setMessage('Failed to upload medical note');
    }
  };

  const savePrescription = async () => {
    if (!selectedAppointment) return;
    setLoading(true);
    try {
      const res = await axios.post(
        `/api/doctor/appointments/${selectedAppointment.id}/prescriptions`,
        prescriptionForm,
        { headers: authHeader }
      );
      setSelectedPrescription(res.data);
      setShowPrescriptionForm(false);
      setPrescriptionForm({
        diagnosis: '',
        medicinesJson: '[]',
        testsRecommended: '',
        followUpDate: '',
        notes: ''
      });
      fetchAppointments();
    } catch (err) {
      setMessage('Error saving prescription');
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateStr) =>
    new Date(dateStr).toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });

  const badgeClass = (status) => status.toLowerCase().replace(/_/g, '-');

  const dayAppointments = useMemo(
    () => appointments.filter((apt) => new Date(apt.appointmentDate).toDateString() === selectedDate.toDateString()),
    [appointments, selectedDate]
  );

  const patientAppointments = useMemo(() => {
    if (!selectedPatient) return [];
    return appointments.filter((apt) => apt.patient?.id === selectedPatient.profileId);
  }, [appointments, selectedPatient]);

  const tileClassName = ({ date }) => {
    const dayList = appointments.filter(
      (apt) => new Date(apt.appointmentDate).toDateString() === date.toDateString()
    );
    const hasAccepted = dayList.some((apt) => ['ACCEPTED', 'COMPLETED'].includes(apt.status));
    const hasRejected = dayList.some((apt) => ['REJECTED', 'CANCELLED', 'NO_SHOW'].includes(apt.status));
    const hasPending = dayList.some((apt) => ['SCHEDULED', 'RESCHEDULED'].includes(apt.status));
    if (hasRejected) return 'doctor-rejected';
    if (hasAccepted) return 'doctor-accepted';
    if (hasPending) return 'doctor-pending';
    return '';
  };

  const pendingAppointments = appointments.filter((apt) => apt.status === 'SCHEDULED');
  const acceptedAppointments = appointments.filter((apt) => apt.status === 'ACCEPTED' || apt.status === 'COMPLETED');
  const rejectedAppointments = appointments.filter((apt) => apt.status === 'REJECTED');

  const sectionTitle = navItems.find((item) => item.key === activeSection)?.label || 'Dashboard';

  return (
    <div className="doctor-shell">
      <aside className="doctor-sidebar">
        <div className="doctor-brand">
          <FaStethoscope size={26} />
          <div>
            <h3>Doctor Desk</h3>
            <p>Care operations hub</p>
          </div>
        </div>

        <div className="doctor-nav">
          {navItems.map((item) => {
            const Icon = item.icon;
            return (
              <button
                key={item.key}
                className={activeSection === item.key ? 'active' : ''}
                onClick={() => setActiveSection(item.key)}
              >
                <Icon size={18} />
                <span>{item.label}</span>
              </button>
            );
          })}
          <button className="logout" onClick={handleLogout}>
            <FiLogOut size={18} />
            <span>Logout</span>
          </button>
        </div>

        <div className="doctor-sidebar-footer">
          <div className="doctor-float-icons">
            <FaRobot className="doctor-float" />
            <FaCalendarAlt className="doctor-float delay" />
            <FaHeartbeat className="doctor-float delay-2" />
          </div>
          <p>Precision care, human touch.</p>
        </div>
      </aside>

      <main className="doctor-main">
        <header className="doctor-header">
          <div>
            <h1>{sectionTitle}</h1>
            <p className="muted">Welcome back, Dr. {profile?.fullName || 'Doctor'}.</p>
          </div>
          <div className="doctor-profile-chip">
            {profilePreview ? (
              <img src={profilePreview} alt="Doctor" />
            ) : (
              <div className="doctor-profile-placeholder">D</div>
            )}
            <div>
              <strong>{profile?.fullName || 'Doctor'}</strong>
              <span>{profile?.specialization || 'Specialist'}</span>
            </div>
          </div>
        </header>

        {message && <div className="doctor-message">{message}</div>}

        {activeSection === 'dashboard' && (
          <section className="doctor-section">
            <div className="doctor-grid doctor-grid-2">
              {[
                { label: 'Pending Requests', value: pendingAppointments.length, icon: FiClock },
                { label: 'Accepted', value: acceptedAppointments.length, icon: FiCheckCircle },
                { label: 'Rejected', value: rejectedAppointments.length, icon: FiXCircle },
                { label: 'Total Appointments', value: appointments.length, icon: FiFileText }
              ].map((card, idx) => {
                const Icon = card.icon;
                return (
                  <motion.div
                    key={card.label}
                    className="doctor-card"
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ delay: idx * 0.05 }}
                  >
                    <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                      <div className="stat-icon">
                        <Icon size={18} />
                      </div>
                      <div>
                        <p>{card.label}</p>
                        <h3>{card.value}</h3>
                      </div>
                    </div>
                  </motion.div>
                );
              })}
            </div>

            <div className="doctor-card">
              <h2>Quick Alerts</h2>
              <p className="muted">Use appointments to accept, reject, or reschedule patient requests.</p>
            </div>
          </section>
        )}

        {activeSection === 'appointments' && (
          <section className="doctor-section">
            <div className="doctor-card">
              <h2>Appointment Requests</h2>
              <div className="table-responsive">
                <table className="doctor-table">
                  <thead>
                    <tr>
                      <th>Patient</th>
                      <th>Date & Time</th>
                      <th>Status</th>
                      <th>Remarks</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {appointments.map((apt) => (
                      <tr key={apt.id}>
                        <td>{apt.patient?.fullName}</td>
                        <td>{formatDate(apt.appointmentDate)}</td>
                        <td>
                          <span className={`badge badge-${badgeClass(apt.status)}`}>
                            {apt.status}
                          </span>
                        </td>
                        <td>{apt.doctorRemarks || '-'}</td>
                        <td>
                          <div className="doctor-actions">
                            <button className="primary" onClick={() => handleDecision(apt.id, 'accept')}>
                              Accept
                            </button>
                            <button
                              className="danger"
                              onClick={() => {
                                setDecisionRemarks('');
                                setRescheduleTarget(apt);
                                setShowReschedule(true);
                              }}
                            >
                              Reject
                            </button>
                            <button
                              className="ghost"
                              onClick={() => {
                                setRescheduleTarget(apt);
                                setShowReschedule(true);
                              }}
                            >
                              Reschedule
                            </button>
                            <button
                              className="ghost"
                              onClick={() => {
                                setSelectedAppointment(apt);
                                setShowPrescriptionForm(true);
                              }}
                            >
                              Prescription
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </section>
        )}

        {activeSection === 'patients' && (
          <section className="doctor-section">
            <div className="doctor-grid doctor-grid-2">
              <div className="doctor-card">
                <h2>Patients</h2>
                <div className="table-responsive">
                  <table className="doctor-table">
                    <thead>
                      <tr>
                        <th>Name</th>
                        <th>Age</th>
                        <th>Phone</th>
                        <th>Action</th>
                      </tr>
                    </thead>
                    <tbody>
                      {patients.map((patient) => (
                        <tr key={patient.userId}>
                          <td>{patient.fullName}</td>
                          <td>{patient.age || '-'}</td>
                          <td>{patient.phone || '-'}</td>
                          <td>
                            <button className="ghost" onClick={() => setSelectedPatient(patient)}>
                              View
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>

              <div className="doctor-card">
                <h2>Patient Details</h2>
                {selectedPatient ? (
                  <div className="doctor-note-list">
                    <div>
                      <strong>{selectedPatient.fullName}</strong>
                      <p className="muted">{selectedPatient.email}</p>
                      <p>Medical History: {selectedPatient.medicalHistory || 'Not provided'}</p>
                    </div>
                    <div>
                      <h4>Medical Notes</h4>
                      {patientRecords.map((record) => (
                        <div key={record.id} className="doctor-note-item">
                          <strong>{record.doctorName || 'Doctor'}</strong>
                          <p>{record.content || 'File uploaded'}</p>
                          {record.filename && <p>File: {record.filename}</p>}
                        </div>
                      ))}
                    </div>
                    <div>
                      <h4>Add Diagnosis / Notes</h4>
                      <textarea
                        className="form-textarea"
                        value={recordNotes}
                        onChange={(e) => setRecordNotes(e.target.value)}
                        placeholder="Add diagnosis or medical notes"
                      />
                      <input type="file" onChange={(e) => setRecordFile(e.target.files?.[0])} />
                      <button className="primary" onClick={addMedicalRecord}>Save Notes</button>
                    </div>
                    <div>
                      <h4>Add Prescription</h4>
                      <select
                        className="form-select"
                        value={selectedPatientAppointmentId}
                        onChange={(e) => setSelectedPatientAppointmentId(e.target.value)}
                      >
                        <option value="">Select appointment</option>
                        {patientAppointments.map((apt) => (
                          <option key={apt.id} value={apt.id}>
                            {formatDate(apt.appointmentDate)} ({apt.status})
                          </option>
                        ))}
                      </select>
                      <button
                        className="primary"
                        onClick={() => {
                          const apt = patientAppointments.find((item) => String(item.id) === selectedPatientAppointmentId);
                          if (apt) {
                            setSelectedAppointment(apt);
                            setShowPrescriptionForm(true);
                          }
                        }}
                      >
                        Add Prescription
                      </button>
                    </div>
                  </div>
                ) : (
                  <p className="muted">Select a patient to view details.</p>
                )}
              </div>
            </div>
          </section>
        )}

        {activeSection === 'calendar' && (
          <section className="doctor-section">
            <div className="doctor-calendar-grid">
              <div className="doctor-calendar-card">
                <Calendar value={selectedDate} onChange={setSelectedDate} tileClassName={tileClassName} />
                <div className="doctor-calendar-legend">
                  <span className="doctor-legend accepted">Accepted</span>
                  <span className="doctor-legend pending">Pending</span>
                  <span className="doctor-legend rejected">Rejected</span>
                </div>
                <p className="muted" style={{ marginTop: 12 }}>
                  Available slots: {profile?.availableSlots || 'Not set'}
                </p>
              </div>
              <div className="doctor-card">
                <h3>{selectedDate.toDateString()}</h3>
                {dayAppointments.length === 0 ? (
                  <p className="muted">No appointments booked.</p>
                ) : (
                  dayAppointments.map((apt) => (
                    <div key={apt.id} className="doctor-note-item">
                      <strong>{apt.patient?.fullName}</strong>
                      <p>{formatDate(apt.appointmentDate)}</p>
                      <span className={`badge badge-${badgeClass(apt.status)}`}>{apt.status}</span>
                    </div>
                  ))
                )}
              </div>
            </div>
          </section>
        )}

        {activeSection === 'profile' && (
          <section className="doctor-section">
            <div className="doctor-card">
              <h2>Edit Profile</h2>
              <div className="doctor-grid doctor-grid-2">
                <div>
                  {profilePreview ? (
                    <img src={profilePreview} alt="Doctor" className="doctor-profile-image-large" />
                  ) : (
                    <div className="doctor-profile-placeholder">D</div>
                  )}
                  <input
                    type="file"
                    accept="image/*"
                    onChange={(e) => {
                      const file = e.target.files?.[0];
                      if (file) {
                        setProfileImage(file);
                        setProfilePreview(URL.createObjectURL(file));
                      }
                    }}
                  />
                  <button className="primary" onClick={uploadProfileImage}>Upload Image</button>
                </div>
                <div className="doctor-profile-form">
                  <label>
                    Full Name
                    <input
                      value={formData.fullName}
                      onChange={(e) => setFormData((prev) => ({ ...prev, fullName: e.target.value }))}
                    />
                  </label>
                  <label>
                    Specialization
                    <input
                      value={formData.specialization}
                      onChange={(e) => setFormData((prev) => ({ ...prev, specialization: e.target.value }))}
                    />
                  </label>
                  <label>
                    Qualification
                    <input
                      value={formData.qualification}
                      onChange={(e) => setFormData((prev) => ({ ...prev, qualification: e.target.value }))}
                    />
                  </label>
                  <label>
                    Experience (Years)
                    <input
                      type="number"
                      value={formData.experienceYears}
                      onChange={(e) => setFormData((prev) => ({ ...prev, experienceYears: e.target.value }))}
                    />
                  </label>
                  <label>
                    Consultation Fee
                    <input
                      type="number"
                      value={formData.consultationFee}
                      onChange={(e) => setFormData((prev) => ({ ...prev, consultationFee: e.target.value }))}
                    />
                  </label>
                  <label>
                    Available Time Slots
                    <textarea
                      value={formData.availableSlots}
                      onChange={(e) => setFormData((prev) => ({ ...prev, availableSlots: e.target.value }))}
                    />
                  </label>
                  <label>
                    Bio / Description
                    <textarea
                      value={formData.bio}
                      onChange={(e) => setFormData((prev) => ({ ...prev, bio: e.target.value }))}
                    />
                  </label>
                  <button onClick={saveProfile} disabled={loading}>
                    {loading ? 'Saving...' : 'Save Profile'}
                  </button>
                </div>
              </div>
            </div>
          </section>
        )}
      </main>

      {showReschedule && rescheduleTarget && (
        <div className="doctor-modal" onClick={() => setShowReschedule(false)}>
          <div className="doctor-modal-card" onClick={(e) => e.stopPropagation()}>
            <h3>Reschedule / Reject</h3>
            <label>
              New Date & Time
              <input
                type="datetime-local"
                value={rescheduleDate}
                onChange={(e) => setRescheduleDate(e.target.value)}
              />
            </label>
            <label>
              Remarks
              <textarea
                value={decisionRemarks}
                onChange={(e) => setDecisionRemarks(e.target.value)}
                placeholder="Add remarks"
              />
            </label>
            <div className="doctor-actions">
              <button
                className="primary"
                onClick={() => handleDecision(rescheduleTarget.id, 'reschedule')}
              >
                Reschedule
              </button>
              <button
                className="danger"
                onClick={() => handleDecision(rescheduleTarget.id, 'reject')}
              >
                Reject
              </button>
              <button className="ghost" onClick={() => setShowReschedule(false)}>Cancel</button>
            </div>
          </div>
        </div>
      )}

      {showPrescriptionForm && selectedAppointment && (
        <div className="doctor-modal" onClick={() => setShowPrescriptionForm(false)}>
          <div className="doctor-modal-card" onClick={(e) => e.stopPropagation()}>
            <h3>Add Prescription</h3>
            <label>
              Diagnosis
              <textarea
                value={prescriptionForm.diagnosis}
                onChange={(e) => setPrescriptionForm({ ...prescriptionForm, diagnosis: e.target.value })}
              />
            </label>
            <label>
              Tests Recommended
              <input
                value={prescriptionForm.testsRecommended}
                onChange={(e) => setPrescriptionForm({ ...prescriptionForm, testsRecommended: e.target.value })}
              />
            </label>
            <label>
              Follow-up Date
              <input
                type="date"
                value={prescriptionForm.followUpDate}
                onChange={(e) => setPrescriptionForm({ ...prescriptionForm, followUpDate: e.target.value })}
              />
            </label>
            <label>
              Notes
              <textarea
                value={prescriptionForm.notes}
                onChange={(e) => setPrescriptionForm({ ...prescriptionForm, notes: e.target.value })}
              />
            </label>
            <div className="doctor-actions">
              <button className="primary" onClick={savePrescription}>Save</button>
              <button className="ghost" onClick={() => setShowPrescriptionForm(false)}>Cancel</button>
            </div>
          </div>
        </div>
      )}

      {selectedPrescription && (
        <PrescriptionView
          prescription={selectedPrescription}
          onClose={() => setSelectedPrescription(null)}
        />
      )}

      {!isProfileComplete && activeSection !== 'profile' && (
        <div className="doctor-overlay">
          <div className="doctor-overlay-card">
            <h3>Complete Your Profile</h3>
            <p>Please fill in your professional details before managing appointments.</p>
            <button className="primary" onClick={() => setActiveSection('profile')}>Go to Profile</button>
          </div>
        </div>
      )}
    </div>
  );
}
