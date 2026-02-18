import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';
import { getBackendUrl } from '../config';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
} from 'chart.js';
import { Line } from 'react-chartjs-2';
import SecureImageTransfer from './SecureImageTransfer';
import PatientVitalList from './PatientVitalList';
import ConsentManagement from './ConsentManagement';
import PatientStatistics from './PatientStatistics';
import AppointmentBooking from './AppointmentBooking';
import DoctorAvailability from './DoctorAvailability';
import SecurityAudit from './SecurityAudit';
import SystemActivities from './SystemActivities';

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend
);

const Dashboard = ({ user, theme, toggleTheme, forceDetail }) => {
  const { patientId: urlPatientId } = useParams();
  const navigate = useNavigate();

  const [patientId, setPatientId] = useState(urlPatientId || (user.role === 'patient' ? user.username : 'patient_001'));
  const [viewMode, setViewMode] = useState(forceDetail || urlPatientId || user.role === 'patient' ? 'detail' : 'list');
  const [activeTab, setActiveTab] = useState('vitals'); // vitals, consent, images, appointments
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isConnected, setIsConnected] = useState(false);
  const [notificationsEnabled, setNotificationsEnabled] = useState(window.Notification ? window.Notification.permission === 'granted' : false);
  const [lastCriticalId, setLastCriticalId] = useState(null);

  const requestNotificationPermission = () => {
    if (!window.Notification) return;
    window.Notification.requestPermission().then(permission => {
      setNotificationsEnabled(permission === 'granted');
    });
  };

  useEffect(() => {
    if (history.length > 0 && notificationsEnabled && viewMode === 'detail') {
      const latest = history[history.length - 1];
      const isCritical = latest.heartRate > 100 || latest.spo2 < 95 || latest.temperature > 37.5;

      // Use record ID or timestamp to prevent duplicate notifications for the same data point
      const recordId = latest.id || history.length;

      if (isCritical && lastCriticalId !== recordId) {
        new window.Notification("🚨 CRITICAL ALERT!", {
          body: `Patient ${patientId} is in critical condition!\nHR: ${latest.heartRate} | SpO2: ${latest.spo2}% | Temp: ${latest.temperature}°C`,
        });
        setLastCriticalId(recordId);
      }
    }
  }, [history, notificationsEnabled, patientId, viewMode]);

  // Synchronize state with URL changes
  useEffect(() => {
    if (urlPatientId) {
      setPatientId(urlPatientId);
      setViewMode('detail');
    } else if (user.role !== 'patient' && !forceDetail) {
      setViewMode('list');
    }
  }, [urlPatientId, user.role, forceDetail]);

  // If role is patient, they can only see themselves
  useEffect(() => {
    if (user && user.role === 'patient') {
      setPatientId(user.username);
    }
  }, [user]);

  // Initial fetch and WebSocket connection
  useEffect(() => {
    const fetchData = async () => {
      try {
        const response = await axios.get(`${getBackendUrl()}/api/sensor/history/${patientId}`);
        setHistory(response.data);
        setLoading(false);
        setIsConnected(true);
      } catch (error) {
        console.error("Error fetching data", error);
        setIsConnected(false);
      }
    };

    fetchData();

    // WebSocket Setup
    const socket = new SockJS(`${getBackendUrl()}/ws-vitals`);
    const stompClient = Stomp.over(socket);
    stompClient.debug = null;

    stompClient.connect({}, (frame) => {
      setIsConnected(true);

      // Subscribe to this patient's vitals
      stompClient.subscribe(`/topic/vitals/${patientId}`, (message) => {
        const newData = JSON.parse(message.body);
        setHistory(prev => [...prev, newData].slice(-50)); // Keep last 50 for trends
      });

      // Subscribe to security alerts/lockdown
      stompClient.subscribe('/topic/alerts', (message) => {
        const alert = JSON.parse(message.body);
        if (alert.patientId === patientId || user.role !== 'patient') {
          if (notificationsEnabled) {
            new window.Notification("🚨 MEDICAL ALERT", {
              body: `Patient ${alert.patientId}: ${alert.anomalies.join(', ')}`,
            });
          }
        }
      });
    }, (error) => {
      console.error("WebSocket Error:", error);
      setIsConnected(false);
    });

    return () => {
      if (stompClient && stompClient.connected) {
        stompClient.disconnect();
      }
    };
  }, [patientId, notificationsEnabled, user.role]);

  const handleLogout = () => {
    localStorage.removeItem('user');
    localStorage.removeItem('token');
    window.location.href = '/';
  };

  // Chart Configuration
  const chartData = {
    labels: history.map((_, index) => index + 1),
    datasets: [
      {
        label: 'Heart Rate (BPM)',
        data: history.map(d => d.heartRate),
        borderColor: '#f87171', // danger-color
        backgroundColor: 'rgba(248, 113, 113, 0.5)',
        tension: 0.4,
        yAxisID: 'y',
      },
      {
        label: 'SpO2 (%)',
        data: history.map(d => d.spo2),
        borderColor: '#38bdf8', // accent-color
        backgroundColor: 'rgba(56, 189, 248, 0.5)',
        tension: 0.4,
        yAxisID: 'y',
      },
    ],
  };

  const options = {
    responsive: true,
    maintainAspectRatio: false,
    interaction: {
      mode: 'index',
      intersect: false,
    },
    plugins: {
      legend: {
        position: 'top',
        labels: { color: theme === 'dark' ? '#94a3b8' : '#1e293b' }
      },
      title: {
        display: false,
      },
    },
    scales: {
      x: {
        grid: { color: theme === 'dark' ? 'rgba(255, 255, 255, 0.1)' : 'rgba(0,0,0,0.1)' },
        ticks: { color: theme === 'dark' ? '#94a3b8' : '#1e293b' }
      },
      y: {
        type: 'linear',
        display: true,
        position: 'left',
        title: {
          display: true,
          text: 'HR / SpO2',
          color: theme === 'dark' ? '#94a3b8' : '#1e293b'
        },
        grid: { color: theme === 'dark' ? 'rgba(255, 255, 255, 0.1)' : 'rgba(0,0,0,0.1)' },
        ticks: { color: theme === 'dark' ? '#94a3b8' : '#1e293b' }
      }
    }
  };

  const currentData = history.length > 0 ? history[history.length - 1] : {
    heartRate: '--',
    spo2: '--',
    temperature: '--',
    humidity: '--',
    systolicBP: '--',
    diastolicBP: '--'
  };

  // Lockdown State
  const [lockdown, setLockdown] = useState({ active: false, reason: '' });
  const [showPerf, setShowPerf] = useState(false);
  const [perfMetrics, setPerfMetrics] = useState(null);

  // Poll for Lockdown Status
  useEffect(() => {
    const checkLockdown = async () => {
      try {
        const response = await axios.get(`${getBackendUrl()}/api/security/status`);
        setLockdown({
          active: response.data.isLockdown,
          reason: response.data.reason
        });
      } catch (error) {
        console.error("Error checking security status", error);
      }
    };

    checkLockdown();
    const interval = setInterval(checkLockdown, 3000);
    return () => clearInterval(interval);
  }, []);

  const handleExport = () => {
    window.open(`${getBackendUrl()}/api/export/logs/csv`, '_blank');
  };

  const exportPatientData = () => {
    // Convert history to CSV
    if (history.length === 0) {
      alert('No data to export');
      return;
    }

    const headers = ['Timestamp', 'Patient ID', 'Heart Rate', 'SpO2', 'Temperature', 'Humidity', 'Systolic BP', 'Diastolic BP'];
    const csvRows = [headers.join(',')];

    history.forEach((record, index) => {
      const row = [
        index + 1,
        patientId,
        record.heartRate || '',
        record.spo2 || '',
        record.temperature || '',
        record.humidity || '',
        record.systolicBP || '',
        record.diastolicBP || ''
      ];
      csvRows.push(row.join(','));
    });

    const csvContent = csvRows.join('\n');
    const blob = new Blob([csvContent], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `patient_${patientId}_vitals_${new Date().toISOString().split('T')[0]}.csv`;
    a.click();
    window.URL.revokeObjectURL(url);
  };

  const handleEmergency = async () => {
    const reason = prompt("🚨 BREAK-GLASS EMERGENCY 🚨\n\nPlease enter the medical reason for immediate access override:");
    if (reason) {
      try {
        await axios.post(`${getBackendUrl()}/api/emergency/override`, {
          doctorId: user.username,
          patientId: patientId,
          reason: reason
        });
        alert("ACCESS GRANTED. Emergency event has been logged on the blockchain.");
      } catch (err) {
        alert("Failed to grant emergency access.");
      }
    }
  };

  const togglePerformance = async () => {
    if (!showPerf) {
      try {
        const res = await axios.get(`${getBackendUrl()}/api/performance/metrics`);
        setPerfMetrics(res.data);
      } catch (e) { console.error(e); }
    }
    setShowPerf(!showPerf);
  };

  if (lockdown.active) {
    return (
      <div style={{
        position: 'fixed', top: 0, left: 0, width: '100vw', height: '100vh',
        backgroundColor: 'rgba(220, 38, 38, 0.95)', color: 'white',
        display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center',
        zIndex: 9999
      }}>
        <h1>🔒 SYSTEM LOCKED DOWN</h1>
        <h2 style={{ marginTop: '1rem' }}>SECURITY ALERT: INTRUSION DETECTED</h2>
        <p style={{ fontSize: '1.2rem', marginTop: '1rem' }}>Reason: {lockdown.reason}</p>
        <p>Please contact System Administrator.</p>
      </div>
    );
  }

  return (
    <div className="container">
      <header>
        <div className="logo">
          <h1>Medi<span className="highlight">Secure</span> IoT</h1>
          <p>Decentralized Health Monitoring</p>
        </div>

        <div className="user-controls" style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>

          {/* Notification Alert Toggle */}
          {!notificationsEnabled && window.Notification && (
            <button onClick={requestNotificationPermission} style={{
              background: '#f59e0b', color: 'white', border: 'none',
              padding: '0.25rem 0.75rem', borderRadius: '4px', cursor: 'pointer', fontSize: '0.8rem'
            }}>
              🔔 Enable Alerts
            </button>
          )}

          {notificationsEnabled && (
            <span style={{ fontSize: '0.8rem', color: 'var(--success-color)' }}>🔔 Alerts On</span>
          )}

          {/* Emergency Button (Doctor Only) */}
          {(user.role === 'doctor' || user.role === 'admin') && (
            <button onClick={handleEmergency} style={{
              background: 'var(--danger-color)', color: 'white', border: 'none',
              padding: '0.25rem 0.75rem', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold'
            }}>
              🚨 BREAK GLASS
            </button>
          )}

          {/* Admin Tools */}
          {user.role === 'admin' && (
            <>
              <button onClick={handleExport} style={{
                background: 'var(--accent-color)', color: 'white', border: 'none',
                padding: '0.25rem 0.75rem', borderRadius: '4px', cursor: 'pointer'
              }}>
                📥 Export Logs (CSV)
              </button>
              <button onClick={togglePerformance} style={{
                background: '#64748b', color: 'white', border: 'none',
                padding: '0.25rem 0.75rem', borderRadius: '4px', cursor: 'pointer'
              }}>
                📊 Performance
              </button>
            </>
          )}

          {/* Theme Toggle */}
          <div className="theme-switch-wrapper">
            <label className="theme-switch" htmlFor="checkbox">
              <input type="checkbox" id="checkbox" checked={theme === 'light'} onChange={toggleTheme} />
              <div className="slider"></div>
            </label>
          </div>

          <div className="status-indicator">
            <span className={`status ${isConnected ? 'connected' : 'disconnected'}`}>
              {isConnected ? 'Connected' : 'Disconnected'}
            </span>
          </div>

          <span style={{ fontSize: '0.9rem', color: 'var(--text-secondary)' }}>
            {user.role === 'doctor' ? '👨‍⚕️' : user.role === 'nurse' ? '👩‍⚕️' : '👤'}
            <strong> {user.username}</strong>
          </span>
          <button onClick={handleLogout} className="btn-logout" style={{
            background: 'transparent',
            border: '1px solid var(--danger-color)',
            color: 'var(--danger-color)',
            padding: '0.25rem 0.75rem',
            borderRadius: '4px',
            cursor: 'pointer'
          }}>Logout</button>
        </div>
      </header>

      {/* Performance Modal */}
      {showPerf && perfMetrics && (
        <div style={{
          position: 'absolute', top: '80px', right: '20px', width: '300px',
          background: theme === 'dark' ? '#1e293b' : 'white',
          border: '1px solid var(--card-border)', borderRadius: '8px', padding: '1rem',
          boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)', zIndex: 100
        }}>
          <h3 style={{ borderBottom: '1px solid var(--card-border)', paddingBottom: '0.5rem' }}>⚡ System Benchmarks</h3>
          <ul style={{ listStyle: 'none', padding: 0, marginTop: '0.5rem', fontSize: '0.9rem' }}>
            <li><strong>Encryption:</strong> {perfMetrics.encryption_time_avg_ms} ms</li>
            <li><strong>Decryption:</strong> {perfMetrics.decryption_time_avg_ms} ms</li>
            <li><strong>API Latency:</strong> {perfMetrics.api_latency_avg_ms} ms</li>
            <li><strong>Blockchain Time:</strong> {perfMetrics.blockchain_log_time_ms} ms</li>
            <li><strong>Throughput:</strong> {perfMetrics.throughput_req_per_sec} req/s</li>
          </ul>
        </div>
      )}

      <main>
        <div style={{ marginBottom: '1.5rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div>
            <h2>{user.role === 'doctor' ? 'Doctor Dashboard' : user.role === 'nurse' ? 'Nurse Station' : user.role === 'admin' ? 'System Administrator' : 'My Health Vitals'}</h2>
            <p style={{ color: 'var(--text-secondary)' }}>
              {user.role === 'doctor' ? 'Select a patient to view real-time records.' :
                user.role === 'nurse' ? 'Monitoring ward stations.' :
                  user.role === 'admin' ? 'System Overview and Logs.' :
                    'View your real-time health statistics.'}
            </p>
          </div>
          {(user.role === 'doctor' || user.role === 'nurse' || user.role === 'admin') && viewMode === 'detail' && (
            <div style={{ display: 'flex', gap: '0.5rem' }}>
              <button
                onClick={exportPatientData}
                className="btn-secondary"
                style={{ padding: '0.5rem 1rem', background: 'var(--success-color)', color: 'white' }}
              >
                📥 Export CSV
              </button>
              <button
                onClick={() => navigate('/dashboard')}
                className="btn-secondary"
                style={{ padding: '0.5rem 1rem' }}
              >
                ⬅️ Back to Monitoring
              </button>
            </div>
          )}
        </div>

        {/* Tab Navigation */}
        <div className="tab-navigation" style={{
          display: 'flex',
          gap: '0.5rem',
          marginBottom: '1.5rem',
          borderBottom: '2px solid var(--card-border)',
          paddingBottom: '0.5rem'
        }}>
          <button
            className={`tab-btn ${activeTab === 'vitals' ? 'active' : ''}`}
            onClick={() => setActiveTab('vitals')}
            style={{
              padding: '0.5rem 1rem',
              border: 'none',
              background: activeTab === 'vitals' ? 'var(--accent-color)' : 'transparent',
              color: activeTab === 'vitals' ? 'white' : 'var(--text-secondary)',
              borderRadius: '8px 8px 0 0',
              cursor: 'pointer',
              fontWeight: activeTab === 'vitals' ? '600' : '400',
              transition: 'all 0.3s ease'
            }}
          >
            📊 Vitals
          </button>
          <button
            className={`tab-btn ${activeTab === 'consent' ? 'active' : ''}`}
            onClick={() => setActiveTab('consent')}
            style={{
              padding: '0.5rem 1rem',
              border: 'none',
              background: activeTab === 'consent' ? 'var(--accent-color)' : 'transparent',
              color: activeTab === 'consent' ? 'white' : 'var(--text-secondary)',
              borderRadius: '8px 8px 0 0',
              cursor: 'pointer',
              fontWeight: activeTab === 'consent' ? '600' : '400',
              transition: 'all 0.3s ease'
            }}
          >
            🔐 Consent
          </button>
          <button
            className={`tab-btn ${activeTab === 'images' ? 'active' : ''}`}
            onClick={() => setActiveTab('images')}
            style={{
              padding: '0.5rem 1rem',
              border: 'none',
              background: activeTab === 'images' ? 'var(--accent-color)' : 'transparent',
              color: activeTab === 'images' ? 'white' : 'var(--text-secondary)',
              borderRadius: '8px 8px 0 0',
              cursor: 'pointer',
              fontWeight: activeTab === 'images' ? '600' : '400',
              transition: 'all 0.3s ease'
            }}
          >
            🖼️ Images
          </button>
          <button
            className={`tab-btn ${activeTab === 'appointments' ? 'active' : ''}`}
            onClick={() => setActiveTab('appointments')}
            style={{
              padding: '0.5rem 1rem',
              border: 'none',
              background: activeTab === 'appointments' ? 'var(--accent-color)' : 'transparent',
              color: activeTab === 'appointments' ? 'white' : 'var(--text-secondary)',
              borderRadius: '8px 8px 0 0',
              cursor: 'pointer',
              fontWeight: activeTab === 'appointments' ? '600' : '400',
              transition: 'all 0.3s ease'
            }}
          >
            📅 Appointments
          </button>
          <button
            className={`tab-btn ${activeTab === 'security' ? 'active' : ''}`}
            onClick={() => setActiveTab('security')}
            style={{
              padding: '0.5rem 1rem',
              border: 'none',
              background: activeTab === 'security' ? 'var(--accent-color)' : 'transparent',
              color: activeTab === 'security' ? 'white' : 'var(--text-secondary)',
              borderRadius: '8px 8px 0 0',
              cursor: 'pointer',
              fontWeight: activeTab === 'security' ? '600' : '400',
              transition: 'all 0.3s ease'
            }}
          >
            🛡️ Security Audit
          </button>
          {user.role === 'admin' && (
            <button
              className={`tab-btn ${activeTab === 'activities' ? 'active' : ''}`}
              onClick={() => setActiveTab('activities')}
              style={{
                padding: '0.5rem 1rem',
                border: 'none',
                background: activeTab === 'activities' ? 'var(--accent-color)' : 'transparent',
                color: activeTab === 'activities' ? 'white' : 'var(--text-secondary)',
                borderRadius: '8px 8px 0 0',
                cursor: 'pointer',
                fontWeight: activeTab === 'activities' ? '600' : '400',
                transition: 'all 0.3s ease'
              }}
            >
              🕵️ System Logs
            </button>
          )}
        </div>

        {/* Appointments Tab */}
        {activeTab === 'appointments' && (
          <>
            {user.role === 'doctor' ? (
              <DoctorAvailability user={user} />
            ) : (
              <AppointmentBooking user={user} />
            )}
          </>
        )}

        {/* Consent Tab */}
        {activeTab === 'consent' && (
          <ConsentManagement patientId={patientId} currentUser={user} theme={theme} />
        )}

        {/* Images Tab */}
        {activeTab === 'images' && (
          <SecureImageTransfer currentUser={user} theme={theme} />
        )}

        {/* Security Audit Tab */}
        {activeTab === 'security' && (
          <SecurityAudit theme={theme} />
        )}

        {/* System Activities Tab */}
        {activeTab === 'activities' && user.role === 'admin' && (
          <SystemActivities theme={theme} />
        )}

        {/* Vitals Tab (existing content) */}
        {activeTab === 'vitals' && (
          <>


            {/* Ward Monitoring List (Only for Doctor/Nurse/Admin in List Mode) */}
            {(user.role === 'doctor' || user.role === 'nurse' || user.role === 'admin') && viewMode === 'list' && (
              <>
                <PatientStatistics theme={theme} />
                <PatientVitalList
                  theme={theme}
                  currentUser={user}
                  onSelectPatient={(id) => {
                    navigate(`/patient/${id}`);
                  }}
                />
              </>
            )}

            {/* Detailed Patient View */}
            {viewMode === 'detail' && (
              <>
                {/* Patient Selection (Only for Doctor/Nurse/Admin) */}
                {(user.role === 'doctor' || user.role === 'nurse' || user.role === 'admin') && (
                  <div className="form-group" style={{ maxWidth: '300px' }}>
                    <label>Monitor Patient ID:</label>
                    <div style={{ display: 'flex', gap: '0.5rem' }}>
                      <input
                        type="text"
                        value={patientId}
                        onChange={(e) => navigate(`/patient/${e.target.value}`)}
                        placeholder="Enter ID"
                      />
                      <select
                        onChange={(e) => navigate(`/patient/${e.target.value}`)}
                        style={{
                          padding: '0.5rem',
                          borderRadius: '4px',
                          border: '1px solid var(--input-border)',
                          backgroundColor: 'var(--card-bg)', // adaptive
                          color: 'var(--text-primary)'
                        }}
                      >
                        <option value="">Select...</option>
                        <option value="123">Patient 123 (Demo)</option>
                        <option value="124">Patient 124 (Cardiac)</option>
                        <option value="125">Patient 125 (Asthma)</option>
                        <option value="999">Patient 999 (Emergency)</option>
                      </select>
                    </div>
                  </div>
                )}

                {/* Vital Signs Grid */}
                <div className="vitals-grid">
                  <div className="card vital-card">
                    <div className="icon">❤️</div>
                    <div className="vital-info">
                      <h3>Heart Rate</h3>
                      <div className="value-unit">
                        <span className="value">{currentData.heartRate}</span>
                        <span className="unit"> BPM</span>
                      </div>
                    </div>
                  </div>

                  <div className="card vital-card">
                    <div className="icon">💧</div>
                    <div className="vital-info">
                      <h3>SpO2</h3>
                      <div className="value-unit">
                        <span className="value">{currentData.spo2}</span>
                        <span className="unit"> %</span>
                      </div>
                    </div>
                  </div>

                  <div className="card vital-card">
                    <div className="icon">🌡️</div>
                    <div className="vital-info">
                      <h3>Temperature</h3>
                      <div className="value-unit">
                        <span className="value" style={{
                          color: currentData.temperature > 37.5 ? 'var(--danger-color)' :
                            currentData.temperature < 36.0 ? 'orange' : 'inherit'
                        }}>
                          {currentData.temperature !== '--' ? currentData.temperature.toFixed(1) : '--'}
                        </span>
                        <span className="unit"> °C</span>
                      </div>
                      {currentData.temperature > 37.5 && (
                        <div style={{ fontSize: '0.7rem', color: 'var(--danger-color)', marginTop: '0.25rem' }}>
                          ⚠️ Fever Detected
                        </div>
                      )}
                    </div>
                  </div>

                  <div className="card vital-card">
                    <div className="icon">☁️</div>
                    <div className="vital-info">
                      <h3>Humidity</h3>
                      <div className="value-unit">
                        <span className="value" style={{
                          color: currentData.humidity < 30 || currentData.humidity > 60 ? 'orange' : 'inherit'
                        }}>
                          {currentData.humidity !== '--' ? currentData.humidity.toFixed(1) : '--'}
                        </span>
                        <span className="unit"> %</span>
                      </div>
                      {currentData.humidity !== '--' && (currentData.humidity < 30 || currentData.humidity > 60) && (
                        <div style={{ fontSize: '0.7rem', color: 'orange', marginTop: '0.25rem' }}>
                          ⚠️ {currentData.humidity < 30 ? 'Too Dry' : 'Too Humid'}
                        </div>
                      )}
                    </div>
                  </div>

                  <div className="card vital-card">
                    <div className="icon">🩺</div>
                    <div className="vital-info">
                      <h3>Blood Pressure</h3>
                      <div className="value-unit">
                        <span className="value" style={{
                          color: currentData.systolicBP > 140 || currentData.diastolicBP > 90 ? 'var(--danger-color)' :
                            currentData.systolicBP < 90 || currentData.diastolicBP < 60 ? 'orange' : 'inherit'
                        }}>
                          {currentData.systolicBP || '--'}/{currentData.diastolicBP || '--'}
                        </span>
                        <span className="unit"> mmHg</span>
                      </div>
                    </div>
                  </div>
                </div>

                {/* Chart Section */}
                <div className="card chart-card">
                  <div className="card-header">
                    <h2>Real-time Trends</h2>
                    <div className="live-badge">LIVE</div>
                  </div>
                  <div className="chart-container">
                    {loading ? <p style={{ color: 'var(--text-secondary)' }}>Loading data...</p> : <Line options={options} data={chartData} />}
                  </div>
                </div>

                {/* Risk Log Section */}
                <div className="card" style={{ marginTop: '2rem' }}>
                  <div className="card-header">
                    <h2>Risk Alert Log</h2>
                  </div>
                  <ul style={{ listStyle: 'none', maxHeight: '200px', overflowY: 'auto' }}>
                    {history.slice().reverse().map((record, i) => {
                      const isRisk = record.heartRate > 100 || record.spo2 < 95;
                      return (
                        <li key={i} style={{
                          padding: '0.75rem',
                          borderBottom: '1px solid var(--card-border)',
                          color: isRisk ? 'var(--danger-color)' : 'var(--success-color)',
                          display: 'flex',
                          justifyContent: 'space-between'
                        }}>
                          <span>
                            <strong>{isRisk ? 'HIGH RISK' : 'NORMAL'}</strong> - HR: {record.heartRate} | SpO2: {record.spo2}%
                          </span>
                          <small style={{ color: 'var(--text-secondary)' }}>{new Date().toLocaleTimeString()}</small>
                        </li>
                      );
                    })}
                  </ul>
                </div>
              </>
            )}
          </>
        )}

      </main>
    </div>
  );
};

export default Dashboard;

