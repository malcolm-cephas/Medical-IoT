import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { getBackendUrl } from '../config';

/**
 * Login Component
 *
 * Provides the user interface for system authentication.
 * Handles input collection, backend API submission, token storage, and redirection.
 *
 * @param {Function} onLogin - Callback prop to update global user state in parent App component.
 */
const Login = ({ onLogin }) => {
    // Local state for form inputs
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');

    // State for error messaging and loading indicators
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const navigate = useNavigate();

    /**
     * Handles login form submission.
     * Prevents default refresh, sends POST request to /api/auth/login.
     */
    const handleLogin = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            // Attempt to authenticate with backend
            const response = await axios.post(`${getBackendUrl()}/api/auth/login`, {
                username,
                password
            });

            // Extract JWT token and user details from successful response
            const { token, user } = response.data;

            // Normalize role to lowercase to ensure consistency across frontend checks
            if (user.role) user.role = user.role.toLowerCase();

            // Set global Authorization header for all future axios requests
            axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;

            // Store session data in localStorage for persistence
            localStorage.setItem('token', token);
            localStorage.setItem('user', JSON.stringify(user));

            // Update global state and redirect to dashboard
            onLogin(user);
            navigate('/dashboard');
        } catch (err) {
            console.error("Login failed", err);
            let errorMessage = "Login failed. Check credentials or server status.";

            // Specific error handling for network issues
            if (err.code === "ERR_NETWORK") {
                errorMessage = "Network Error. Ensure the backend is running at http://localhost:8080";
            } else if (err.response) {
                // Backend returned an error message
                errorMessage = err.response.data?.message || JSON.stringify(err.response.data);
            }

            setError(errorMessage);
        } finally {
            setLoading(false);
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
                    {/* Display error message box if error exists */}
                    {error && (
                        <div style={{
                            color: '#ff6b6b',
                            marginBottom: '1rem',
                            textAlign: 'center',
                            padding: '0.5rem',
                            background: 'rgba(255, 107, 107, 0.1)',
                            borderRadius: '4px',
                            border: '1px solid rgba(255, 107, 107, 0.2)'
                        }}>
                            {error}
                        </div>
                    )}
                    <button type="submit" className="btn-login">Login</button>
                </form>
                {/* Helper Section for Demo Credentials */}
                <div className="login-help">
                    <p>Don't have an account? <a href="#" style={{ color: 'var(--accent-color)' }}>Create Account</a></p>
                    <hr style={{ border: 0, borderTop: '1px solid rgba(255,255,255,0.1)', margin: '1rem 0' }} />
                    <p><strong>Demo Credentials:</strong></p>
                    <ul>
                        <li>Admin: admin / [YOUR_PASSWORD]</li>
                        <li>Doctor: doctor_micheal / [YOUR_PASSWORD]</li>
                        <li>Nurse: nurse_staff / [YOUR_PASSWORD]</li>
                        <li>Patient: patient_001 / [YOUR_PASSWORD]</li>
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
