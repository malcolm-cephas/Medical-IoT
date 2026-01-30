import React, { useEffect, useState } from 'react';
import axios from 'axios';
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

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend
);

const Dashboard = ({ user, theme, toggleTheme }) => {
  const [patientId, setPatientId] = useState('123'); // Default 
  const [viewMode, setViewMode] = useState(user.role === 'patient' ? 'detail' : 'list');
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

  // If role is patient, they can only see themselves
  useEffect(() => {
    if (user && user.role === 'patient') {
      // In a real app, use the actual signed-in user's ID
      // For demo, we might map 'patient_alpha' to '123' or just use the name
      setPatientId(user.username === 'patient_alpha' ? '123' : user.username);
    }
  }, [user]);

  // Poll for data every 2 seconds
  useEffect(() => {
    const fetchData = async () => {
      try {
        const response = await axios.get(`http://localhost:8080/api/sensor/history/${patientId}`);
        setHistory(response.data);
        setLoading(false);
        setIsConnected(true);
      } catch (error) {
        console.error("Error fetching data", error);
        setIsConnected(false);
      }
    };

    fetchData(); // Initial call
    const interval = setInterval(fetchData, 2000); // Polling
    return () => clearInterval(interval);
  }, [patientId]);

  const handleLogout = () => {
    window.location.href = '/'; // Simple reload to clear state
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
      {
        label: 'Temperature (°C)',
        data: history.map(d => d.temperature),
        borderColor: '#fb923c', // orange
        backgroundColor: 'rgba(251, 146, 60, 0.5)',
        tension: 0.4,
        yAxisID: 'y1',
      },
      {
        label: 'Humidity (%)',
        data: history.map(d => d.humidity),
        borderColor: '#a78bfa', // purple
        backgroundColor: 'rgba(167, 139, 250, 0.5)',
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
          text: 'HR / SpO2 / Humidity',
          color: theme === 'dark' ? '#94a3b8' : '#1e293b'
        },
        grid: { color: theme === 'dark' ? 'rgba(255, 255, 255, 0.1)' : 'rgba(0,0,0,0.1)' },
        ticks: { color: theme === 'dark' ? '#94a3b8' : '#1e293b' }
      },
      y1: {
        type: 'linear',
        display: true,
        position: 'right',
        title: {
          display: true,
          text: 'Temperature (°C)',
          color: theme === 'dark' ? '#94a3b8' : '#1e293b'
        },
        grid: {
          drawOnChartArea: false,
        },
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
        const response = await axios.get('http://localhost:8080/api/security/status');
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
    window.open('http://localhost:8080/api/export/logs/csv', '_blank');
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
        await axios.post('http://localhost:8080/api/emergency/override', {
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
        const res = await axios.get('http://localhost:8080/api/performance/metrics');
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
            <h2>{user.role === 'doctor' ? 'Doctor Dashboard' : user.role === 'nurse' ? 'Nurse Station' : 'My Health Vitals'}</h2>
            <p style={{ color: 'var(--text-secondary)' }}>
              {user.role === 'doctor' ? 'Select a patient to view real-time records.' :
                user.role === 'nurse' ? 'Monitoring ward stations.' :
                  'View your real-time health statistics.'}
            </p>
          </div>
          {(user.role === 'doctor' || user.role === 'nurse') && viewMode === 'detail' && (
            <div style={{ display: 'flex', gap: '0.5rem' }}>
              <button
                onClick={exportPatientData}
                className="btn-secondary"
                style={{ padding: '0.5rem 1rem', background: 'var(--success-color)', color: 'white' }}
              >
                📥 Export CSV
              </button>
              <button
                onClick={() => setViewMode('list')}
                className="btn-secondary"
                style={{ padding: '0.5rem 1rem' }}
              >
                ⬅️ Back to Monitoring
              </button>
            </div>
          )}
        </div>

        {/* Ward Monitoring List (Only for Doctor/Nurse in List Mode) */}
        {(user.role === 'doctor' || user.role === 'nurse') && viewMode === 'list' && (
          <>
            <PatientStatistics theme={theme} />
            <PatientVitalList
              theme={theme}
              currentUser={user}
              onSelectPatient={(id) => {
                setPatientId(id);
                setViewMode('detail');
              }}
            />
          </>
        )}

        {/* Detailed Patient View */}
        {viewMode === 'detail' && (
          <>
            {/* Patient Selection (Only for Doctor/Nurse) */}
            {(user.role === 'doctor' || user.role === 'nurse') && (
              <div className="form-group" style={{ maxWidth: '300px' }}>
                <label>Monitor Patient ID:</label>
                <div style={{ display: 'flex', gap: '0.5rem' }}>
                  <input
                    type="text"
                    value={patientId}
                    onChange={(e) => setPatientId(e.target.value)}
                    placeholder="Enter ID"
                  />
                  <select
                    onChange={(e) => setPatientId(e.target.value)}
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

            {/* New Feature: Secure Image Transfer */}
            <SecureImageTransfer theme={theme} />

            {/* Consent Management for Patients */}
            {user.role === 'patient' && (
              <ConsentManagement patientId={user.username} theme={theme} />
            )}
          </>
        )}

      </main>
    </div>
  );
};

export default Dashboard;
