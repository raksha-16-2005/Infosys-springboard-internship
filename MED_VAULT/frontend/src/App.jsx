import React, { useState, useEffect } from 'react';
import { Routes, Route, Link, useNavigate, Navigate } from 'react-router-dom'
import { motion } from 'framer-motion';

// Error Boundary Component
class ErrorBoundary extends React.Component {
    constructor(props) {
        super(props);
        this.state = { hasError: false, error: null };
    }

    static getDerivedStateFromError(error) {
        return { hasError: true, error };
    }

    componentDidCatch(error, errorInfo) {
        console.error('Component Error:', error, errorInfo);
    }

    render() {
        if (this.state.hasError) {
            return (
                <div style={{ padding: '40px', textAlign: 'center' }}>
                    <h2>⚠️ Component Error</h2>
                    <p>{this.state.error?.message}</p>
                    <button onClick={() => window.location.reload()} style={{
                        padding: '10px 20px',
                        backgroundColor: '#2563eb',
                        color: 'white',
                        border: 'none',
                        borderRadius: '8px',
                        cursor: 'pointer'
                    }}>
                        Reload Page
                    </button>
                </div>
            );
        }

        return this.props.children;
    }
}

// Lazy load heavy components to avoid blank pages
const AuthModal = React.lazy(() => import('./components/AuthModal'));
const ForgotPassword = React.lazy(() => import('./components/ForgotPassword'));
const ResetPassword = React.lazy(() => import('./components/ResetPassword'));
const PatientDashboardNew = React.lazy(() => import('./components/PatientDashboardNew'));
const DoctorDashboardNew = React.lazy(() => import('./components/DoctorDashboardNew'));
const AdminDashboard = React.lazy(() => import('./components/AdminDashboard'));

const LoadingFallback = () => (
    <div style={{ padding: '40px', textAlign: 'center' }}>
        <h2>Loading...</h2>
    </div>
);

import './styles/global.css';

function App() {
    const [isAuthOpen, setAuthOpen] = useState(false);
    const [user, setUser] = useState(() => {
        const storedUser = localStorage.getItem('user');
        if (!storedUser) return null;
        try {
            return JSON.parse(storedUser);
        } catch (e) {
            console.error('Failed to parse stored user', e);
            localStorage.removeItem('user');
            return null;
        }
    });
    const navigate = useNavigate();

    const handleLoginSuccess = (userData) => {
        setUser(userData);
    };

    const handleLogout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        setUser(null);
        navigate('/');
    };

    const dashboardPath =
        user?.role && user.role.includes('ADMIN')
            ? '/admin'
            : user?.role && user.role.includes('PATIENT')
                ? '/patient'
                : '/doctor';

    const ProtectedRoute = ({ allowedRoles, children }) => {
        const token = localStorage.getItem('token');
        
        React.useEffect(() => {
            if (!token || !user) {
                setAuthOpen(true);
            }
        }, [token, user]);
        
        if (!token || !user) {
            return <Navigate to="/" replace />;
        }

        const normalizedRole = String(user.role || '').replace(/^ROLE_/, '');
        if (!allowedRoles.includes(normalizedRole)) {
            return <Navigate to={dashboardPath} replace />;
        }

        return children;
    };

    const HomePage = () => {
        return (
            <motion.div
                className="app-hero-shell"
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                transition={{ duration: 0.4 }}
            >
            <div className="app-floating-objects" aria-hidden="true">
                <span className="float-orb orb-1" />
                <span className="float-orb orb-2" />
                <span className="float-orb orb-3" />
                <span className="float-orb orb-4" />
            </div>

            <div className="container app-hero">
                <motion.div className="hero-content" initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.45 }}>
                    <h1>Secure Healthcare Management Platform</h1>
                    <p>
                        MedVault delivers appointment scheduling, patient records, and role-based access in a clean hospital-ready workflow.
                    </p>

                    <div className="hero-cta-row">
                        {!user && (
                            <button className="btn btn-primary hero-btn" onClick={() => setAuthOpen(true)}>
                                Get Started Now
                            </button>
                        )}
                        <button className="btn btn-outline hero-btn">
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
                </motion.div>

                <motion.div className="hero-visual-wrap" initial={{ opacity: 0, y: 18 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.5, delay: 0.1 }}>
                    <div className="glass-panel hero-visual-card">
                        <div className="hero-visual-overlay"></div>
                        <div className="hero-visual-content">
                            <h3>Operational Snapshot</h3>
                            <div className="hero-kpi-grid">
                                <div className="hero-kpi-item">
                                    <strong>99.9%</strong>
                                    <span>Platform uptime</span>
                                </div>
                                <div className="hero-kpi-item">
                                    <strong>24/7</strong>
                                    <span>Scheduling support</span>
                                </div>
                                <div className="hero-kpi-item">
                                    <strong>10k+</strong>
                                    <span>Patient records</span>
                                </div>
                                <div className="hero-kpi-item">
                                    <strong>HIPAA</strong>
                                    <span>Security alignment</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </motion.div>
            </div>

            <section className="container hospital-dna-section">
                <motion.article
                    className="dna-about-card"
                    initial={{ opacity: 0, y: 16 }}
                    whileInView={{ opacity: 1, y: 0 }}
                    viewport={{ once: true, amount: 0.25 }}
                    transition={{ duration: 0.45 }}
                >
                    <h2>Hospital DNA</h2>
                    <p>
                        MedVault is designed for modern hospital flow: triage visibility, doctor-patient continuity,
                        and secure medical intelligence in one operational surface.
                    </p>
                    <ul>
                        <li>Care-path orchestration with real-time updates</li>
                        <li>Clinical accuracy with role-based controls</li>
                        <li>Operational reliability with audit-ready records</li>
                    </ul>
                </motion.article>

                <motion.article
                    className="dna-animation-card"
                    initial={{ opacity: 0, y: 16 }}
                    whileInView={{ opacity: 1, y: 0 }}
                    viewport={{ once: true, amount: 0.25 }}
                    transition={{ duration: 0.48, delay: 0.06 }}
                >
                    <div className="dna-orbit-shell">
                        <span className="dna-ring dna-ring-1" />
                        <span className="dna-ring dna-ring-2" />
                        <span className="dna-ring dna-ring-3" />
                        <span className="dna-core">+</span>
                    </div>
                    <p>Live telemetry • Smart scheduling • Precision care signals</p>
                </motion.article>
            </section>

            <section className="container app-feature-timeline">
                <motion.h3
                    className="timeline-title"
                    initial={{ opacity: 0, y: 16 }}
                    whileInView={{ opacity: 1, y: 0 }}
                    viewport={{ once: true, amount: 0.3 }}
                    transition={{ duration: 0.4 }}
                >
                    MedVault Feature Journey
                </motion.h3>

                <div className="timeline-line" aria-hidden="true" />
                <motion.div
                    className="timeline-progress"
                    initial={{ scaleY: 0 }}
                    whileInView={{ scaleY: 1 }}
                    viewport={{ once: false, amount: 0.25 }}
                    transition={{ duration: 0.9, ease: 'easeOut' }}
                />

                {[
                    { step: '01', title: 'Patient Onboarding', text: 'Secure sign-up and profile setup with structured medical details.' },
                    { step: '02', title: 'Smart Scheduling', text: 'Patients book slots and doctors manage queue updates in real time.' },
                    { step: '03', title: 'Care Documentation', text: 'Doctors add diagnosis notes, records, and prescriptions per visit.' },
                    { step: '04', title: 'Feedback Intelligence', text: 'Patient reviews are collected to improve consultation quality.' }
                ].map((item, index) => (
                    <motion.article
                        key={item.step}
                        className={`timeline-item ${index % 2 === 0 ? 'timeline-left' : 'timeline-right'}`}
                        initial={{ opacity: 0, x: index % 2 === 0 ? -24 : 24, filter: 'blur(8px)' }}
                        whileInView={{ opacity: 1, x: 0, filter: 'blur(0px)' }}
                        viewport={{ once: false, amount: 0.35 }}
                        transition={{ duration: 0.42, delay: index * 0.07 }}
                    >
                        <span className="timeline-dot" aria-hidden="true" />
                        <div className="timeline-step">{item.step}</div>
                        <div className="timeline-content">
                            <h4>{item.title}</h4>
                            <p>{item.text}</p>
                        </div>
                    </motion.article>
                ))}
            </section>

            <section className="container app-feature-section">
                {[
                    { title: 'Smart Booking Engine', text: 'Doctor availability, patient urgency, and slot confidence in one flow.' },
                    { title: 'Reschedule Intelligence', text: 'Fast appointment shifts with status clarity for doctor and patient.' },
                    { title: 'Medical Timeline', text: 'Prescriptions, notes, and records connected to each appointment history.' },
                    { title: 'Role-secure Access', text: 'Admin, doctor, and patient views with clean and isolated workflows.' }
                ].map((feature, index) => (
                    <motion.div
                        key={feature.title}
                        className="app-feature-card"
                        initial={{ opacity: 0, y: 16 }}
                        whileInView={{ opacity: 1, y: 0 }}
                        viewport={{ once: true, amount: 0.2 }}
                        transition={{ duration: 0.4, delay: index * 0.06 }}
                        whileHover={{ y: -6 }}
                    >
                        <h3>{feature.title}</h3>
                        <p>{feature.text}</p>
                    </motion.div>
                ))}
            </section>

            <section className="container app-feedback-preview">
                <motion.article
                    className="feedback-preview-card"
                    initial={{ opacity: 0, y: 16 }}
                    whileInView={{ opacity: 1, y: 0 }}
                    viewport={{ once: true, amount: 0.25 }}
                    transition={{ duration: 0.4 }}
                >
                    <div>
                        <h3>Patient Feedback Review</h3>
                        <p>Recent consultation feedback from patient experiences.</p>
                    </div>
                    <div className="feedback-score">4.8 / 5</div>
                </motion.article>
            </section>

            <div className="app-home-end-space" aria-hidden="true" />
        </motion.div>
        );
    };

    return (
        <div className="app-shell">
            <nav className="navbar glass-panel app-navbar">
                <Link to="/" className="logo app-logo">
                    <span className="logo-mark">MV</span> MedVault
                </Link>
                <div className="app-nav-actions">
                    {user ? (
                        <div className="app-user-actions">
                            <span className="app-user-chip">Hi, {user.displayName || user.username} ({user.role})</span>
                            <button className="btn btn-outline" onClick={handleLogout}>Logout</button>
                            <Link to={dashboardPath} className="btn btn-primary">
                                Dashboard
                            </Link>
                        </div>
                    ) : (
                        <>
                            <button className="btn btn-outline" onClick={() => setAuthOpen(true)}>Login</button>
                            <button className="btn btn-primary" onClick={() => setAuthOpen(true)}>Sign Up</button>
                        </>
                    )}
                </div>
            </nav>

            <div className="app-page-content">
                <ErrorBoundary>
                    <Routes>
                        <Route path="/" element={<HomePage />} />
                        <Route path="/forgot-password" element={
                            <React.Suspense fallback={<LoadingFallback />}>
                                <ForgotPassword />
                            </React.Suspense>
                        } />
                        <Route path="/reset-password" element={
                            <React.Suspense fallback={<LoadingFallback />}>
                                <ResetPassword />
                            </React.Suspense>
                        } />
                        <Route path="/patient" element={
                            <ProtectedRoute allowedRoles={['PATIENT']}>
                                <React.Suspense fallback={<LoadingFallback />}>
                                    <PatientDashboardNew />
                                </React.Suspense>
                            </ProtectedRoute>
                        } />
                        <Route path="/doctor" element={
                            <ProtectedRoute allowedRoles={['DOCTOR']}>
                                <React.Suspense fallback={<LoadingFallback />}>
                                    <DoctorDashboardNew />
                                </React.Suspense>
                            </ProtectedRoute>
                        } />
                        <Route path="/admin" element={
                            <ProtectedRoute allowedRoles={['ADMIN']}>
                                <React.Suspense fallback={<LoadingFallback />}>
                                    <AdminDashboard onLogout={handleLogout} />
                                </React.Suspense>
                            </ProtectedRoute>
                        } />
                    </Routes>
                </ErrorBoundary>
            </div>

            <React.Suspense fallback={null}>
                <AuthModal
                    isOpen={isAuthOpen}
                    onClose={() => setAuthOpen(false)}
                    onLoginSuccess={handleLoginSuccess}
                />
            </React.Suspense>
        </div>
    )
}

export default App
