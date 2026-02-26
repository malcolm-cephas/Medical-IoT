import React from 'react';

const PatientSidebar = ({ patientId, isOpen, onClose, theme }) => {
    if (!isOpen) return null;

    // Mock Data for Demographics (In real app, fetch from /api/patients/{id})
    const patientDetails = {
        fullName: `Patient ${patientId}`,
        age: 45,
        gender: 'Male',
        bloodType: 'O+',
        allergies: ['Penicillin', 'Peanuts'],
        conditions: ['Hypertension', 'Type 2 Diabetes'],
        medications: [
            { name: 'Metformin', dosage: '500mg', freq: 'BID' },
            { name: 'Lisinopril', dosage: '10mg', freq: 'OD' }
        ],
        emergencyContact: {
            name: 'Sarah Connor',
            relation: 'Wife',
            phone: '+1-555-0199'
        }
    };

    return (
        <div style={{
            position: 'fixed',
            top: '0',
            right: '0',
            width: '320px',
            height: '100vh',
            background: 'var(--card-bg)',
            borderLeft: '1px solid var(--card-border)',
            boxShadow: '-4px 0 15px rgba(0,0,0,0.1)',
            padding: '1.5rem',
            overflowY: 'auto',
            zIndex: 1000,
            transition: 'transform 0.3s ease-in-out',
            transform: isOpen ? 'translateX(0)' : 'translateX(100%)'
        }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
                <h2 style={{ fontSize: '1.2rem', margin: 0 }}>Patient Details</h2>
                <button onClick={onClose} style={{ background: 'none', border: 'none', fontSize: '1.2rem', cursor: 'pointer', color: 'var(--text-secondary)' }}>✕</button>
            </div>

            <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
                <div style={{ width: '80px', height: '80px', background: 'var(--accent-color)', borderRadius: '50%', margin: '0 auto 1rem', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '2rem', color: 'white' }}>
                    👤
                </div>
                <h3 style={{ margin: '0.5rem 0' }}>{patientDetails.fullName}</h3>
                <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>ID: {patientId}</p>
            </div>

            <div className="detail-section" style={{ marginBottom: '1.5rem' }}>
                <h4 style={{ fontSize: '0.9rem', color: 'var(--text-secondary)', textTransform: 'uppercase', marginBottom: '0.5rem' }}>Demographics</h4>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.5rem', fontSize: '0.9rem' }}>
                    <div><strong>Age:</strong> {patientDetails.age}</div>
                    <div><strong>Gender:</strong> {patientDetails.gender}</div>
                    <div><strong>Blood:</strong> {patientDetails.bloodType}</div>
                </div>
            </div>

            <div className="detail-section" style={{ marginBottom: '1.5rem' }}>
                <h4 style={{ fontSize: '0.9rem', color: 'var(--text-secondary)', textTransform: 'uppercase', marginBottom: '0.5rem' }}>⚠️ Allergies</h4>
                <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
                    {patientDetails.allergies.map(allergy => (
                        <span key={allergy} style={{ background: 'rgba(239, 68, 68, 0.1)', color: '#ef4444', padding: '0.25rem 0.5rem', borderRadius: '4px', fontSize: '0.8rem' }}>
                            {allergy}
                        </span>
                    ))}
                </div>
            </div>

            <div className="detail-section" style={{ marginBottom: '1.5rem' }}>
                <h4 style={{ fontSize: '0.9rem', color: 'var(--text-secondary)', textTransform: 'uppercase', marginBottom: '0.5rem' }}>💊 Current Medications</h4>
                <ul style={{ listStyle: 'none', padding: 0 }}>
                    {patientDetails.medications.map((med, i) => (
                        <li key={i} style={{ padding: '0.5rem', background: 'var(--input-bg)', marginBottom: '0.5rem', borderRadius: '6px', fontSize: '0.9rem' }}>
                            <strong>{med.name}</strong> <span style={{ color: 'var(--text-secondary)' }}>- {med.dosage} ({med.freq})</span>
                        </li>
                    ))}
                </ul>
            </div>

            <div className="detail-section">
                <h4 style={{ fontSize: '0.9rem', color: 'var(--text-secondary)', textTransform: 'uppercase', marginBottom: '0.5rem' }}>📞 Emergency Contact</h4>
                <div style={{ fontSize: '0.9rem', background: 'var(--input-bg)', padding: '0.75rem', borderRadius: '6px' }}>
                    <strong>{patientDetails.emergencyContact.name}</strong> ({patientDetails.emergencyContact.relation})<br />
                    <a href={`tel:${patientDetails.emergencyContact.phone}`} style={{ color: 'var(--accent-color)', textDecoration: 'none', marginTop: '0.2rem', display: 'inline-block' }}>
                        {patientDetails.emergencyContact.phone}
                    </a>
                </div>
            </div>

        </div>
    );
};

export default PatientSidebar;
