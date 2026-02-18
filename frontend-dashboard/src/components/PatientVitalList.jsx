import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { getBackendUrl } from '../config';
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

const PatientVitalList = ({ onSelectPatient, theme, currentUser }) => {
    const [patients, setPatients] = useState([]);
    const [page, setPage] = useState(0);
    const [size, setSize] = useState(5);
    const [totalPages, setTotalPages] = useState(0);
    const [loading, setLoading] = useState(false);
    const [searchTerm, setSearchTerm] = useState('');
    const [consentStatuses, setConsentStatuses] = useState({});

    // WebSocket for real-time list updates
    useEffect(() => {
        const socket = new SockJS(`${getBackendUrl()}/ws-vitals`);
        const stompClient = Stomp.over(socket);
        stompClient.debug = null;

        stompClient.connect({}, () => {
            stompClient.subscribe('/topic/ward', (message) => {
                const newData = JSON.parse(message.body);
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

    useEffect(() => {
        fetchPatients();
    }, [page, size, searchTerm]);

    const fetchPatients = async () => {
        setLoading(true);
        try {
            const response = await axios.get(`${getBackendUrl()}/api/patients?page=${page}&size=${size}&search=${searchTerm}`);
            setPatients(response.data.patients);
            setTotalPages(response.data.totalPages);

            // Fetch consent status for each patient
            if (currentUser) {
                const statuses = {};
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

    const handleViewPatient = (patient) => {
        const status = consentStatuses[patient.username];
        if (status === 'APPROVED' || status === 'NONE') {
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
                                    <td style={{ padding: '1rem' }}>
                                        <span style={{ color: patient.latestHeartRate > 100 ? 'var(--danger-color)' : 'var(--text-primary)', fontWeight: 'bold' }}>
                                            {patient.latestHeartRate} BPM
                                        </span>
                                    </td>
                                    <td style={{ padding: '1rem' }}>
                                        <span style={{ color: patient.latestSpo2 < 95 ? 'var(--danger-color)' : 'var(--text-primary)', fontWeight: 'bold' }}>
                                            {patient.latestSpo2}%
                                        </span>
                                    </td>
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
