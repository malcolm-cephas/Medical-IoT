import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { getBackendUrl } from '../config';

/**
 * ConsentManagement Component
 * 
 * Manages the dynamic access control policies for patient data.
 * Allows patients to approve/reject doctor access requests.
 * Allows doctors to see the status of their requests.
 * 
 * This component interacts with the backend's policy engine (ABAC/RBAC hybrid).
 * 
 * @param {string} patientId - The ID of the patient whose consents are being managed.
 * @param {Object} currentUser - The currently logged-in user (can be patient or doctor).
 * @param {string} theme - 'light' or 'dark' mode.
 */
const ConsentManagement = ({ patientId, currentUser, theme }) => {
    const [requests, setRequests] = useState([]);
    const [loading, setLoading] = useState(false);

    // Fetch requests whenever the patient context changes
    useEffect(() => {
        if (patientId) {
            fetchConsentRequests();
        }
    }, [patientId]);

    /**
     * Retrieves the list of consent requests for the current patient.
     */
    const fetchConsentRequests = async () => {
        setLoading(true);
        try {
            const response = await axios.get(`${getBackendUrl()}/api/consent/patient/${patientId}`);
            setRequests(response.data);
        } catch (error) {
            console.error("Error fetching consent requests", error);
        } finally {
            setLoading(false);
        }
    };

    /**
     * Handles the user's decision (Approve/Reject) on a consent request.
     * 
     * @param {string} consentId - ID of the consent record.
     * @param {string} status - New status ('APPROVED' or 'REJECTED').
     */
    const respondToRequest = async (consentId, status) => {
        try {
            await axios.post(`${getBackendUrl()}/api/consent/respond`, {
                consentId: consentId.toString(),
                status: status
            });
            alert(`Request ${status.toLowerCase()}`);
            fetchConsentRequests(); // Refresh list to reflect changes
        } catch (error) {
            console.error("Error responding to request", error);
            alert("Failed to respond to request");
        }
    };

    // Filter requests by status for grouped display
    const pendingRequests = requests.filter(r => r.status === 'PENDING');
    const approvedRequests = requests.filter(r => r.status === 'APPROVED');
    const rejectedRequests = requests.filter(r => r.status === 'REJECTED');

    // Helper flags to determine UI actions
    const isPatient = currentUser && currentUser.role === 'patient';
    const isOwner = currentUser && (currentUser.username === patientId || currentUser.id === patientId);

    return (
        <div className="card" style={{ marginTop: '2rem' }}>
            <div className="card-header">
                <h2>🔐 Access Consent Management</h2>
                <p style={{ fontSize: '0.9rem', color: 'var(--text-secondary)', marginTop: '0.5rem' }}>
                    {isPatient ? 'Manage who can access your medical data' : `Consent status for Patient: ${patientId}`}
                </p>
            </div>

            {loading ? (
                <p style={{ padding: '2rem', textAlign: 'center' }}>Loading requests...</p>
            ) : (
                <>
                    {/* Pending Requests Section */}
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
                                            <strong style={{ fontSize: '1rem' }}>Doctor: {request.doctorId}</strong>
                                            <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginTop: '0.25rem' }}>
                                                Requested: {new Date(request.requestedAt).toLocaleString()}
                                            </p>
                                        </div>
                                        {/* Actions available only to the patient/owner */}
                                        {isPatient && isOwner && (
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
                                        )}
                                        {(!isPatient || !isOwner) && (
                                            <span style={{ fontSize: '0.9rem', color: 'orange', fontWeight: 'bold' }}>PENDING</span>
                                        )}
                                    </div>
                                ))}
                            </div>
                        </div>
                    )}

                    {/* Approved Requests Section */}
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
                                            <strong>Doctor: {request.doctorId}</strong>
                                            <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginLeft: '1rem' }}>
                                                Approved: {new Date(request.approvedAt).toLocaleString()}
                                            </span>
                                        </div>
                                        {/* Revoke Option */}
                                        {isPatient && isOwner && (
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
                                        )}
                                    </div>
                                ))}
                            </div>
                        </div>
                    )}

                    {/* Rejected Requests Section */}
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
                                        <strong>Doctor: {request.doctorId}</strong>
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
