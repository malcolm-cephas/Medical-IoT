import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { getBackendUrl } from '../config';

const DoctorAvailability = ({ user }) => {
    const [appointments, setAppointments] = useState([]);
    const [slots, setSlots] = useState([]);
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState({ text: '', type: '' });
    const [newSlot, setNewSlot] = useState({
        fromTime: '',
        toTime: ''
    });

    useEffect(() => {
        fetchMyAppointments();
        fetchMySlots();
    }, []);

    const fetchMyAppointments = async () => {
        try {
            const response = await axios.get(`${getBackendUrl()}/api/doctor/appointments`, {
                headers: { 'X-User-Id': user.username }
            });
            setAppointments(response.data);
        } catch (error) {
            console.error('Error fetching appointments:', error);
        }
    };

    const fetchMySlots = async () => {
        try {
            const response = await axios.get(
                `${getBackendUrl()}/api/doctor/${user.username}/slots`
            );
            setSlots(response.data);
        } catch (error) {
            console.error('Error fetching slots:', error);
        }
    };

    const setAvailability = async (e) => {
        e.preventDefault();
        setLoading(true);

        try {
            const response = await axios.post(
                `${getBackendUrl()}/api/doctor/set-availability`,
                newSlot,
                { headers: { 'X-User-Id': user.username } }
            );
            setMessage({ text: response.data.message, type: 'success' });
            setNewSlot({ fromTime: '', toTime: '' });
            fetchMySlots();
        } catch (error) {
            console.error('Error setting availability:', error);
            setMessage({
                text: error.response?.data?.error || 'Failed to set availability',
                type: 'error'
            });
        } finally {
            setLoading(false);
        }
    };

    const completeAppointment = async (appointmentId) => {
        setLoading(true);
        try {
            const response = await axios.post(
                `${getBackendUrl()}/api/doctor/appointments/${appointmentId}/complete`,
                {},
                { headers: { 'X-User-Id': user.username } }
            );
            setMessage({ text: response.data.message, type: 'success' });
            fetchMyAppointments();
        } catch (error) {
            console.error('Error completing appointment:', error);
            setMessage({ text: 'Failed to complete appointment', type: 'error' });
        } finally {
            setLoading(false);
        }
    };

    const cancelSlot = async (slotId) => {
        if (!window.confirm('Are you sure you want to cancel this slot?')) return;

        setLoading(true);
        try {
            const response = await axios.post(
                `${getBackendUrl()}/api/doctor/slots/${slotId}/cancel`,
                {},
                { headers: { 'X-User-Id': user.username } }
            );
            setMessage({ text: response.data.message, type: 'success' });
            fetchMySlots();
        } catch (error) {
            console.error('Error canceling slot:', error);
            setMessage({ text: 'Failed to cancel slot', type: 'error' });
        } finally {
            setLoading(false);
        }
    };

    const formatDateTime = (dateString) => {
        if (!dateString) return 'N/A';
        const date = new Date(dateString);
        return date.toLocaleString('en-US', {
            month: 'short',
            day: 'numeric',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    };

    const getMinDateTime = () => {
        const now = new Date();
        now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
        return now.toISOString().slice(0, 16);
    };

    return (
        <div className="doctor-availability">
            <h2>🩺 Doctor Availability Management</h2>

            {message.text && (
                <div className={`message ${message.type}`}>
                    {message.text}
                    <button onClick={() => setMessage({ text: '', type: '' })}>×</button>
                </div>
            )}

            {/* Set Availability Form */}
            <div className="section">
                <h3>Set New Availability</h3>
                <form onSubmit={setAvailability} className="availability-form">
                    <div className="form-group">
                        <label>From:</label>
                        <input
                            type="datetime-local"
                            value={newSlot.fromTime}
                            min={getMinDateTime()}
                            onChange={(e) => setNewSlot({ ...newSlot, fromTime: e.target.value })}
                            required
                        />
                    </div>
                    <div className="form-group">
                        <label>To:</label>
                        <input
                            type="datetime-local"
                            value={newSlot.toTime}
                            min={newSlot.fromTime || getMinDateTime()}
                            onChange={(e) => setNewSlot({ ...newSlot, toTime: e.target.value })}
                            required
                        />
                    </div>
                    <button type="submit" className="btn-submit" disabled={loading}>
                        {loading ? 'Setting...' : 'Set Availability'}
                    </button>
                </form>
            </div>

            {/* My Slots */}
            <div className="section">
                <h3>My Availability Slots</h3>
                {slots.length === 0 ? (
                    <p className="empty-state">No availability slots set</p>
                ) : (
                    <div className="slots-list">
                        {slots.map(slot => (
                            <div key={slot.id} className={`slot-card status-${slot.status.toLowerCase()}`}>
                                <div className="slot-time">
                                    <div>📅 {formatDateTime(slot.fromTime)}</div>
                                    <div>to {formatDateTime(slot.toTime)}</div>
                                </div>
                                <div className="slot-footer">
                                    <span className={`status-badge ${slot.status.toLowerCase()}`}>
                                        {slot.status}
                                    </span>
                                    {slot.status === 'AVAILABLE' && (
                                        <button
                                            className="btn-cancel-slot"
                                            onClick={() => cancelSlot(slot.id)}
                                            disabled={loading}
                                        >
                                            Cancel
                                        </button>
                                    )}
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>

            {/* My Appointments */}
            <div className="section">
                <h3>My Appointments</h3>
                {appointments.length === 0 ? (
                    <p className="empty-state">No appointments scheduled</p>
                ) : (
                    <div className="appointments-list">
                        {appointments.map(apt => (
                            <div key={apt.id} className={`appointment-card status-${apt.status.toLowerCase()}`}>
                                <div className="apt-header">
                                    <span className="patient-name">Patient: {apt.patientId}</span>
                                    <span className={`status-badge ${apt.status.toLowerCase()}`}>
                                        {apt.status}
                                    </span>
                                </div>
                                <div className="apt-time">
                                    🕐 {formatDateTime(apt.appointmentTime)}
                                </div>
                                {apt.status === 'SCHEDULED' && (
                                    <button
                                        className="btn-complete"
                                        onClick={() => completeAppointment(apt.id)}
                                        disabled={loading}
                                    >
                                        Mark as Completed
                                    </button>
                                )}
                            </div>
                        ))}
                    </div>
                )}
            </div>

            <style>{`
                .doctor-availability {
                    padding: 1rem;
                }

                .section {
                    margin-bottom: 2rem;
                    background: var(--card-bg);
                    padding: 1.5rem;
                    border-radius: 12px;
                }

                .message {
                    padding: 1rem;
                    border-radius: 8px;
                    margin-bottom: 1rem;
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                }

                .message.success {
                    background: rgba(34, 197, 94, 0.1);
                    border: 1px solid #22c55e;
                    color: #22c55e;
                }

                .message.error {
                    background: rgba(239, 68, 68, 0.1);
                    border: 1px solid #ef4444;
                    color: #ef4444;
                }

                .message button {
                    background: none;
                    border: none;
                    font-size: 1.5rem;
                    cursor: pointer;
                    color: inherit;
                }

                .availability-form {
                    display: grid;
                    gap: 1rem;
                    max-width: 600px;
                }

                .form-group {
                    display: flex;
                    flex-direction: column;
                    gap: 0.5rem;
                }

                .form-group label {
                    font-weight: 600;
                    color: var(--text-primary);
                }

                .form-group input {
                    padding: 0.75rem;
                    border-radius: 8px;
                    border: 1px solid rgba(255, 255, 255, 0.2);
                    background: rgba(255, 255, 255, 0.05);
                    color: var(--text-primary);
                    font-size: 1rem;
                }

                .btn-submit {
                    padding: 0.75rem 1.5rem;
                    border-radius: 8px;
                    border: none;
                    background: var(--accent-color);
                    color: white;
                    font-weight: 600;
                    cursor: pointer;
                    transition: all 0.3s ease;
                }

                .btn-submit:hover:not(:disabled) {
                    transform: translateY(-2px);
                    box-shadow: 0 4px 12px rgba(var(--accent-rgb), 0.4);
                }

                .btn-submit:disabled {
                    opacity: 0.5;
                    cursor: not-allowed;
                }

                .slots-list, .appointments-list {
                    display: grid;
                    gap: 1rem;
                }

                .slot-card, .appointment-card {
                    padding: 1rem;
                    border-radius: 8px;
                    background: rgba(255, 255, 255, 0.05);
                    border: 1px solid rgba(255, 255, 255, 0.1);
                }

                .slot-time {
                    font-size: 0.95rem;
                    color: var(--text-secondary);
                    margin-bottom: 0.75rem;
                }

                .slot-footer {
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                }

                .apt-header {
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    margin-bottom: 0.5rem;
                }

                .patient-name {
                    font-weight: 600;
                    font-size: 1.1rem;
                }

                .status-badge {
                    padding: 0.25rem 0.75rem;
                    border-radius: 20px;
                    font-size: 0.85rem;
                    font-weight: 600;
                }

                .status-badge.available {
                    background: rgba(34, 197, 94, 0.2);
                    color: #22c55e;
                }

                .status-badge.booked, .status-badge.scheduled {
                    background: rgba(59, 130, 246, 0.2);
                    color: #3b82f6;
                }

                .status-badge.completed {
                    background: rgba(34, 197, 94, 0.2);
                    color: #22c55e;
                }

                .status-badge.cancelled {
                    background: rgba(239, 68, 68, 0.2);
                    color: #ef4444;
                }

                .apt-time {
                    color: var(--text-secondary);
                    margin-bottom: 0.75rem;
                }

                .btn-complete, .btn-cancel-slot {
                    padding: 0.5rem 1rem;
                    border-radius: 6px;
                    border: none;
                    font-weight: 600;
                    cursor: pointer;
                    transition: all 0.3s ease;
                }

                .btn-complete {
                    background: rgba(34, 197, 94, 0.2);
                    color: #22c55e;
                }

                .btn-complete:hover:not(:disabled) {
                    background: rgba(34, 197, 94, 0.3);
                }

                .btn-cancel-slot {
                    background: rgba(239, 68, 68, 0.2);
                    color: #ef4444;
                }

                .btn-cancel-slot:hover:not(:disabled) {
                    background: rgba(239, 68, 68, 0.3);
                }

                .empty-state {
                    text-align: center;
                    color: var(--text-secondary);
                    padding: 2rem;
                }
            `}</style>
        </div>
    );
};

export default DoctorAvailability;
