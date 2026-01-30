import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const Login = ({ onLogin }) => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const navigate = useNavigate();

    const handleLogin = (e) => {
        e.preventDefault();

        // Simulate Authentication for Demo
        // In a real app, this would hit /api/auth/login
        let role = 'patient';
        if (username.toLowerCase().includes('doctor')) role = 'doctor';
        else if (username.toLowerCase().includes('nurse')) role = 'nurse';

        // Validate simple demo credentials (or allow any for now as per "demo" feel)
        if (password) {
            onLogin({ username, role });
            navigate('/dashboard');
        } else {
            alert("Please enter a password");
        }
    };

    return (
        <div className="login-container">
            <div className="card login-card">
                <h2>System Access</h2>
                <form onSubmit={handleLogin}>
                    <div className="form-group">
                        <label>Username</label>
                        <input
                            type="text"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            required
                            placeholder="e.g., doctor, nurse, patient_alpha"
                        />
                    </div>
                    <div className="form-group">
                        <label>Password</label>
                        <input
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                            placeholder="system password"
                        />
                    </div>
                    <button type="submit" className="btn-login">Login</button>
                </form>
                <div className="login-help">
                    <p>Don't have an account? <a href="#" style={{ color: 'var(--accent-color)' }}>Create Account</a></p>
                    <hr style={{ border: 0, borderTop: '1px solid rgba(255,255,255,0.1)', margin: '1rem 0' }} />
                    <p><strong>Demo Credentials:</strong></p>
                    <ul>
                        <li>Admin: admin / password</li>
                        <li>Doctor: doctor_micheal / password</li>
                        <li>Nurse: nurse_jane / password</li>
                        <li>Patient: patient_001 / password</li>
                        <li>Patient: patient_002 / password</li>
                        <li>Patient: patient_003 / password</li>
                    </ul>
                </div>
            </div>

            <style>{`
        .login-container {
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 80vh;
        }
    
        .login-card {
            width: 100%;
            max-width: 400px;
            padding: 2rem;
        }
    
        .login-card h2 {
            text-align: center;
            margin-bottom: 2rem;
            color: var(--accent-color);
        }
    
        .login-help {
            margin-top: 2rem;
            font-size: 0.8rem;
            color: var(--text-secondary);
            background: rgba(0, 0, 0, 0.2);
            padding: 1rem;
            border-radius: 8px;
        }
    
        .login-help ul {
            padding-left: 1.2rem;
            margin-top: 0.5rem;
        }
      `}</style>
        </div>
    );
};

export default Login;
