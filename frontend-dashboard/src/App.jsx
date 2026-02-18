import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import axios from 'axios';
import Dashboard from './components/Dashboard';
import MobileDashboard from './components/MobileDashboard';
import Login from './components/Login';

// Helper to get initial user state synchronously
const getInitialUser = () => {
  const savedUser = localStorage.getItem('user');
  const savedToken = localStorage.getItem('token');
  if (savedUser && savedToken) {
    try {
      const user = JSON.parse(savedUser);
      axios.defaults.headers.common['Authorization'] = `Bearer ${savedToken}`;
      return user;
    } catch (e) {
      // If parsing fails, clear invalid data
      localStorage.removeItem('user');
      localStorage.removeItem('token');
      return null;
    }
  }
  return null;
};

function App() {
  const [user, setUser] = useState(getInitialUser());
  const [theme, setTheme] = useState(localStorage.getItem('theme') || 'dark');
  const [isMobile, setIsMobile] = useState(window.innerWidth < 768);

  useEffect(() => {
    // Handle responsive resize
    const handleResize = () => setIsMobile(window.innerWidth < 768);
    window.addEventListener('resize', handleResize);

    // Initial theme set or update if theme state changes
    document.documentElement.setAttribute('data-theme', theme);

    return () => window.removeEventListener('resize', handleResize);
  }, [theme]); // Re-run if theme changes to update data-theme attribute

  const toggleTheme = () => {
    const newTheme = theme === 'dark' ? 'light' : 'dark';
    setTheme(newTheme);
    localStorage.setItem('theme', newTheme);
    // document.documentElement.setAttribute('data-theme', newTheme); // This is now handled by the useEffect
  };

  return (
    <Router>
      <div className="app-container">
        <Routes>
          <Route path="/" element={user ? <Navigate to="/dashboard" replace /> : <Login onLogin={setUser} />} />
          <Route
            path="/dashboard"
            element={
              user ? (
                isMobile ? (
                  <MobileDashboard user={user} theme={theme} toggleTheme={toggleTheme} />
                ) : (
                  <Dashboard user={user} theme={theme} toggleTheme={toggleTheme} />
                )
              ) : (
                <Navigate to="/" replace />
              )
            }
          />
          <Route
            path="/patient/:patientId"
            element={
              user ? (
                <Dashboard user={user} theme={theme} toggleTheme={toggleTheme} forceDetail={true} />
              ) : (
                <Navigate to="/" replace />
              )
            }
          />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
