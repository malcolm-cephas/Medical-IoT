import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import axios from 'axios';
import Dashboard from './components/Dashboard';
import MobileDashboard from './components/MobileDashboard';
import Login from './components/Login';

/**
 * Helper function to retrieve the initial user state from local storage.
 * This runs synchronously when the app starts to prevent flickering of the login screen.
 *
 * @returns {Object|null} The user object if found and valid, otherwise null.
 */
const getInitialUser = () => {
  const savedUser = localStorage.getItem('user');
  const savedToken = localStorage.getItem('token');

  if (savedUser && savedToken) {
    try {
      const user = JSON.parse(savedUser);
      return user;
    } catch (e) {
      localStorage.removeItem('user');
      localStorage.removeItem('token');
      return null;
    }
  }
  return null;
};

// Add a request interceptor to dynamically attach the token to every request
axios.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

/**
 * Main Application Component.
 * Handles the overall layout, routing, and global state (user, theme, responsiveness).
 */
function App() {
  // State for the authenticated user (initialized from localStorage)
  const [user, setUser] = useState(getInitialUser());

  // State for the application theme (light/dark), default to dark or saved preference
  const [theme, setTheme] = useState(localStorage.getItem('theme') || 'dark');

  // State to track if the view is mobile (width < 768px)
  const [isMobile, setIsMobile] = useState(window.innerWidth < 768);

  // Effect hook to handle window resizing and update mobile state
  useEffect(() => {
    const handleResize = () => setIsMobile(window.innerWidth < 768);
    window.addEventListener('resize', handleResize);

    // Initial theme set or update if theme state changes
    // Sets a data attribute on the <html> element for CSS querying
    document.documentElement.setAttribute('data-theme', theme);

    // Cleanup listener on component unmount
    return () => window.removeEventListener('resize', handleResize);
  }, [theme]); // Re-run if theme changes to update data-theme attribute

  /**
   * Toggles the application theme between light and dark.
   * Persists the choice to localStorage.
   */
  const toggleTheme = () => {
    const newTheme = theme === 'dark' ? 'light' : 'dark';
    setTheme(newTheme);
    localStorage.setItem('theme', newTheme);
    // Attribute update is handled by the useEffect above
  };

  return (
    <Router>
      <div className="app-container">
        <Routes>
          {/* 
            Root Route: 
            If user is logged in, redirect to dashboard.
            If not, show Login component.
          */}
          <Route path="/" element={user ? <Navigate to="/dashboard" replace /> : <Login onLogin={setUser} />} />

          {/* 
            Dashboard Route:
            Protected route - redirects to / if no user.
            Conditionally renders MobileDashboard or Dashboard based on screen size.
          */}
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

          {/* 
            Patient Detail Route:
            Opens the Dashboard forcing detail view for a specific patient.
          */}
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
