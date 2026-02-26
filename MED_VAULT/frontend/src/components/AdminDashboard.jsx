import React, { useEffect, useMemo, useState } from 'react';
import axios from 'axios';
import { motion } from 'framer-motion';
import Calendar from 'react-calendar';
import {
  FiHome,
  FiUsers,
  FiUser,
  FiCalendar,
  FiFileText,
  FiSettings,
  FiLogOut,
  FiSearch,
  FiCheckCircle,
  FiClock,
  FiMenu,
  FiX
} from 'react-icons/fi';
import { FaRobot, FaUserMd, FaHospital } from 'react-icons/fa';
import 'react-calendar/dist/Calendar.css';
import AdminRescheduleForm from './AdminRescheduleForm';

const navItems = [
  { key: 'dashboard', label: 'Dashboard', icon: FiHome },
  { key: 'doctors', label: 'Doctors', icon: FaUserMd },
  { key: 'patients', label: 'Patients', icon: FiUsers },
  { key: 'appointments', label: 'Appointments', icon: FiFileText },
  { key: 'reports', label: 'Reports', icon: FiFileText },
  { key: 'calendar', label: 'Calendar', icon: FiCalendar },
  { key: 'profile', label: 'Edit Profile', icon: FiSettings }
];

export default function AdminDashboard({ onLogout }) {
  const token = localStorage.getItem('token');
  const [activeSection, setActiveSection] = useState('dashboard');
  const [stats, setStats] = useState(null);
  const [doctors, setDoctors] = useState([]);
  const [patients, setPatients] = useState([]);
  const [appointments, setAppointments] = useState([]);
  const [profile, setProfile] = useState(null);
  const [message, setMessage] = useState('');
  const [search, setSearch] = useState('');
  const [selectedDoctor, setSelectedDoctor] = useState(null);
  const [selectedPatient, setSelectedPatient] = useState(null);
  const [selectedDate, setSelectedDate] = useState(new Date());
  const [profileForm, setProfileForm] = useState({
    displayName: '',
    email: '',
    password: ''
  });
  const [profileImage, setProfileImage] = useState(null);
  const [profilePreview, setProfilePreview] = useState('');
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);

  useEffect(() => {
    fetchStats();
    fetchDoctors();
    fetchPatients();
    fetchAppointments();
    fetchProfile();
  }, []);

  const authHeader = useMemo(() => ({ Authorization: `Bearer ${token}` }), [token]);

  const fetchStats = async () => {
    try {
      const res = await axios.get('/api/admin/stats', { headers: authHeader });
      setStats(res.data);
    } catch (err) {
      console.error('Failed to fetch stats', err);
    }
  };

  const fetchDoctors = async () => {
    try {
      const res = await axios.get('/api/admin/doctors', { headers: authHeader });
      setDoctors(res.data || []);
    } catch (err) {
      console.error('Failed to fetch doctors', err);
    }
  };

  const fetchPatients = async () => {
    try {
      const res = await axios.get('/api/admin/patients', { headers: authHeader });
      setPatients(res.data || []);
    } catch (err) {
      console.error('Failed to fetch patients', err);
    }
  };

  const fetchAppointments = async () => {
    try {
      const res = await axios.get('/api/admin/appointments', { headers: authHeader });
      setAppointments(res.data || []);
    } catch (err) {
      console.error('Failed to fetch appointments', err);
    }
  };

  const fetchProfile = async () => {
    try {
      const res = await axios.get('/api/admin/profile', { headers: authHeader });
      setProfile(res.data);
      setProfileForm({
        displayName: res.data.displayName || '',
        email: res.data.email || '',
        password: ''
      });
      if (res.data.profileImagePath) {
        setProfilePreview('/api/admin/profile/image');
      }
    } catch (err) {
      console.error('Failed to fetch admin profile', err);
    }
  };

  const handleToggleDoctor = async (doctor) => {
    try {
      const res = await axios.put(
        `/api/admin/doctors/${doctor.id}/status`,
        { active: !doctor.active },
        { headers: authHeader }
      );
      setDoctors((prev) => prev.map((d) => (d.id === doctor.id ? res.data : d)));
    } catch (err) {
      console.error('Failed to update doctor status', err);
    }
  };

  const handleProfileSave = async () => {
    setMessage('');
    const formData = new FormData();
    if (profileForm.displayName) formData.append('displayName', profileForm.displayName);
    if (profileForm.email) formData.append('email', profileForm.email);
    if (profileForm.password) formData.append('password', profileForm.password);
    if (profileImage) formData.append('profileImage', profileImage);

    try {
      const res = await axios.post('/api/admin/profile', formData, {
        headers: { ...authHeader, 'Content-Type': 'multipart/form-data' }
      });
      setProfile(res.data);
      setProfileForm((prev) => ({ ...prev, password: '' }));
      if (res.data.profileImagePath) {
        setProfilePreview(`/api/admin/profile/image?ts=${Date.now()}`);
      }
      const storedUser = (() => {
        try {
          return JSON.parse(localStorage.getItem('user') || 'null');
        } catch (e) {
          return null;
        }
      })();
      if (storedUser) {
        storedUser.displayName = res.data.displayName;
        storedUser.email = res.data.email;
        storedUser.profileImagePath = res.data.profileImagePath;
        localStorage.setItem('user', JSON.stringify(storedUser));
        window.dispatchEvent(new Event('userUpdated'));
      }
      setMessage('Profile updated successfully');
    } catch (err) {
      const serverMsg = err?.response?.data || 'Failed to update profile';
      setMessage(typeof serverMsg === 'string' ? serverMsg : 'Failed to update profile');
    }
  };

  const filteredPatients = useMemo(() => {
    if (!search.trim()) return patients;
    const term = search.toLowerCase();
    return patients.filter((p) =>
      [p.fullName, p.email, p.phone, p.bloodGroup, p.gender]
        .filter(Boolean)
        .some((value) => value.toLowerCase().includes(term))
    );
  }, [patients, search]);

  const getAppointmentsForDate = (date) =>
    appointments.filter((apt) => new Date(apt.appointmentDate).toDateString() === date.toDateString());

  const getTileClass = (date) => {
    const dayAppointments = getAppointmentsForDate(date);
    const isToday = new Date().toDateString() === date.toDateString();
    const hasApproved = dayAppointments.some((apt) => apt.status === 'COMPLETED');
    const hasPending = dayAppointments.some((apt) => apt.status === 'SCHEDULED');

    if (isToday) return 'admin-calendar-today';
    if (hasApproved) return 'admin-calendar-approved';
    if (hasPending) return 'admin-calendar-pending';
    return '';
  };

  const formatDate = (dateStr) =>
    new Date(dateStr).toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });

  const sectionTitle = navItems.find((item) => item.key === activeSection)?.label || 'Dashboard';
  const handleSectionChange = (section) => {
    setActiveSection(section);
    setIsSidebarOpen(false);
  };

  return (
    <motion.div
      className="admin-shell"
      initial={{ opacity: 0, y: 16 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.45, ease: 'easeOut' }}
    >
      <div className="admin-bg-clouds" aria-hidden="true">
        <span className="admin-cloud cloud-1" />
        <span className="admin-cloud cloud-2" />
        <span className="admin-cloud cloud-3" />
      </div>

      <button
        className="admin-mobile-toggle"
        type="button"
        onClick={() => setIsSidebarOpen((prev) => !prev)}
        aria-label={isSidebarOpen ? 'Close sidebar' : 'Open sidebar'}
      >
        {isSidebarOpen ? <FiX size={20} /> : <FiMenu size={20} />}
      </button>

      <aside className={`admin-sidebar ${isSidebarOpen ? 'open' : ''}`}>
        <div className="admin-brand">
          <FaHospital className="brand-icon" />
          <div>
            <h3>MedVault Admin</h3>
            <p>Command Center</p>
          </div>
        </div>

        <nav className="admin-nav">
          {navItems.map((item) => {
            const Icon = item.icon;
            return (
              <motion.button
                key={item.key}
                type="button"
                className={`admin-nav-item ${activeSection === item.key ? 'active' : ''}`}
                onClick={() => handleSectionChange(item.key)}
                whileHover={{ x: 5 }}
                whileTap={{ scale: 0.98 }}
              >
                <Icon size={18} />
                <span>{item.label}</span>
              </motion.button>
            );
          })}
          <motion.button
            type="button"
            className="admin-nav-item logout"
            onClick={onLogout}
            whileHover={{ x: 5 }}
            whileTap={{ scale: 0.98 }}
          >
            <FiLogOut size={18} />
            <span>Logout</span>
          </motion.button>
        </nav>

        <div className="admin-sidebar-footer">
          <div className="floating-icons">
            <FaRobot className="admin-float" />
            <FaUserMd className="admin-float delay" />
            <FaHospital className="admin-float delay-2" />
          </div>
          <p>Secure. Responsive. Human.</p>
        </div>
      </aside>

      <motion.main
        className="admin-main"
        initial={{ opacity: 0, y: 12 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.4, delay: 0.1 }}
      >
        <header className="admin-header admin-glass-header">
          <div>
            <h1>{sectionTitle}</h1>
            <p className="muted">Real-time hospital intelligence and oversight.</p>
          </div>
          <div className="admin-profile-chip">
            {profilePreview ? (
              <img src={profilePreview} alt="Admin" />
            ) : (
              <div className="admin-profile-placeholder">A</div>
            )}
            <div>
              <strong>{profile?.displayName || profile?.username || 'Administrator'}</strong>
              <span>{profile?.email || 'admin@medvault.local'}</span>
            </div>
          </div>
        </header>

        <div className="admin-heartbeat" aria-hidden="true" />

        {message && <div className="admin-message">{message}</div>}

        {activeSection === 'dashboard' && (
          <section className="admin-section">
            <div className="admin-stats-grid">
              {[
                { label: 'Total Doctors', value: stats?.totalDoctors || 0, icon: FiUser },
                { label: 'Total Patients', value: stats?.totalPatients || 0, icon: FiUsers },
                { label: 'Total Appointments', value: stats?.totalAppointments || 0, icon: FiFileText },
                { label: "Today's Appointments", value: stats?.todaysAppointments || 0, icon: FiClock },
                { label: 'Approved Appointments', value: stats?.approvedAppointments || 0, icon: FiCheckCircle },
                { label: 'Pending Appointments', value: stats?.pendingAppointments || 0, icon: FiClock }
              ].map((card, idx) => {
                const Icon = card.icon;
                return (
                  <motion.div
                    key={card.label}
                    className="admin-stat-card"
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ delay: idx * 0.05 }}
                    whileHover={{ y: -7, scale: 1.015 }}
                  >
                    <div className="stat-icon">
                      <Icon size={20} />
                    </div>
                    <div>
                      <p>{card.label}</p>
                      <h3>{card.value}</h3>
                    </div>
                  </motion.div>
                );
              })}
            </div>

            <div className="admin-highlight">
              <div>
                <h2>AI-ready care orchestration</h2>
                <p>Track approvals, staffing, and patient flow with intelligent scheduling insights.</p>
              </div>
              <div className="highlight-icons">
                <FaRobot />
                <FaUserMd />
                <FaHospital />
              </div>
            </div>
          </section>
        )}

        {activeSection === 'doctors' && (
          <section className="admin-section">
            <div className="admin-table-card">
              <div className="admin-table-header">
                <h2>Doctors Management</h2>
                <span className="muted">Activate or review doctor profiles</span>
              </div>
              <div className="admin-table-wrapper">
                <table className="admin-table">
                  <thead>
                    <tr>
                      <th>Name</th>
                      <th>Specialization</th>
                      <th>Qualification</th>
                      <th>Experience</th>
                      <th>Email</th>
                      <th>Status</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {doctors.map((doctor) => (
                      <tr key={doctor.id}>
                        <td>{doctor.fullName || 'Unknown'}</td>
                        <td>{doctor.specialization || 'General'}</td>
                        <td>{doctor.qualification || '-'}</td>
                        <td>{doctor.experienceYears ? `${doctor.experienceYears} yrs` : '-'}</td>
                        <td>{doctor.email || '-'}</td>
                        <td>
                          <span className={`admin-badge ${doctor.active ? 'active' : 'inactive'}`}>
                            {doctor.active ? 'Active' : 'Inactive'}
                          </span>
                        </td>
                        <td className="admin-actions">
                          <button className="ghost" onClick={() => setSelectedDoctor(doctor)}>
                            View
                          </button>
                          <button className="primary" onClick={() => handleToggleDoctor(doctor)}>
                            {doctor.active ? 'Deactivate' : 'Activate'}
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>

            {selectedDoctor && (
              <div className="admin-modal" onClick={() => setSelectedDoctor(null)}>
                <div className="admin-modal-card" onClick={(e) => e.stopPropagation()}>
                  <h3>Doctor Profile</h3>
                  <p><strong>Name:</strong> {selectedDoctor.fullName || 'Unknown'}</p>
                  <p><strong>Specialization:</strong> {selectedDoctor.specialization || 'General'}</p>
                  <p><strong>Qualification:</strong> {selectedDoctor.qualification || '-'}</p>
                  <p><strong>Experience:</strong> {selectedDoctor.experienceYears || 0} years</p>
                  <p><strong>Email:</strong> {selectedDoctor.email || '-'}</p>
                  <button className="primary" onClick={() => setSelectedDoctor(null)}>Close</button>
                </div>
              </div>
            )}
          </section>
        )}

        {activeSection === 'patients' && (
          <section className="admin-section">
            <div className="admin-table-card">
              <div className="admin-table-header">
                <h2>Patients Management</h2>
                <div className="admin-search">
                  <FiSearch size={16} />
                  <input
                    placeholder="Search patients by name, email, or phone"
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                  />
                </div>
              </div>
              <div className="admin-table-wrapper">
                <table className="admin-table">
                  <thead>
                    <tr>
                      <th>Name</th>
                      <th>Age</th>
                      <th>Gender</th>
                      <th>Blood Group</th>
                      <th>Phone</th>
                      <th>Email</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filteredPatients.map((patient) => (
                      <tr key={patient.id}>
                        <td>{patient.fullName || 'Unknown'}</td>
                        <td>{patient.age || '-'}</td>
                        <td>{patient.gender || '-'}</td>
                        <td>{patient.bloodGroup || '-'}</td>
                        <td>{patient.phone || '-'}</td>
                        <td>{patient.email || '-'}</td>
                        <td className="admin-actions">
                          <button className="ghost" onClick={() => setSelectedPatient(patient)}>
                            Medical History
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>

            {selectedPatient && (
              <div className="admin-modal" onClick={() => setSelectedPatient(null)}>
                <div className="admin-modal-card" onClick={(e) => e.stopPropagation()}>
                  <h3>Medical History</h3>
                  <p><strong>Patient:</strong> {selectedPatient.fullName || 'Unknown'}</p>
                  <p>{selectedPatient.medicalHistory || 'No medical history recorded.'}</p>
                  <button className="primary" onClick={() => setSelectedPatient(null)}>Close</button>
                </div>
              </div>
            )}
          </section>
        )}

        {activeSection === 'appointments' && (
          <section className="admin-section">
            <AdminRescheduleForm onRescheduled={fetchAppointments} />
            <div className="admin-table-card">
              <div className="admin-table-header">
                <h2>Appointments Overview</h2>
                <span className="muted">Approved = Completed, Pending = Scheduled</span>
              </div>
              <div className="admin-table-wrapper">
                <table className="admin-table">
                  <thead>
                    <tr>
                      <th>Patient</th>
                      <th>Doctor</th>
                      <th>Specialization</th>
                      <th>Date</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {appointments.map((apt) => (
                      <tr key={apt.id}>
                        <td>{apt.patientName || '-'}</td>
                        <td>{apt.doctorName || '-'}</td>
                        <td>{apt.doctorSpecialization || '-'}</td>
                        <td>{formatDate(apt.appointmentDate)}</td>
                        <td>
                          <span className={`admin-badge ${apt.status === 'COMPLETED' ? 'active' : 'pending'}`}>
                            {apt.status}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </section>
        )}

        {activeSection === 'reports' && (
          <section className="admin-section">
            <div className="admin-report">
              <h2>Operational Reports</h2>
              <p className="muted">Snapshot of hospital operations and workflow health.</p>
              <div className="admin-report-grid">
                <div>
                  <h4>Approval Rate</h4>
                  <p>{stats ? Math.round((stats.approvedAppointments / Math.max(stats.totalAppointments, 1)) * 100) : 0}%</p>
                </div>
                <div>
                  <h4>Pending Queue</h4>
                  <p>{stats?.pendingAppointments || 0} appointments awaiting approval</p>
                </div>
                <div>
                  <h4>Active Doctors</h4>
                  <p>{doctors.filter((d) => d.active).length} currently active</p>
                </div>
              </div>
            </div>
          </section>
        )}

        {activeSection === 'calendar' && (
          <section className="admin-section">
            <div className="admin-calendar-grid">
              <div className="admin-calendar-card">
                <Calendar
                  value={selectedDate}
                  onChange={setSelectedDate}
                  tileClassName={({ date }) => getTileClass(date)}
                />
                <div className="admin-calendar-legend">
                  <span className="legend approved">Approved</span>
                  <span className="legend pending">Pending</span>
                  <span className="legend today">Today</span>
                </div>
              </div>
              <div className="admin-calendar-list">
                <h3>{selectedDate.toDateString()}</h3>
                {getAppointmentsForDate(selectedDate).length === 0 && (
                  <p className="muted">No appointments scheduled.</p>
                )}
                {getAppointmentsForDate(selectedDate).map((apt) => (
                  <div key={apt.id} className="admin-appointment-card">
                    <div>
                      <strong>{apt.patientName || 'Patient'}</strong>
                      <p>{apt.doctorName || 'Doctor'} - {apt.doctorSpecialization || 'General'}</p>
                      <span>{formatDate(apt.appointmentDate)}</span>
                    </div>
                    <span className={`admin-badge ${apt.status === 'COMPLETED' ? 'active' : 'pending'}`}>
                      {apt.status}
                    </span>
                  </div>
                ))}
              </div>
            </div>
          </section>
        )}

        {activeSection === 'profile' && (
          <section className="admin-section">
            <div className="admin-profile-card">
              <h2>Edit Profile</h2>
              <div className="admin-profile-grid">
                <div className="admin-profile-image">
                  {profilePreview ? (
                    <img src={profilePreview} alt="Admin profile" />
                  ) : (
                    <div className="admin-profile-placeholder large">A</div>
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
                </div>
                <div className="admin-profile-form">
                  <label>
                    Display Name
                    <input
                      type="text"
                      value={profileForm.displayName}
                      onChange={(e) => setProfileForm((prev) => ({ ...prev, displayName: e.target.value }))}
                    />
                  </label>
                  <label>
                    Email
                    <input
                      type="email"
                      value={profileForm.email}
                      onChange={(e) => setProfileForm((prev) => ({ ...prev, email: e.target.value }))}
                    />
                  </label>
                  <label>
                    New Password
                    <input
                      type="password"
                      value={profileForm.password}
                      onChange={(e) => setProfileForm((prev) => ({ ...prev, password: e.target.value }))}
                      placeholder="Leave blank to keep current"
                    />
                  </label>
                  <button className="primary" onClick={handleProfileSave}>Save Changes</button>
                </div>
              </div>
            </div>
          </section>
        )}
      </motion.main>
    </motion.div>
  );
}
