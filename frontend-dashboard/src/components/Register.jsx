import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { getAuthUrl } from '../config';

const Register = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [fullName, setFullName] = useState('');
    const [age, setAge] = useState('');
    const [gender, setGender] = useState('Male');
    const [role, setRole] = useState('PATIENT');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const navigate = useNavigate();

    const handleRegister = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            await axios.post(`${getAuthUrl()}/api/auth/register`, {
                username,
                password,
                fullName,
                age: parseInt(age, 10),
                gender,
                role
            });

            alert('Registration Successful! Please login.');
            navigate('/');
        } catch (err) {
            console.error("Registration failed", err);
            setError(err.response?.data || "Registration failed. Try a different username.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="login-container">
            <div className="card login-card" style={{ maxWidth: '450px' }}>
                <h2>Create Account</h2>
                <form onSubmit={handleRegister}>
                    <div className="form-group">
                        <label>Username</label>
                        <input type="text" value={username} onChange={(e) => setUsername(e.target.value)} required />
                    </div>
                    <div className="form-group">
                        <label>Full Name</label>
                        <input type="text" value={fullName} onChange={(e) => setFullName(e.target.value)} required />
                    </div>
                    <div style={{ display: 'flex', gap: '1rem' }}>
                        <div className="form-group" style={{ flex: 1 }}>
                            <label>Age</label>
                            <input type="number" value={age} onChange={(e) => setAge(e.target.value)} required />
                        </div>
                        <div className="form-group" style={{ flex: 1 }}>
                            <label>Gender</label>
                            <select value={gender} onChange={(e) => setGender(e.target.value)} style={{ padding: '0.75rem', width: '100%', borderRadius: '4px' }}>
                                <option>Male</option>
                                <option>Female</option>
                                <option>Other</option>
                            </select>
                        </div>
                    </div>
                    <div className="form-group">
                        <label>Role</label>
                        <select value={role} onChange={(e) => setRole(e.target.value)} style={{ padding: '0.75rem', width: '100%', borderRadius: '4px' }}>
                            <option value="PATIENT">Patient</option>
                            <option value="DOCTOR">Doctor</option>
                            <option value="NURSE">Nurse</option>
                        </select>
                    </div>
                    <div className="form-group">
                        <label>Password</label>
                        <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
                    </div>

                    {error && <div style={{ color: '#ff6b6b', marginBottom: '1rem', textAlign: 'center' }}>{error}</div>}

                    <button type="submit" className="btn-login" disabled={loading}>
                        {loading ? 'Creating...' : 'Register'}
                    </button>

                    <div style={{ textAlign: 'center', marginTop: '1rem' }}>
                        <span onClick={() => navigate('/')} style={{ color: 'var(--accent-color)', cursor: 'pointer', textDecoration: 'underline' }}>Back to Login</span>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default Register;
