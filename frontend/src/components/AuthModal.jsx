import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import axios from 'axios';
import { loginStart, loginSuccess, loginFailure } from '../features/auth/authSlice';

const AuthModal = ({ isOpen, onClose }) => {
    const navigate = useNavigate();
    const dispatch = useDispatch();
    const [mode, setMode] = useState('login'); // 'login' | 'signup'
    const [loginMethod, setLoginMethod] = useState('otp'); // 'otp' | 'password'
    const [step, setStep] = useState(1); // 1: Email/Form, 2: OTP Verification (Login only)
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState(null);

    const [formData, setFormData] = useState({
        username: '',
        email: '',
        password: '',
        role: 'PATIENT'
    });
    const [otp, setOtp] = useState('');

    if (!isOpen) return null;

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const resetForm = () => {
        setFormData({ username: '', email: '', password: '', role: 'PATIENT' });
        setOtp('');
        setStep(1);
        setMessage(null);
    };

    const handleClose = () => {
        resetForm();
        onClose();
    };

    const extractErrorMessage = (err, fallback) => {
        const data = err?.response?.data;
        if (!data) return fallback;
        if (typeof data === 'string') return data;
        if (typeof data.message === 'string') return data.message;
        return fallback;
    };

    // --- LOGIN FLOW (OTP) ---
    const handleRequestOtp = async (e) => {
        e.preventDefault();
        setLoading(true);
        setMessage(null);
        try {
            const res = await axios.post('/api/auth/request-otp', { email: formData.email });
            const api = res.data;
            const text = typeof api === 'string' ? api : api.message || 'OTP sent to your email!';
            setStep(2);
            setMessage({ type: 'success', text });
        } catch (err) {
            setMessage({ type: 'error', text: extractErrorMessage(err, 'Failed to send OTP') });
        } finally {
            setLoading(false);
        }
    };

    const handleVerifyOtp = async (e) => {
        e.preventDefault();
        setLoading(true);
        setMessage(null);
        try {
            dispatch(loginStart());
            const res = await axios.post('/api/auth/verify-otp', {
                email: formData.email,
                otp: otp
            });
            const api = res.data;
            if (!api.success) {
                throw { response: { data: api } };
            }
            const payload = api.data; // JwtResponse
            dispatch(loginSuccess(payload));
            handleClose();
        } catch (err) {
            dispatch(loginFailure(extractErrorMessage(err)));
            setMessage({ type: 'error', text: extractErrorMessage(err, 'Invalid OTP') });
        } finally {
            setLoading(false);
        }
    };

    // --- SIGNUP FLOW ---
    const handleSignup = async (e) => {
        e.preventDefault();
        setLoading(true);
        setMessage(null);
        try {
            // Map simple roles to backend enum values
            let roleEnum = formData.role;
            if (!roleEnum.startsWith('ROLE_')) {
                roleEnum = `ROLE_${roleEnum}`;
            }

            const res = await axios.post('/api/auth/register', {
                username: formData.username,
                name: formData.username, // use username as display name for now
                email: formData.email,
                password: formData.password,
                role: roleEnum
            });
            const api = res.data;
            const text = typeof api === 'string'
                ? api
                : (api.message || api.data || 'Registration successful');
            setMessage({ type: 'success', text: text + ' Please switch to login.' });
            // Optional: Automatically switch to login
            setTimeout(() => {
                setMode('login');
                setMessage({ type: 'success', text: 'Account created. Please login with OTP.' });
            }, 1500);
        } catch (err) {
            setMessage({ type: 'error', text: extractErrorMessage(err, 'Registration failed') });
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="modal-overlay" onClick={handleClose}>
            <div className="glass-panel modal-content" onClick={e => e.stopPropagation()}>
                <button className="close-btn" onClick={handleClose}>&times;</button>

                <h2 style={{ marginBottom: '1.5rem', textAlign: 'center' }}>
                    {mode === 'login' ? (step === 1 ? 'Login with OTP' : 'Verify OTP') : 'Create Account'}
                </h2>

                {message && (
                    <div className={message.type === 'error' ? 'error-msg' : 'success-msg'}>
                        {message.text}
                    </div>
                )}

                {mode === 'login' && step === 1 && (
                    <div>
                        <div style={{display:'flex',gap:10,marginBottom:12}}>
                            <button className={loginMethod==='otp'? 'btn btn-primary':'btn'} onClick={()=>setLoginMethod('otp')}>OTP</button>
                            <button className={loginMethod==='password'? 'btn btn-primary':'btn'} onClick={()=>setLoginMethod('password')}>Password</button>
                        </div>

                        {loginMethod === 'otp' && (
                            <form onSubmit={handleRequestOtp}>
                                <div className="form-group">
                                    <label>Email Address</label>
                                    <input
                                        type="email"
                                        name="email"
                                        value={formData.email}
                                        onChange={handleChange}
                                        required
                                        placeholder="Enter your email"
                                    />
                                </div>
                                <button type="submit" className="btn btn-primary" style={{ width: '100%' }} disabled={loading}>
                                    {loading ? 'Sending...' : 'Get OTP'}
                                </button>
                            </form>
                        )}

                        {loginMethod === 'password' && (
                            <form onSubmit={async e=>{
                                e.preventDefault();
                                setLoading(true);
                                setMessage(null);
                                try{
                                    dispatch(loginStart());
                                    const res = await axios.post('/api/auth/login',{username: formData.username, password: formData.password});
                                    const api = res.data;
                                    if (!api.success) {
                                        throw { response: { data: api } };
                                    }
                                    const payload = api.data; // JwtResponse
                                    dispatch(loginSuccess(payload));
                                    handleClose();
                                }catch(err){
                                    dispatch(loginFailure(extractErrorMessage(err)));
                                    setMessage({type:'error', text: extractErrorMessage(err, 'Login failed')});
                                }
                                finally{
                                    setLoading(false);
                                }
                            }}>
                                <div className="form-group">
                                    <label>Username</label>
                                    <input type="text" name="username" value={formData.username} onChange={handleChange} required />
                                </div>
                                <div className="form-group">
                                    <label>Password</label>
                                    <input type="password" name="password" value={formData.password} onChange={handleChange} required />
                                    <a href="#" onClick={(e) => { e.preventDefault(); handleClose(); navigate('/forgot-password'); }} style={{ fontSize: '0.85rem', color: '#64748b', marginTop: '0.5rem', display: 'block' }}>Forgot Password?</a>
                                </div>
                                <button type="submit" className="btn btn-primary" style={{ width: '100%' }} disabled={loading}>{loading? 'Signing in...' : 'Sign in'}</button>
                            </form>
                        )}

                        <p style={{ marginTop: '1rem', textAlign: 'center', fontSize: '0.9rem' }}>
                            Don't have an account? <a href="#" onClick={(e) => { e.preventDefault(); setMode('signup'); setMessage(null); }} style={{ color: 'var(--primary-color)' }}>Sign up</a>
                        </p>
                    </div>
                )}

                {mode === 'login' && step === 2 && (
                    <form onSubmit={handleVerifyOtp}>
                        <div className="form-group">
                            <label>Enter OTP</label>
                            <input
                                type="text"
                                value={otp}
                                onChange={(e) => setOtp(e.target.value)}
                                required
                                placeholder="6-digit code"
                                maxLength={6}
                                style={{ letterSpacing: '0.5rem', textAlign: 'center', fontSize: '1.5rem' }}
                            />
                        </div>
                        <button type="submit" className="btn btn-primary" style={{ width: '100%' }} disabled={loading}>
                            {loading ? 'Verifying...' : 'Login'}
                        </button>
                        <p style={{ marginTop: '1rem', textAlign: 'center', fontSize: '0.9rem' }}>
                            <a href="#" onClick={(e) => { e.preventDefault(); setStep(1); }} style={{ color: '#64748b' }}>Back to Email</a>
                        </p>
                    </form>
                )}

                {mode === 'signup' && (
                    <form onSubmit={handleSignup}>
                        <div className="form-group">
                            <label>Username</label>
                            <input type="text" name="username" value={formData.username} onChange={handleChange} required />
                        </div>
                        <div className="form-group">
                            <label>Email</label>
                            <input type="email" name="email" value={formData.email} onChange={handleChange} required />
                        </div>
                        <div className="form-group">
                            <label>Password</label>
                            <input type="password" name="password" value={formData.password} onChange={handleChange} required />
                        </div>
                        <div className="form-group">
                            <label>I am a:</label>
                            <select name="role" value={formData.role} onChange={handleChange}>
                                <option value="PATIENT">Patient</option>
                                <option value="ADMIN">Admin</option>
                            </select>
                        </div>
                        <button type="submit" className="btn btn-primary" style={{ width: '100%' }} disabled={loading}>
                            {loading ? 'Creating Account...' : 'Sign Up'}
                        </button>
                        <p style={{ marginTop: '1rem', textAlign: 'center', fontSize: '0.9rem' }}>
                            Already have an account? <a href="#" onClick={(e) => { e.preventDefault(); setMode('login'); setMessage(null); }} style={{ color: 'var(--primary-color)' }}>Login</a>
                        </p>
                    </form>
                )}

            </div>
        </div>
    );
};

export default AuthModal;
