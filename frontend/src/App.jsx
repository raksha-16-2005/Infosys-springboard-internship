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
                        MedVault provides a unified platform for patients and administrators to manage health records securely.
                        Experience the future of medical data with our role-based, encrypted, and easy-to-use system.
                    </p>

                    <div style={{ display: 'flex', gap: '1rem' }}>
                        {!user && (
                            <button className="btn btn-primary" style={{ fontSize: '1.2rem', padding: '1rem 2rem' }} onClick={() => setAuthOpen(true)}>
                                Get Started Now
                            </button>
                        )}
                        <button className="btn btn-outline" style={{ fontSize: '1.2rem', padding: '1rem 2rem' }}>
                            Learn More
                        </button>
                    </div>

                    <div className="hero-stats">
                        <div className="stat-item">
                            <h3>10k+</h3>
                            <p>Patients</p>
                        </div>
                        <div className="stat-item">
                            <h3>99.9%</h3>
                            <p>Uptime</p>
                        </div>
                        <div className="stat-item">
                            <h3>ISO</h3>
                            <p>Certified</p>
                        </div>
                    </div>
                </div>

                {/* Abstract Visual / Illustration Placeholder */}
                <div style={{ flex: 1, display: 'flex', justifyContent: 'center' }}>
                    <div className="glass-panel" style={{ width: '400px', height: '500px', display: 'flex', alignItems: 'center', justifyContent: 'center', position: 'relative' }}>
                        <div style={{ position: 'absolute', inset: 0, background: 'linear-gradient(45deg, rgba(37,99,235,0.1), rgba(8,145,178,0.1))', borderRadius: '16px' }}></div>
                        <div style={{ textAlign: 'center', padding: '2rem' }}>
                            <div style={{ fontSize: '5rem', marginBottom: '1rem' }}>🛡️</div>
                            <h3>Bank-Grade Security</h3>
                            <p style={{ color: '#64748b' }}>Your data is encrypted and protected with industry standard security protocols.</p>
                        </div>
                    </div>
                </div>
            </div>
        </>
    );

    return (
        <div>
            {/* Navbar */}
            <nav className="navbar glass-panel" style={{ borderRadius: '0 0 16px 16px', top: 0, margin: '0 1rem', width: 'calc(100% - 2rem)', position: 'fixed' }}>
                <Link to="/" className="logo" style={{ textDecoration: 'none', color: 'inherit' }}>
                    <span style={{ fontSize: '1.8rem' }}>🏥</span> MedVault
                </Link>
                <div style={{ display: 'flex', gap: '1rem' }}>
                    {user ? (
                        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                            <span style={{ fontWeight: 500 }}>Hi, {user.username} ({user.role})</span>
                            <button className="btn btn-outline" onClick={handleLogout}>Logout</button>
                            <a href={user.role && user.role.includes('PATIENT')? '/patient':'/doctor'} className="btn btn-primary">Dashboard</a>
                        </div>
                    ) : (
                        <>
                            <button className="btn btn-outline" onClick={() => setAuthOpen(true)}>Login</button>
                            <button className="btn btn-primary" onClick={() => setAuthOpen(true)}>Sign Up</button>
                        </>
                    )}

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
