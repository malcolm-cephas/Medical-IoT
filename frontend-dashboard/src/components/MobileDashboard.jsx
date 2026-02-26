import React, { useEffect, useState } from 'react';
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
import PatientVitalList from './PatientVitalList';
import PatientStatistics from './PatientStatistics';
import PrescriptionPad from './PrescriptionPad';

ChartJS.register(
    CategoryScale,
    LinearScale,
    PointElement,
    LineElement,
    Title,
    Tooltip,
    Legend
);

const MobileDashboard = ({ user, theme, toggleTheme }) => {
    const [patientId, setPatientId] = useState(user.role === 'patient' ? user.username : 'patient_001');
    const [history, setHistory] = useState([]);
    const [loading, setLoading] = useState(true);
    const [isConnected, setIsConnected] = useState(false);
    const [activeTab, setActiveTab] = useState(user.role === 'patient' ? 'vitals' : 'ward');
    const [viewMode, setViewMode] = useState(user.role === 'patient' ? 'detail' : 'list');
    const [lockdown, setLockdown] = useState({ active: false, reason: '' });
    const [showPrescriptionPad, setShowPrescriptionPad] = useState(false);

    // WebSocket and polling logic (kept identical to PC for reliability)
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

        const socket = new SockJS(`${getBackendUrl()}/ws-vitals`);
        const stompClient = Stomp.over(socket);
        stompClient.debug = null;

        stompClient.connect({}, (frame) => {
            setIsConnected(true);
            stompClient.subscribe(`/topic/vitals/${patientId}`, (message) => {
                const newData = JSON.parse(message.body);
                setHistory(prev => [...prev, newData].slice(-30)); // Keep fewer points for mobile to save memory
            });
        }, () => setIsConnected(false));

        return () => {
            if (stompClient && stompClient.connected) stompClient.disconnect();
        };
    }, [patientId]);

    useEffect(() => {
        const checkLockdown = async () => {
            try {
                const response = await axios.get(`${getBackendUrl()}/api/security/status`);
                setLockdown({ active: response.data.isLockdown, reason: response.data.reason });
            } catch (e) { console.error(e); }
        };
        const interval = setInterval(checkLockdown, 5000);
        return () => clearInterval(interval);
    }, []);

    const currentData = history.length > 0 ? history[history.length - 1] : {
        heartRate: '--', spo2: '--', temperature: '--', humidity: '--'
    };

    const chartData = {
        labels: history.map((_, i) => i + 1),
        datasets: [
            {
                label: 'HR',
                data: history.map(d => d.heartRate),
                borderColor: '#f87171',
                tension: 0.4,
                pointRadius: 0,
            },
            {
                label: 'SpO2',
                data: history.map(d => d.spo2),
                borderColor: '#38bdf8',
                tension: 0.4,
                pointRadius: 0,
            }
        ]
    };

    const chartOptions = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { display: false } },
        scales: {
            x: { display: false },
            y: { grid: { color: 'rgba(148, 163, 184, 0.1)' } }
        }
    };

    if (lockdown.active) {
        return <div className="lockdown-mobile"><h1>🔒 SYSTEM LOCKED</h1><p>{lockdown.reason}</p></div>;
    }

    return (
        <div className="mobile-dashboard">
            {/* Mobile Top Header */}
            <header className="mobile-header">
                <div className="brand">
                    <span className="logo-icon">🏥</span>
                    <div>
                        <h2>MediSecure</h2>
                        <p className={isConnected ? 'status-on' : 'status-off'}>
                            {isConnected ? '● Connected' : '○ Offline'}
                        </p>
                    </div>
                </div>
                <button onClick={toggleTheme} className="theme-toggle-btn">
                    {theme === 'dark' ? '☀️' : '🌙'}
                </button>
            </header>

            {/* Main Content Area */}
            <main className="mobile-content">
                {activeTab === 'ward' && (user.role === 'doctor' || user.role === 'nurse') && (
                    <div className="ward-view animate-fade-in">
                        <PatientStatistics theme={theme} />
                        <PatientVitalList
                            theme={theme}
                            currentUser={user}
                            onSelectPatient={(id) => {
                                setPatientId(id);
                                setActiveTab('vitals');
                            }}
                        />
                    </div>
                )}

                {activeTab === 'vitals' && (
                    <div className="vitals-view animate-fade-in">
                        <div className="patient-banner">
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                <h3>Monitoring: {patientId}</h3>
                                {(user.role === 'doctor' || user.role === 'nurse') && (
                                    <div style={{ display: 'flex', gap: '0.5rem' }}>
                                        <button onClick={() => setShowPrescriptionPad(true)}
                                            style={{ background: '#ec4899', border: 'none', color: 'white', borderRadius: '4px', padding: '0.2rem 0.5rem', fontSize: '0.7rem' }}>
                                            💊
                                        </button>
                                        <button onClick={() => {
                                            const id = prompt('Enter Patient ID to monitor:');
                                            if (id) setPatientId(id);
                                        }} style={{ background: 'var(--accent-color)', border: 'none', color: 'white', borderRadius: '4px', padding: '0.2rem 0.5rem', fontSize: '0.7rem' }}>
                                            Switch
                                        </button>
                                        <button onClick={() => setActiveTab('ward')}
                                            style={{ background: '#64748b', border: 'none', color: 'white', borderRadius: '4px', padding: '0.2rem 0.5rem', fontSize: '0.7rem' }}>
                                            Ward
                                        </button>
                                    </div>
                                )}
                            </div>
                            <p>Last update: {new Date().toLocaleTimeString()}</p>
                        </div>

                        <div className="mobile-grid">
                            <div className={`m-card ${currentData.heartRate > 100 ? 'critical' : ''}`}>
                                <span className="m-icon">❤️</span>
                                <div className="m-data">
                                    <label>Heart Rate</label>
                                    <div className="m-value">{currentData.heartRate}<span className="m-unit">BPM</span></div>
                                </div>
                            </div>

                            <div className={`m-card ${currentData.spo2 < 95 ? 'critical' : ''}`}>
                                <span className="m-icon">💧</span>
                                <div className="m-data">
                                    <label>SpO2</label>
                                    <div className="m-value">{currentData.spo2}<span className="m-unit">%</span></div>
                                </div>
                            </div>

                            <div className="m-card">
                                <span className="m-icon">🌡️</span>
                                <div className="m-data">
                                    <label>Temp</label>
                                    <div className="m-value">{currentData.temperature !== '--' ? currentData.temperature.toFixed(1) : '--'}<span className="m-unit">°C</span></div>
                                </div>
                            </div>

                            <div className="m-card">
                                <span className="m-icon">☁️</span>
                                <div className="m-data">
                                    <label>Humidity</label>
                                    <div className="m-value">{currentData.humidity !== '--' ? currentData.humidity.toFixed(1) : '--'}<span className="m-unit">%</span></div>
                                </div>
                            </div>
                        </div>

                        {/* Quick Status */}
                        <div className="safety-badge">
                            {currentData.heartRate > 100 || currentData.spo2 < 95 ?
                                <span className="warn">🚨 Attention Required</span> :
                                <span className="safe">✅ Condition Stable</span>}
                        </div>
                    </div>
                )}

                {activeTab === 'trends' && (
                    <div className="trends-view animate-fade-in">
                        <div className="card m-chart-card">
                            <h3>Real-time Trend: {patientId}</h3>
                            <div className="m-chart-container">
                                {loading ? <p>Loading graph...</p> : <Line data={chartData} options={chartOptions} />}
                            </div>
                        </div>
                        <div className="chart-legend">
                            <span style={{ color: '#f87171' }}>● Heart Rate</span>
                            <span style={{ color: '#38bdf8' }}>● Oxygen Level</span>
                        </div>
                        <p style={{ textAlign: 'center', fontSize: '0.7rem', color: 'var(--text-secondary)', marginTop: '1rem' }}>
                            Showing last 30 data points
                        </p>
                    </div>
                )}

                {activeTab === 'security' && (
                    <div className="security-view animate-fade-in">
                        <div className="card m-security-card">
                            <h3>Security Status</h3>
                            <div className="security-item">
                                <label>User Role</label>
                                <span style={{ textTransform: 'uppercase' }}>{user.role}</span>
                            </div>
                            <div className="security-item">
                                <label>System Integrity</label>
                                <span className="green">Verified</span>
                            </div>
                            <div className="security-item">
                                <label>Encryption Policy</label>
                                <span>ABE 256-bit</span>
                            </div>
                            <button onClick={() => window.location.href = '/'} className="m-logout-btn">
                                Log Out System
                            </button>
                        </div>
                    </div>
                )}
                {showPrescriptionPad && (
                    <div style={{
                        position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
                        background: 'rgba(0,0,0,0.8)', zIndex: 2000,
                        display: 'flex', alignItems: 'center', justifyContent: 'center'
                    }}>
                        <div style={{ width: '95%', maxHeight: '90%', overflowY: 'auto', background: 'var(--card-bg)', borderRadius: '12px', padding: '1rem', position: 'relative' }}>
                            <button
                                onClick={() => setShowPrescriptionPad(false)}
                                style={{
                                    position: 'absolute', top: '10px', right: '10px',
                                    background: 'none', border: 'none', fontSize: '1.5rem', color: 'var(--text-primary)'
                                }}
                            >✕</button>
                            <PrescriptionPad
                                doctorId={user.username === 'doctor_micheal' ? 1 : 2} // Temp mapping
                                selectedPatientId={patientId}
                                onClose={() => setShowPrescriptionPad(false)}
                            />
                        </div>
                    </div>
                )}
            </main>

            {/* Mobile Bottom Navigation */}
            <nav className="mobile-nav">
                {(user.role === 'doctor' || user.role === 'nurse') && (
                    <button className={activeTab === 'ward' ? 'active' : ''} onClick={() => setActiveTab('ward')}>
                        <span className="nav-icon">🏥</span>
                        <span>Ward</span>
                    </button>
                )}
                <button className={activeTab === 'vitals' ? 'active' : ''} onClick={() => setActiveTab('vitals')}>
                    <span className="nav-icon">📈</span>
                    <span>Vitals</span>
                </button>
                <button className={activeTab === 'trends' ? 'active' : ''} onClick={() => setActiveTab('trends')}>
                    <span className="nav-icon">📊</span>
                    <span>Trends</span>
                </button>
                <button className={activeTab === 'security' ? 'active' : ''} onClick={() => setActiveTab('security')}>
                    <span className="nav-icon">🛡️</span>
                    <span>Security</span>
                </button>
            </nav>

            <style dangerouslySetInnerHTML={{
                __html: `
        .mobile-dashboard {
          display: flex;
          flex-direction: column;
          height: 100vh;
          background-color: var(--bg-color);
          overflow: hidden;
        }
        .mobile-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 1.2rem 1rem;
          background: var(--card-bg);
          border-bottom: 1px solid var(--card-border);
          box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        .brand { display: flex; align-items: center; gap: 0.8rem; }
        .logo-icon { font-size: 1.5rem; }
        .brand h2 { font-size: 1.1rem; margin: 0; }
        .brand p { font-size: 0.7rem; margin: 0; opacity: 0.8; }
        .status-on { color: var(--success-color); }
        .status-off { color: var(--danger-color); }
        .theme-toggle-btn { background: var(--input-bg); border: none; padding: 0.5rem; border-radius: 50%; width: 36px; height: 36px; display: flex; align-items: center; justify-content: center; font-size: 1.2rem; cursor: pointer; }
        
        .mobile-content { flex: 1; overflow-y: auto; padding: 1.2rem; padding-bottom: 80px; }
        .patient-banner { background: var(--card-bg); padding: 1rem; border-radius: 12px; margin-bottom: 1.5rem; border-left: 4px solid var(--accent-color); }
        .patient-banner h3 { font-size: 1rem; margin-bottom: 0.2rem; }
        .patient-banner p { font-size: 0.75rem; color: var(--text-secondary); }

        .mobile-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
        .m-card { background: var(--card-bg); padding: 1.2rem; border-radius: 16px; border: 1px solid var(--card-border); display: flex; flex-direction: column; gap: 0.5rem; position: relative; overflow: hidden; }
        .m-card.critical { border: 2px solid var(--danger-color); animation: pulse-border 2s infinite; }
        @keyframes pulse-border { 0% { box-shadow: 0 0 0 0 rgba(248, 113, 113, 0.4); } 70% { box-shadow: 0 0 0 10px rgba(248, 113, 113, 0); } 100% { box-shadow: 0 0 0 0 rgba(248, 113, 113, 0); } }
        .m-icon { font-size: 1.5rem; opacity: 0.8; }
        .m-data label { font-size: 0.7rem; color: var(--text-secondary); text-transform: uppercase; letter-spacing: 0.05em; }
        .m-value { font-size: 1.6rem; fontWeight: 800; }
        .m-unit { font-size: 0.8rem; margin-left: 0.2rem; color: var(--text-secondary); }

        .safety-badge { margin-top: 1.5rem; text-align: center; }
        .safety-badge span { padding: 0.6rem 1.2rem; border-radius: 30px; font-weight: 700; font-size: 0.9rem; }
        .safe { background: rgba(74, 222, 128, 0.15); color: var(--success-color); border: 1px solid var(--success-color); }
        .warn { background: rgba(248, 113, 113, 0.15); color: var(--danger-color); border: 1px solid var(--danger-color); }

        .m-chart-card { padding: 1.2rem; height: 320px; }
        .m-chart-container { height: 240px; margin-top: 1rem; }
        .chart-legend { display: flex; justify-content: center; gap: 1rem; font-size: 0.75rem; font-weight: 600; margin-top: 1rem; }

        .security-item { display: flex; justify-content: space-between; padding: 1rem 0; border-bottom: 1px solid var(--card-border); font-size: 0.9rem; }
        .security-item label { color: var(--text-secondary); }
        .m-logout-btn { width: 100%; margin-top: 2rem; padding: 1rem; background: var(--danger-color); color: white; border: none; border-radius: 12px; font-weight: 700; font-size: 1rem; }

        .mobile-nav { position: fixed; bottom: 0; left: 0; right: 0; height: 70px; background: var(--card-bg); display: flex; border-top: 1px solid var(--card-border); z-index: 1000; box-shadow: 0 -2px 10px rgba(0,0,0,0.1); }
        .mobile-nav button { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; background: none; border: none; font-size: 0.7rem; color: var(--text-secondary); gap: 0.3rem; transition: all 0.2s; }
        .mobile-nav button.active { color: var(--accent-color); }
        .nav-icon { font-size: 1.4rem; }

        .animate-fade-in { animation: fadeIn 0.3s ease-in; }
        @keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
        
        .lockdown-mobile { height: 100vh; display: flex; flex-direction: column; align-items: center; justify-content: center; background: var(--danger-color); color: white; padding: 2rem; text-align: center; }
      ` }} />
        </div>
    );
};

export default MobileDashboard;
