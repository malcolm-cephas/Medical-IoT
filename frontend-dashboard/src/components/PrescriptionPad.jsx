import React, { useState } from 'react';
import axios from 'axios';
import { getBackendUrl, getAnalyticsUrl } from '../config';
import FaceVerification from './FaceVerification';

/**
 * PrescriptionPad Component
 *
 * This component allows a doctor to issue a prescription to a selected patient.
 * It provides a form to input diagnosis, medication (Rx), and additional notes,
 * and submits this data to the backend API.
 *
 * @param {Object} props - The component props
 * @param {number|string} props.doctorId - The ID of the doctor issuing the prescription
 * @param {number|string} props.selectedPatientId - The ID of the patient receiving the prescription
 * @param {Function} props.onClose - Validation callback to close the prescription pad or modal
 */
const PrescriptionPad = ({ doctorId, selectedPatientId, onClose }) => {
    // State variables to hold form input values
    const [diagnosis, setDiagnosis] = useState('');
    const [medicine, setMedicine] = useState('');
    const [notes, setNotes] = useState('');

    // State to manage loading status during API calls
    const [loading, setLoading] = useState(false);
    // State to manage success feedback to the user
    const [success, setSuccess] = useState(false);

    // Biometric 2FA State
    const [showFaceVerify, setShowFaceVerify] = useState(false);
    const [biometricData, setBiometricData] = useState(null);

    /**
     * Handles the form submission to create a new prescription.
     *
     * @param {Event} e - The submit event
     */
    const handleSubmit = async (e) => {
        if (e) e.preventDefault();

        if (!selectedPatientId) {
            alert("Please select a patient first.");
            return;
        }

        setLoading(true);
        try {
            // Make a POST request to the backend to save the prescription
            await axios.post(`${getBackendUrl()}/api/prescriptions/add`, {
                doctorId: doctorId,
                patientId: selectedPatientId,
                diagnosis,
                medicine,
                notes
            }, {
                headers: {
                    'X-User-Id': doctorId,
                    'X-User-Role': 'DOCTOR' // Explicitly set role for 2FA trigger
                }
            });

            setSuccess(true);
            setShowFaceVerify(false);

            setTimeout(() => {
                setSuccess(false);
                setDiagnosis('');
                setMedicine('');
                setNotes('');
                if (onClose) onClose();
            }, 2000);
        } catch (error) {
            console.error("Error creating prescription:", error.response || error);
            
            // Handle 2FA Requirement from Spring Boot
            if (error.response && error.response.status === 403 && error.response.data['2fa_required']) {
                setShowFaceVerify(true);
            } else {
                alert(error.response?.data?.error || error.response?.data?.message || "Failed to save prescription.");
            }
        } finally {
            setLoading(false);
        }
    };

    /**
     * Called when FaceVerification component captures an image.
     * Sends the image to Spring Boot to START a 5-minute authorized session.
     */
    const handleFaceSuccess = async (base64Image) => {
        try {
            setLoading(true);
            
            // Convert Base64 to Blob for Multipart upload
            const response = await fetch(base64Image);
            const blob = await response.blob();
            
            const formData = new FormData();
            formData.append('username', doctorId);
            formData.append('file', blob, 'face_capture.jpg');

            // Send to Spring Boot FaceController (Not Python directly)
            const res = await axios.post(`${getBackendUrl()}/api/face/verify`, formData, {
                headers: { 'Content-Type': 'multipart/form-data' }
            });

            if (res.data.verified) {
                setShowFaceVerify(false);
                // Retry the original submission - now the backend will see a valid session!
                await handleSubmit(null);
            }
        } catch (err) {
            console.error("Biometric Verification Error:", err.response || err);
            // Removed alert as requested. Silently close modal on failure.
            setShowFaceVerify(false);
        } finally {
            setLoading(false);
        }
    };

    // Formatter for display names (e.g. doctor_micheal -> Micheal)
    const formatName = (name) => {
        if (!name) return "";
        return name.toString().split('_').map(word => 
            word.charAt(0).toUpperCase() + word.slice(1)
        ).join(' ').replace('Doctor ', ''); // Remove 'Doctor' if it's already in the username
    };

    return (
        <div className="prescription-pad card fade-in" style={{ 
            maxWidth: '500px', 
            maxHeight: '85vh',
            overflowY: 'auto',
            margin: '0 auto', 
            padding: '1.5rem', 
            position: 'relative',
            background: 'var(--card-bg)',
            border: '1px solid var(--card-border)',
            boxShadow: '0 10px 30px rgba(0,0,0,0.2)'
        }}>
            {/* Biometric Modal Overlay */}
            {showFaceVerify && (
                <FaceVerification 
                    username={doctorId} 
                    mode="verify" 
                    onSuccess={handleFaceSuccess} 
                    onCancel={() => setShowFaceVerify(false)} 
                />
            )}

            {/* Professional Medical Header */}
            <div className="prescription-header" style={{ 
                borderBottom: '2px solid var(--accent-color)', 
                paddingBottom: '0.75rem', 
                marginBottom: '1rem',
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'flex-start'
            }}>
                <div>
                    <h2 style={{ margin: 0, fontSize: '1.2rem', color: 'var(--accent-color)', letterSpacing: '1px' }}>
                        🏥 MEDISECURE INSTITUTE
                    </h2>
                    <p style={{ margin: '0.1rem 0', fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
                        Electronic Prescription Service
                    </p>
                </div>
                <div style={{ textAlign: 'right' }}>
                    <div style={{ fontWeight: 'bold', fontSize: '0.85rem' }}>DR. {formatName(doctorId).toUpperCase()}</div>
                    <div style={{ fontSize: '0.7rem', color: 'var(--text-secondary)' }}>Lic: MED-{doctorId.toString().substring(0,4).toUpperCase()}</div>
                </div>
            </div>

            {/* Patient Context Bar */}
            <div style={{ 
                background: 'rgba(56, 189, 248, 0.05)', 
                padding: '0.5rem 0.75rem', 
                borderRadius: '8px', 
                marginBottom: '1rem',
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                border: '1px dashed var(--accent-color)'
            }}>
                <span style={{ fontSize: '0.85rem', fontWeight: '500' }}>Patient: <span style={{ color: 'var(--accent-color)' }}>{selectedPatientId}</span></span>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>{new Date().toLocaleDateString()}</span>
            </div>

            {success ? (
                <div style={{ 
                    padding: '2rem 1rem', 
                    textAlign: 'center', 
                    background: 'rgba(16, 185, 129, 0.1)', 
                    borderRadius: '12px',
                    border: '1px solid var(--success-color)'
                }}>
                    <div style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>✅</div>
                    <h3 style={{ color: 'var(--success-color)', margin: 0 }}>Prescription Issued</h3>
                    <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', marginTop: '0.25rem' }}>Encrypted & stored on IPFS.</p>
                </div>
            ) : (
                <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                    
                    <div className="input-group">
                        <label style={{ display: 'block', fontSize: '0.75rem', fontWeight: 'bold', color: 'var(--text-secondary)', marginBottom: '0.2rem', textTransform: 'uppercase' }}>
                            Clinical Diagnosis
                        </label>
                        <input
                            type="text"
                            required
                            style={{ 
                                width: '100%', 
                                padding: '0.6rem', 
                                background: 'var(--input-bg)', 
                                border: '1px solid var(--input-border)', 
                                borderRadius: '6px',
                                color: 'var(--text-primary)',
                                fontSize: '0.9rem'
                            }}
                            placeholder="Enter patient diagnosis..."
                            value={diagnosis}
                            onChange={(e) => setDiagnosis(e.target.value)}
                        />
                    </div>

                    <div className="input-group">
                        <label style={{ display: 'block', fontSize: '0.75rem', fontWeight: 'bold', color: 'var(--text-secondary)', marginBottom: '0.2rem', textTransform: 'uppercase' }}>
                            Rx (Medication & Dosage)
                        </label>
                        <textarea
                            required
                            rows="3"
                            style={{ 
                                width: '100%', 
                                padding: '0.6rem', 
                                background: 'var(--input-bg)', 
                                border: '1px solid var(--input-border)', 
                                borderRadius: '6px',
                                color: 'var(--text-primary)',
                                fontSize: '0.9rem',
                                fontFamily: 'monospace',
                                lineHeight: '1.4'
                            }}
                            placeholder="e.g. Amoxicillin 500mg &#10;- 1 tablet every 8 hours"
                            value={medicine}
                            onChange={(e) => setMedicine(e.target.value)}
                        />
                    </div>

                    <div className="input-group">
                        <label style={{ display: 'block', fontSize: '0.75rem', fontWeight: 'bold', color: 'var(--text-secondary)', marginBottom: '0.2rem', textTransform: 'uppercase' }}>
                            Additional Advice
                        </label>
                        <textarea
                            rows="2"
                            style={{ 
                                width: '100%', 
                                padding: '0.6rem', 
                                background: 'var(--input-bg)', 
                                border: '1px solid var(--input-border)', 
                                borderRadius: '6px',
                                color: 'var(--text-primary)',
                                fontSize: '0.85rem'
                            }}
                            placeholder="e.g. Drink plenty of fluids."
                            value={notes}
                            onChange={(e) => setNotes(e.target.value)}
                        />
                    </div>

                    {/* Digital Signature Simulation */}
                    <div style={{ 
                        marginTop: '0.5rem', 
                        padding: '0.75rem', 
                        background: 'rgba(0,0,0,0.05)', 
                        borderRadius: '6px',
                        borderLeft: '4px solid var(--accent-color)'
                    }}>
                        <div style={{ fontSize: '0.7rem', color: 'var(--text-secondary)', fontStyle: 'italic' }}>
                            Digitally signed by:
                        </div>
                        <div style={{ fontFamily: '"Great Vibes", cursive', fontSize: '1.1rem', color: 'var(--accent-color)', marginTop: '0.1rem' }}>
                            Dr. {formatName(doctorId)}
                        </div>
                    </div>

                    <button
                        type="submit"
                        disabled={loading || !selectedPatientId}
                        style={{ 
                            width: '100%', 
                            padding: '0.8rem', 
                            background: loading ? '#ccc' : 'var(--accent-color)', 
                            color: 'white', 
                            border: 'none', 
                            borderRadius: '8px', 
                            fontWeight: 'bold',
                            fontSize: '0.9rem',
                            cursor: loading ? 'not-allowed' : 'pointer',
                            boxShadow: '0 4px 10px rgba(56, 189, 248, 0.2)',
                            transition: 'all 0.3s ease'
                        }}
                    >
                        {loading ? '🔐 Authenticating...' : '🚀 Finalize & Issue'}
                    </button>
                </form>
            )}

            {/* Close Button */}
            <button 
                onClick={onClose}
                style={{
                    position: 'absolute',
                    top: '1rem',
                    right: '1rem',
                    background: 'none',
                    border: 'none',
                    fontSize: '1.5rem',
                    color: 'var(--text-secondary)',
                    cursor: 'pointer'
                }}
            >
                ✕
            </button>
        </div>
    );
};

export default PrescriptionPad;
