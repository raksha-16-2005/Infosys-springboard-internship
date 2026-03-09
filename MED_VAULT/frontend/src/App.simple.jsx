import React, { useState, useEffect } from 'react';
import { Routes, Route, Link, useNavigate, Navigate } from 'react-router-dom'
import { motion } from 'framer-motion';

function App() {
    const [isAuthOpen, setAuthOpen] = useState(false);
    const [user, setUser] = useState(null);
    const navigate = useNavigate();

    useEffect(() => {
        // Check for existing session
        const storedUser = localStorage.getItem('user');
        if (storedUser) {
            setUser(JSON.parse(storedUser));
        }
    }, []);

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

    const HomePage = () => {
        return (
            <motion.div
                className="app-hero-shell"
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                transition={{ duration: 0.4 }}
            >
                <div className="container app-hero">
                    <div className="hero-content">
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
                    </div>
                </div>
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
                <Routes>
                    <Route path="/" element={<HomePage />} />
                </Routes>
            </div>

            <div style={{padding: '20px', textAlign: 'center', marginTop: '50px'}}>
                <h2>🔧 Frontend Under Maintenance</h2>
                <p>Some dashboard features are being updated. Full app coming soon!</p>
            </div>
        </div>
    )
}

export default App
