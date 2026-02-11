import React, { useState, useEffect } from 'react';
import { Routes, Route, Link, useNavigate } from 'react-router-dom'
import AuthModal from './components/AuthModal';
import ForgotPassword from './components/ForgotPassword';
import ResetPassword from './components/ResetPassword';
import PatientDashboardNew from './components/PatientDashboardNew';
import DoctorDashboardNew from './components/DoctorDashboardNew';
import './styles/global.css';
import './styles/dashboard.css';
import './styles/calendar.css';
import './styles/prescription.css';

function App() {
    const [isAuthOpen, setAuthOpen] = useState(false);
    const [user, setUser] = useState(null);

    useEffect(() => {
        // Check for existing session
        const storedUser = localStorage.getItem('user');
        if (storedUser) {
            setUser(JSON.parse(storedUser));
        }
    }, []);

    const handleLoginSuccess = (userData) => {
        setUser(userData);
    };

    const navigate = useNavigate();

    const handleLogout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        setUser(null);
        navigate('/');
    };

    const HomePage = () => (
        <>
            {/* Hero Section */}
            <div className="container hero">
                <div className="hero-content">
                    <h1>Secure Healthcare Management for Everyone</h1>
                    <p>
                        MedVault is your digital hospital desk – a single place for appointments, prescriptions,
                        and records for both patients and doctors.
                    </p>

                    <div style={{ display: 'flex', gap: '1rem' }}>
                        {!user && (
                            <button className="btn btn-primary" style={{ fontSize: '1.2rem', padding: '1rem 2rem' }} onClick={() => setAuthOpen(true)}>
                                Get Started Now
                            </button>
                        )}
                        <a href="#how-it-works" className="btn btn-outline" style={{ fontSize: '1.2rem', padding: '1rem 2rem', textDecoration: 'none' }}>
                            How it works
                        </a>
                    </div>

                    <div className="hero-stats">
                        <div className="stat-item">
                            <h3>24/7</h3>
                            <p>Access to your vault</p>
                        </div>
                        <div className="stat-item">
                            <h3>Role‑based</h3>
                            <p>Doctor / Patient views</p>
                        </div>
                        <div className="stat-item">
                            <h3>End‑to‑end</h3>
                            <p>Encrypted records</p>
                        </div>
                    </div>
                </div>

                <div style={{ flex: 1, display: 'flex', justifyContent: 'center' }}>
                    <div className="glass-panel" style={{ width: '420px', height: '520px', display: 'flex', alignItems: 'center', justifyContent: 'center', position: 'relative' }}>
                        <div style={{ position: 'absolute', inset: 0, background: 'linear-gradient(45deg, rgba(124,58,237,0.08), rgba(217,70,239,0.12))', borderRadius: '24px' }}></div>
                        <div style={{ textAlign: 'center', padding: '2rem', position: 'relative' }}>
                            <div style={{ fontSize: '4.5rem', marginBottom: '1rem' }}>🗄️</div>
                            <h3>MedVault snapshot</h3>
                            <p style={{ color: '#64748b', marginBottom: '1.5rem' }}>
                                Today’s appointments, active prescriptions, and follow‑ups in one clean dashboard.
                            </p>
                            <div style={{ display: 'grid', gap: '0.75rem', textAlign: 'left', fontSize: '0.9rem' }}>
                                <div>• Patient profile card with history</div>
                                <div>• Doctor‑side queue for today’s visits</div>
                                <div>• Smart calendar for upcoming slots</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <section id="features" className="container" style={{ padding: '4rem 0 2rem 0' }}>
                <h2 style={{ fontSize: '2rem', marginBottom: '1.5rem' }}>Why MedVault?</h2>
                <div className="grid grid-3">
                    <div className="card">
                        <h3 className="card-title">Unified Patient Vault</h3>
                        <p className="text-muted mt-2">
                            All appointments, prescriptions, and medical history are linked to one secure profile,
                            so nothing is scattered across papers or screenshots.
                        </p>
                    </div>
                    <div className="card">
                        <h3 className="card-title">Doctor‑friendly Workbench</h3>
                        <p className="text-muted mt-2">
                            Doctors see today’s queue, update visit status, and issue prescriptions from a single,
                            clean dashboard – no extra clicks.
                        </p>
                    </div>
                    <div className="card">
                        <h3 className="card-title">Calendar‑first Experience</h3>
                        <p className="text-muted mt-2">
                            A medical calendar view helps patients track follow‑ups and doctors quickly scan
                            busy days at a glance.
                        </p>
                    </div>
                </div>
            </section>

            <section id="security" className="container" style={{ padding: '2rem 0 2rem 0' }}>
                <div className="card" style={{ display: 'flex', gap: '2rem', alignItems: 'flex-start' }}>
                    <div style={{ flex: 1 }}>
                        <h2 className="card-title">Security you can trust</h2>
                        <p className="text-muted mt-2">
                            MedVault treats every record like a bank transaction. Data is encrypted in transit and at rest,
                            access is strictly role‑based, and every action can be audited.
                        </p>
                        <ul className="text-muted" style={{ marginTop: '1.25rem', paddingLeft: '1.25rem', lineHeight: 1.8 }}>
                            <li>JWT‑based authentication and short‑lived tokens</li>
                            <li>Separate views and APIs for patients, doctors, and admins</li>
                            <li>Secure password reset and OTP‑based login options</li>
                        </ul>
                    </div>
                    <div style={{ flex: 1 }}>
                        <div className="glass-panel" style={{ padding: '1.5rem 1.75rem', borderRadius: '20px' }}>
                            <h4 style={{ marginBottom: '0.75rem' }}>Inside the MedVault</h4>
                            <p className="text-muted" style={{ fontSize: '0.95rem' }}>
                                Only authenticated users can reach the API. Patients can see and edit only their own
                                profile and appointments; doctors can see only the patients assigned to their schedule.
                                Admin endpoints are separated and protected in the backend.
                            </p>
                        </div>
                    </div>
                </div>
            </section>

            <section id="how-it-works" className="container" style={{ padding: '2rem 0 4rem 0' }}>
                <h2 style={{ fontSize: '2rem', marginBottom: '1.5rem' }}>How MedVault works</h2>
                <div className="grid grid-3">
                    <div className="card">
                        <h3 className="card-title">1. Login or Sign up</h3>
                        <p className="text-muted mt-2">
                            Click <strong>Login</strong> or <strong>Sign Up</strong> in the top‑right. You can register
                            as a patient or admin, then log in with password or OTP from your email.
                        </p>
                    </div>
                    <div className="card">
                        <h3 className="card-title">2. Open your dashboard</h3>
                        <p className="text-muted mt-2">
                            After login, the <strong>Open Dashboard</strong> button sends patients to their patient dashboard
                            and doctors to the doctor dashboard with stats, upcoming visits, and a medical calendar.
                        </p>
                    </div>
                    <div className="card">
                        <h3 className="card-title">3. Book and manage visits</h3>
                        <p className="text-muted mt-2">
                            Appointments and prescriptions are tied to your profile. The calendar view highlights days
                            with visits, and clicking a date shows detailed appointment cards for that day.
                        </p>
                    </div>
                </div>
            </section>
        </>
    );

    return (
        <div>
            {/* Navbar */}
            <nav className="navbar glass-panel" style={{ borderRadius: '0 0 16px 16px' }}>
                <div className="navbar-inner">
                    <Link to="/" className="logo" style={{ textDecoration: 'none', color: 'inherit' }}>
                        <span style={{ fontSize: '1.8rem' }}>🏥</span> MedVault
                    </Link>

                    <div className="nav-links">
                        <a href="#features" className="nav-link">Features</a>
                        <a href="#security" className="nav-link">Security</a>
                        <a href="#how-it-works" className="nav-link">How it works</a>
                    </div>

                    <div className="nav-actions">
                        {user ? (
                            <>
                                <span style={{ fontWeight: 500, fontSize: '0.95rem', color: '#6b7280' }}>
                                    Hi, {user.username} ({user.role})
                                </span>
                                <button className="btn btn-secondary" onClick={handleLogout}>Logout</button>
                                <a
                                    href={user.role && user.role.includes('PATIENT') ? '/patient' : '/doctor'}
                                    className="btn btn-primary"
                                >
                                    Open Dashboard
                                </a>
                            </>
                        ) : (
                            <>
                                <button className="btn btn-secondary" onClick={() => setAuthOpen(true)}>Login</button>
                                <button className="btn btn-primary" onClick={() => setAuthOpen(true)}>Sign Up</button>
                            </>
                        )}
                    </div>
                </div>
            </nav>

            <div style={{ marginTop: 120 }}>
                <Routes>
                    <Route path="/" element={<HomePage />} />
                    <Route path="/forgot-password" element={<ForgotPassword />} />
                    <Route path="/reset-password" element={<ResetPassword />} />
                    <Route path="/patient" element={<PatientDashboardNew />} />
                    <Route path="/doctor" element={<DoctorDashboardNew />} />
                </Routes>
            </div>

            <AuthModal
                isOpen={isAuthOpen}
                onClose={() => setAuthOpen(false)}
                onLoginSuccess={handleLoginSuccess}
            />
        </div>
    )
}

export default App
