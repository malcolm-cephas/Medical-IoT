import React, { useState, useEffect } from 'react';
import axios from 'axios';

const ConsentManagement = ({ patientId, theme }) => {
    const [requests, setRequests] = useState([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        fetchConsentRequests();
    }, [patientId]);

    const fetchConsentRequests = async () => {
        setLoading(true);
        try {
            const response = await axios.get(`http://localhost:8080/api/consent/patient/${patientId}`);
            setRequests(response.data);
        } catch (error) {
            console.error("Error fetching consent requests", error);
        } finally {
            setLoading(false);
        }
    };

    const respondToRequest = async (consentId, status) => {
        try {
            await axios.post('http://localhost:8080/api/consent/respond', {
                consentId: consentId.toString(),
                status: status
            });
            alert(`Request ${status.toLowerCase()}`);
            fetchConsentRequests(); // Refresh list
        } catch (error) {
            console.error("Error responding to request", error);
            alert("Failed to respond to request");
        }
    };

    const pendingRequests = requests.filter(r => r.status === 'PENDING');
    const approvedRequests = requests.filter(r => r.status === 'APPROVED');
    const rejectedRequests = requests.filter(r => r.status === 'REJECTED');

    return (
        <div className="card" style={{ marginTop: '2rem' }}>
            <div className="card-header">
                <h2>🔐 Access Consent Management</h2>
                <p style={{ fontSize: '0.9rem', color: 'var(--text-secondary)', marginTop: '0.5rem' }}>
                    Manage who can access your medical data
                </p>
            </div>

            {loading ? (
                <p style={{ padding: '2rem', textAlign: 'center' }}>Loading requests...</p>
            ) : (
                <>
                    {/* Pending Requests */}
                    {pendingRequests.length > 0 && (
                        <div style={{ marginBottom: '2rem' }}>
                            <h3 style={{ fontSize: '1.1rem', marginBottom: '1rem', color: 'orange' }}>
                                ⏳ Pending Requests ({pendingRequests.length})
                            </h3>
                            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                                {pendingRequests.map(request => (
                                    <div
                                        key={request.id}
                                        style={{
                                            padding: '1rem',
                                            border: '2px solid orange',
                                            borderRadius: '8px',
                                            background: theme === 'dark' ? 'rgba(255, 165, 0, 0.1)' : 'rgba(255, 165, 0, 0.05)',
                                            display: 'flex',
                                            justifyContent: 'space-between',
                                            alignItems: 'center'
                                        }}
                                    >
                                        <div>
                                            <strong style={{ fontSize: '1rem' }}>{request.doctorId}</strong>
                                            <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginTop: '0.25rem' }}>
                                                Requested: {new Date(request.requestedAt).toLocaleString()}
                                            </p>
                                        </div>
                                        <div style={{ display: 'flex', gap: '0.5rem' }}>
                                            <button
                                                onClick={() => respondToRequest(request.id, 'APPROVED')}
                                                style={{
                                                    padding: '0.5rem 1rem',
                                                    background: 'var(--success-color)',
                                                    color: 'white',
                                                    border: 'none',
                                                    borderRadius: '4px',
                                                    cursor: 'pointer',
                                                    fontWeight: 'bold'
                                                }}
                                            >
                                                ✓ Approve
                                            </button>
                                            <button
                                                onClick={() => respondToRequest(request.id, 'REJECTED')}
                                                style={{
                                                    padding: '0.5rem 1rem',
                                                    background: 'var(--danger-color)',
                                                    color: 'white',
                                                    border: 'none',
                                                    borderRadius: '4px',
                                                    cursor: 'pointer',
                                                    fontWeight: 'bold'
                                                }}
                                            >
                                                ✗ Reject
                                            </button>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </div>
                    )}

                    {/* Approved Requests */}
                    {approvedRequests.length > 0 && (
                        <div style={{ marginBottom: '2rem' }}>
                            <h3 style={{ fontSize: '1.1rem', marginBottom: '1rem', color: 'var(--success-color)' }}>
                                ✓ Approved Access ({approvedRequests.length})
                            </h3>
                            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                                {approvedRequests.map(request => (
                                    <div
                                        key={request.id}
                                        style={{
                                            padding: '0.75rem',
                                            border: '1px solid var(--success-color)',
                                            borderRadius: '4px',
                                            background: theme === 'dark' ? 'rgba(74, 222, 128, 0.1)' : 'rgba(74, 222, 128, 0.05)',
                                            display: 'flex',
                                            justifyContent: 'space-between',
                                            alignItems: 'center'
                                        }}
                                    >
                                        <div>
                                            <strong>{request.doctorId}</strong>
                                            <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginLeft: '1rem' }}>
                                                Approved: {new Date(request.approvedAt).toLocaleString()}
                                            </span>
                                        </div>
                                        <button
                                            onClick={() => respondToRequest(request.id, 'REJECTED')}
                                            style={{
                                                padding: '0.3rem 0.6rem',
                                                background: 'transparent',
                                                color: 'var(--danger-color)',
                                                border: '1px solid var(--danger-color)',
                                                borderRadius: '4px',
                                                cursor: 'pointer',
                                                fontSize: '0.8rem'
                                            }}
                                        >
                                            Revoke
                                        </button>
                                    </div>
                                ))}
                            </div>
                        </div>
                    )}

                    {/* Rejected Requests */}
                    {rejectedRequests.length > 0 && (
                        <div>
                            <h3 style={{ fontSize: '1.1rem', marginBottom: '1rem', color: 'var(--text-secondary)' }}>
                                ✗ Rejected Requests ({rejectedRequests.length})
                            </h3>
                            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                                {rejectedRequests.map(request => (
                                    <div
                                        key={request.id}
                                        style={{
                                            padding: '0.75rem',
                                            border: '1px solid var(--card-border)',
                                            borderRadius: '4px',
                                            background: theme === 'dark' ? 'rgba(0,0,0,0.2)' : 'rgba(0,0,0,0.05)',
                                            opacity: 0.7
                                        }}
                                    >
                                        <strong>{request.doctorId}</strong>
                                        <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginLeft: '1rem' }}>
                                            Rejected
                                        </span>
                                    </div>
                                ))}
                            </div>
                        </div>
                    )}

                    {requests.length === 0 && (
                        <p style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-secondary)' }}>
                            No access requests yet
                        </p>
                    )}
                </>
            )}
        </div>
    );
};

export default ConsentManagement;
