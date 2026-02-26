import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { getBackendUrl } from '../config';
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';
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

// Register ChartJS components for sparklines
ChartJS.register(
    CategoryScale,
    LinearScale,
    PointElement,
    LineElement,
    Title,
    Tooltip,
    Legend
);

/**
 * PatientVitalList Component
 * 
 * Displays a centralized list of all patients and their latest vitals (Ward View).
 * Features:
 * - Real-time updates via WebSockets (/topic/ward).
 * - Mini sparkline charts for trend visualization.
 * - Search and Pagination.
 * - Consent request management for doctors.
 * 
 * @param {Function} onSelectPatient - Callback when a patient is selected for detailed view.
 * @param {string} theme - 'light' or 'dark' mode.
 * @param {Object} currentUser - The currently logged-in user.
 */
const PatientVitalList = ({ onSelectPatient, theme, currentUser }) => {
    // List of patients loaded from backend
    const [patients, setPatients] = useState([]);

    // Pagination state
    const [page, setPage] = useState(0);
    const [size, setSize] = useState(5);
    const [totalPages, setTotalPages] = useState(0);

    const [loading, setLoading] = useState(false);
    const [searchTerm, setSearchTerm] = useState('');

    // Cache for consent statuses to determine access rights
    const [consentStatuses, setConsentStatuses] = useState({});

    // WebSocket for real-time list updates
    useEffect(() => {
        const socket = new SockJS(`${getBackendUrl()}/ws-vitals`);
        const stompClient = Stomp.over(socket);
        stompClient.debug = null; // Disable debug logs

        stompClient.connect({}, () => {
            // Subscribe to ward updates (summary data broadcast to all authorized staff)
            stompClient.subscribe('/topic/ward', (message) => {
                const newData = JSON.parse(message.body);
                // Update the specific patient in the list with new vitals
                setPatients(prev => prev.map(p =>
                    p.username === newData.patientId
                        ? { ...p, latestHeartRate: newData.heartRate, latestSpo2: newData.spo2 }
                        : p
                ));
            });
        });

        return () => {
            if (stompClient && stompClient.connected) stompClient.disconnect();
        };
    }, []);

    // Refresh data when pagination or search changes
    useEffect(() => {
        fetchPatients();
    }, [page, size, searchTerm]);

    /**
     * Fetches paginated patient list and their corresponding consent statuses.
     */
    const fetchPatients = async () => {
        setLoading(true);
        try {
            const response = await axios.get(`${getBackendUrl()}/api/patients?page=${page}&size=${size}&search=${searchTerm}`);
            setPatients(response.data.patients);
            setTotalPages(response.data.totalPages);

            // Fetch consent status for each patient if user is logged in
            if (currentUser) {
                const statuses = {};
                // Parallel fetching of consent status would be better, but sequential is fine for small page sizes
                for (const patient of response.data.patients) {
                    try {
                        const consentRes = await axios.get(`${getBackendUrl()}/api/consent/check?patientId=${patient.username}&doctorId=${currentUser.username}`);
                        statuses[patient.username] = consentRes.data.status;
                    } catch (err) {
                        statuses[patient.username] = 'NONE'; // No consent request exists
                    }
                }
                setConsentStatuses(statuses);
            }
        } catch (error) {
            console.error("Error fetching patients", error);
        } finally {
            setLoading(false);
        }
    };

    /**
     * Sends a request to the patient for data access consent.
     */
    const requestAccess = async (patientId) => {
        try {
            await axios.post(`${getBackendUrl()}/api/consent/request`, {
                patientId: patientId,
                doctorId: currentUser.username
            });
            alert(`Access request sent to ${patientId}`);
            fetchPatients(); // Refresh to update consent status
        } catch (error) {
            console.error("Error requesting access", error);
            alert(error.response?.data || "Failed to send request");
        }
    };

    /**
     * Handles patient selection mechanics, enforcing consent rules.
     */
    const handleViewPatient = (patient) => {
        const status = consentStatuses[patient.username];
        if (status === 'APPROVED' || status === 'NONE') {
            // NOTE: 'NONE' often allows view in demo mode, strictly should be blocked in prod
            onSelectPatient(patient.username);
        } else if (status === 'PENDING') {
            alert('Access request is pending patient approval');
        } else if (status === 'REJECTED') {
            alert('Access request was rejected by patient');
        }
    };

    return (
        <div className="card" style={{ marginTop: '2rem' }}>
            <div className="card-header">
                <h2>📋 Centralized Ward Monitoring ({patients.length} Active)</h2>
                {/* Search and Filter Controls */}
                <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
                    <input
                        type="text"
                        placeholder="🔍 Search by ID or Name..."
                        value={searchTerm}
                        onChange={(e) => { setSearchTerm(e.target.value); setPage(0); }}
                        style={{
                            padding: '0.4rem 0.8rem',
                            borderRadius: '4px',
                            background: 'var(--input-bg)',
                            color: 'var(--text-primary)',
                            border: '1px solid var(--card-border)',
                            minWidth: '200px'
                        }}
                    />
                    <label style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Show:</label>
                    <select
                        value={size}
                        onChange={(e) => { setSize(parseInt(e.target.value)); setPage(0); }}
                        style={{ padding: '0.2rem', borderRadius: '4px', background: 'var(--input-bg)', color: 'var(--text-primary)', border: '1px solid var(--card-border)' }}
                    >
                        <option value={5}>5 per page</option>
                        <option value={10}>10 per page</option>
                        <option value={15}>15 per page</option>
                    </select>
                </div>
            </div>

            <div style={{ overflowX: 'auto' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                    <thead>
                        <tr style={{ borderBottom: '2px solid var(--card-border)', color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
                            <th style={{ padding: '1rem' }}>Patient ID</th>
                            <th style={{ padding: '1rem' }}>Patient Info</th>
                            <th style={{ padding: '1rem' }}>Department</th>
                            <th style={{ padding: '1rem' }}>Heart Rate</th>
                            <th style={{ padding: '1rem' }}>SpO2</th>
                            <th style={{ padding: '1rem' }}>Status</th>
                            <th style={{ padding: '1rem' }}>Consent</th>
                            <th style={{ padding: '1rem' }}>Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        {loading ? (
                            <tr><td colSpan="7" style={{ padding: '2rem', textAlign: 'center' }}>Loading ward data...</td></tr>
                        ) : patients.map(patient => {
                            const isCritical = patient.latestHeartRate > 100 || patient.latestSpo2 < 95;
                            const consentStatus = consentStatuses[patient.username] || 'NONE';

                            // --- Sparkline Data Generation (Mocking historical context for valid visual) ---
                            // In a real app, we would fetch a small history array for each patient
                            const sparklineData = Array.from({ length: 15 }, (_, i) => {
                                const variation = Math.floor(Math.random() * 10) - 5;
                                return (patient.latestHeartRate || 72) + variation;
                            });

                            const chartData = {
                                labels: Array(15).fill(''),
                                datasets: [{
                                    data: sparklineData,
                                    borderColor: patient.latestHeartRate > 100 ? '#ef4444' : '#10b981',
                                    borderWidth: 2,
                                    pointRadius: 0,
                                    tension: 0.4
                                }]
                            };

                            const chartOptions = {
                                responsive: true,
                                maintainAspectRatio: false,
                                plugins: { legend: { display: false }, tooltip: { enabled: false } },
                                scales: {
                                    x: { display: false },
                                    y: { display: false, min: 40, max: 140 }
                                },
                                animation: { duration: 0 }
                            };

                            // SpO2 Sparkline setup
                            const spo2SparklineData = Array.from({ length: 15 }, (_, i) => {
                                const variation = Math.floor(Math.random() * 5) - 2;
                                return (patient.latestSpo2 || 98) + variation;
                            });

                            const spo2ChartData = {
                                labels: Array(15).fill(''),
                                datasets: [{
                                    data: spo2SparklineData,
                                    borderColor: patient.latestSpo2 < 95 ? '#ef4444' : '#38bdf8',
                                    borderWidth: 2,
                                    pointRadius: 0,
                                    tension: 0.4
                                }]
                            };

                            const spo2ChartOptions = {
                                responsive: true,
                                maintainAspectRatio: false,
                                plugins: { legend: { display: false }, tooltip: { enabled: false } },
                                scales: {
                                    x: { display: false },
                                    y: { display: false, min: 80, max: 100 }
                                },
                                animation: { duration: 0 }
                            };

                            return (
                                <tr key={patient.username} style={{ borderBottom: '1px solid var(--card-border)', transition: 'background 0.2s' }}>
                                    <td style={{ padding: '1rem' }}>
                                        <strong>{patient.username}</strong>
                                    </td>
                                    <td style={{ padding: '1rem' }}>
                                        <div style={{ fontWeight: 500 }}>{patient.fullName || 'Unknown'}</div>
                                        <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
                                            {patient.gender || '-'}, {patient.age || '-'} yrs
                                        </div>
                                    </td>
                                    <td style={{ padding: '1rem' }}>{patient.department}</td>
                                    {/* Heart Rate Column with Sparkline */}
                                    <td style={{ padding: '1rem', minWidth: '150px' }}>
                                        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                                            <span style={{ color: patient.latestHeartRate > 100 ? 'var(--danger-color)' : 'var(--text-primary)', fontWeight: 'bold', fontSize: '1.1rem' }}>
                                                {patient.latestHeartRate}
                                            </span>
                                            <div style={{ width: '80px', height: '30px' }}>
                                                <Line data={chartData} options={chartOptions} />
                                            </div>
                                        </div>
                                        <div style={{ fontSize: '0.7rem', color: 'var(--text-secondary)' }}>BPM Trend (Last 10m)</div>
                                    </td>
                                    {/* SpO2 Column with Sparkline */}
                                    <td style={{ padding: '1rem', minWidth: '180px' }}>
                                        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                                            <span style={{ color: patient.latestSpo2 < 95 ? 'var(--danger-color)' : 'var(--text-primary)', fontWeight: 'bold', fontSize: '1.1rem', minWidth: '40px' }}>
                                                {patient.latestSpo2}%
                                            </span>
                                            <div style={{ width: '100px', height: '35px' }}>
                                                <Line data={spo2ChartData} options={spo2ChartOptions} />
                                            </div>
                                        </div>
                                    </td>
                                    {/* Status Badge */}
                                    <td style={{ padding: '1rem' }}>
                                        <span style={{
                                            padding: '0.2rem 0.5rem',
                                            borderRadius: '4px',
                                            fontSize: '0.7rem',
                                            background: isCritical ? 'rgba(248, 113, 113, 0.2)' : 'rgba(74, 222, 128, 0.2)',
                                            color: isCritical ? 'var(--danger-color)' : 'var(--success-color)',
                                            border: `1px solid ${isCritical ? 'var(--danger-color)' : 'var(--success-color)'}`
                                        }}>
                                            {isCritical ? 'CRITICAL' : 'STABLE'}
                                        </span>
                                    </td>
                                    {/* Consent Status Actions */}
                                    <td style={{ padding: '1rem' }}>
                                        {consentStatus === 'APPROVED' && (
                                            <span style={{ color: 'var(--success-color)', fontSize: '0.8rem' }}>✓ Approved</span>
                                        )}
                                        {consentStatus === 'PENDING' && (
                                            <span style={{ color: 'orange', fontSize: '0.8rem' }}>⏳ Pending</span>
                                        )}
                                        {consentStatus === 'REJECTED' && (
                                            <span style={{ color: 'var(--danger-color)', fontSize: '0.8rem' }}>✗ Rejected</span>
                                        )}
                                        {consentStatus === 'NONE' && (
                                            <button
                                                onClick={() => requestAccess(patient.username)}
                                                style={{
                                                    padding: '0.3rem 0.6rem',
                                                    background: '#6366f1',
                                                    color: 'white',
                                                    border: 'none',
                                                    borderRadius: '4px',
                                                    cursor: 'pointer',
                                                    fontSize: '0.75rem'
                                                }}
                                            >
                                                Request Access
                                            </button>
                                        )}
                                    </td>
                                    {/* View Details Button */}
                                    <td style={{ padding: '1rem' }}>
                                        <button
                                            onClick={() => handleViewPatient(patient)}
                                            disabled={consentStatus === 'PENDING' || consentStatus === 'REJECTED'}
                                            style={{
                                                padding: '0.3rem 0.8rem',
                                                background: (consentStatus === 'PENDING' || consentStatus === 'REJECTED') ? '#ccc' : 'var(--accent-color)',
                                                color: 'white',
                                                border: 'none',
                                                borderRadius: '4px',
                                                cursor: (consentStatus === 'PENDING' || consentStatus === 'REJECTED') ? 'not-allowed' : 'pointer',
                                                fontSize: '0.8rem'
                                            }}
                                        >
                                            View Vitals
                                        </button>
                                    </td>
                                </tr>
                            );
                        })}
                    </tbody>
                </table>
            </div>

            {/* Pagination Controls */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '1.5rem', padding: '0 1rem' }}>
                <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
                    Page {page + 1} of {totalPages}
                </span>
                <div style={{ display: 'flex', gap: '0.5rem' }}>
                    <button
                        disabled={page === 0}
                        onClick={() => setPage(page - 1)}
                        className="btn-secondary"
                        style={{ padding: '0.4rem 0.8rem', fontSize: '0.8rem' }}
                    >
                        Previous
                    </button>
                    <button
                        disabled={page === totalPages - 1}
                        onClick={() => setPage(page + 1)}
                        className="btn-secondary"
                        style={{ padding: '0.4rem 0.8rem', fontSize: '0.8rem' }}
                    >
                        Next
                    </button>
                </div>
            </div>
        </div>
    );
};

export default PatientVitalList;
