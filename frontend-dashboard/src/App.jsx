import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Dashboard from './components/Dashboard';
import MobileDashboard from './components/MobileDashboard';
import Login from './components/Login';

function App() {
  const [user, setUser] = useState(null);
  const [theme, setTheme] = useState('dark');
  const [isMobile, setIsMobile] = useState(window.innerWidth < 768);

  useEffect(() => {
    // Handle responsive resize
    const handleResize = () => setIsMobile(window.innerWidth < 768);
    window.addEventListener('resize', handleResize);

    // Load theme from local storage
    const savedTheme = localStorage.getItem('theme') || 'dark';
    setTheme(savedTheme);
    document.documentElement.setAttribute('data-theme', savedTheme);

    return () => window.removeEventListener('resize', handleResize);
  }, []);

  const toggleTheme = () => {
    const newTheme = theme === 'dark' ? 'light' : 'dark';
    setTheme(newTheme);
    localStorage.setItem('theme', newTheme);
    document.documentElement.setAttribute('data-theme', newTheme);
  };

  return (
    <Router>
      <div className="app-container">
        <Routes>
          <Route path="/" element={<Login onLogin={setUser} />} />
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
        </Routes>
      </div>
    </Router>
  );
}

export default App;
