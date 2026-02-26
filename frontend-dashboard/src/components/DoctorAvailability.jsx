import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { getBackendUrl } from '../config';

/**
 * DoctorAvailability Component
 * 
 * Allows doctors to manage their working hours (availability slots) and view/manage appointments.
 * Doctors can:
 * - Set recurring weekly availability (e.g., Every Monday 9-5).
 * - View their current "Office Hours" slots.
 * - Remove specific availability slots.
 * - View upcoming appointments booked by patients.
 * - Mark appointments as completed.
 * 
 * @param {Object} user - The currently logged-in doctor.
 */
const DoctorAvailability = ({ user }) => {
    const [appointments, setAppointments] = useState([]);
    const [slots, setSlots] = useState([]);
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState({ text: '', type: '' });

    // State for new availability creation form
    const [newSlot, setNewSlot] = useState({
        daysOfWeek: [], // Array of selected days (e.g., ['MONDAY', 'WEDNESDAY'])
        startTime: '09:00',
        endTime: '17:00'
    });
    const [showDayDropdown, setShowDayDropdown] = useState(false);

    const days = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];

    useEffect(() => {
        fetchMyAppointments();
        fetchMySlots();
    }, []);

    /**
     * Toggles a day in the multi-select dropdown.
     */
    const toggleDay = (day) => {
        const currentDays = [...newSlot.daysOfWeek];
        if (currentDays.includes(day)) {
            setNewSlot({ ...newSlot, daysOfWeek: currentDays.filter(d => d !== day) });
        } else {
            setNewSlot({ ...newSlot, daysOfWeek: [...currentDays, day] });
        }
    };

    /**
     * Fetches appointments assigned to this doctor.
     */
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

    /**
     * Fetches current availability configuration.
     */
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

    /**
     * Submits the new availability schedule to the backend.
     * Creates slots for all selected days.
     */
    const setAvailability = async (e) => {
        e.preventDefault();
        if (newSlot.daysOfWeek.length === 0) {
            setMessage({ text: 'Please select at least one day', type: 'error' });
            return;
        }
        setLoading(true);

        try {
            const response = await axios.post(
                `${getBackendUrl()}/api/doctor/set-availability`,
                newSlot,
                { headers: { 'X-User-Id': user.username } }
            );
            setMessage({ text: response.data.message, type: 'success' });
            alert(response.data.message); // Clear confirmation for user
            setNewSlot({ ...newSlot, daysOfWeek: [] }); // Reset days selection
            fetchMySlots(); // Refresh list
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

    /**
     * Marks an appointment as completed.
     * This might trigger post-visit workflows in a real system (billing, prescriptions).
     */
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

    /**
     * Removes an availability slot.
     * Prevents future bookings for that time slot.
     */
    const cancelSlot = async (slotId) => {
        if (!window.confirm('Are you sure you want to remove these office hours?')) return;

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
            console.error('Error removing hours:', error);
            setMessage({ text: 'Failed to remove office hours', type: 'error' });
        } finally {
            setLoading(false);
        }
    };

    const formatTime = (timeString) => {
        if (!timeString) return 'N/A';
        return timeString.substring(0, 5);
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

    return (
        <div className="doctor-availability">
            <h2>🩺 Office Hours Management</h2>

            {message.text && (
                <div className={`message ${message.type}`}>
                    {message.text}
                    <button onClick={() => setMessage({ text: '', type: '' })}>×</button>
                </div>
            )}

            {/* Section: Set Recurring Schedule */}
            <div className="section">
                <h3>Set Recurring Weekly Schedule</h3>
                <form onSubmit={setAvailability} className="availability-form">
                    <div className="form-group" style={{ display: 'flex', flexDirection: 'row', gap: '1rem', alignItems: 'flex-end', flexWrap: 'wrap' }}>
                        <div style={{ flex: 1, minWidth: '200px', position: 'relative' }}>
                            <label>Days of Week:</label>
                            <div
                                className="multi-select-trigger"
                                onClick={() => setShowDayDropdown(!showDayDropdown)}
                                style={{
                                    width: '100%',
                                    padding: '0.75rem',
                                    borderRadius: '8px',
                                    border: '1px solid rgba(255,255,255,0.2)',
                                    background: 'rgba(255,255,255,0.05)',
                                    color: 'white',
                                    cursor: 'pointer',
                                    display: 'flex',
                                    justifyContent: 'space-between',
                                    alignItems: 'center'
                                }}
                            >
                                <span>
                                    {newSlot.daysOfWeek.length === 0
                                        ? 'Select Days...'
                                        : `${newSlot.daysOfWeek.length} days selected`}
                                </span>
                                <span>{showDayDropdown ? '▲' : '▼'}</span>
                            </div>

                            {showDayDropdown && (
                                <div className="multi-select-dropdown" style={{
                                    position: 'absolute',
                                    top: '100%',
                                    left: 0,
                                    right: 0,
                                    background: '#1e293b',
                                    border: '1px solid rgba(255,255,255,0.2)',
                                    borderRadius: '8px',
                                    zIndex: 100,
                                    marginTop: '4px',
                                    maxHeight: '200px',
                                    overflowY: 'auto',
                                    padding: '0.5rem',
                                    boxShadow: '0 8px 16px rgba(0,0,0,0.4)'
                                }}>
                                    {days.map(day => (
                                        <label key={day} style={{
                                            display: 'flex',
                                            alignItems: 'center',
                                            padding: '0.5rem',
                                            cursor: 'pointer',
                                            gap: '0.5rem',
                                            borderRadius: '4px',
                                            transition: 'background 0.2s'
                                        }} className="day-option">
                                            <input
                                                type="checkbox"
                                                checked={newSlot.daysOfWeek.includes(day)}
                                                onChange={() => toggleDay(day)}
                                                style={{ cursor: 'pointer' }}
                                            />
                                            <span style={{ fontSize: '0.9rem', color: 'white' }}>{day}</span>
                                        </label>
                                    ))}
                                </div>
                            )}
                        </div>
                        <div style={{ flex: 1, minWidth: '150px' }}>
                            <label>Start Time:</label>
                            <input
                                type="time"
                                value={newSlot.startTime}
                                onChange={(e) => setNewSlot({ ...newSlot, startTime: e.target.value })}
                                required
                                style={{ width: '100%', padding: '0.75rem', borderRadius: '8px', border: '1px solid rgba(255,255,255,0.2)', background: 'rgba(255,255,255,0.05)', color: 'white' }}
                            />
                        </div>
                        <div style={{ flex: 1, minWidth: '150px' }}>
                            <label>End Time:</label>
                            <input
                                type="time"
                                value={newSlot.endTime}
                                onChange={(e) => setNewSlot({ ...newSlot, endTime: e.target.value })}
                                required
                                style={{ width: '100%', padding: '0.75rem', borderRadius: '8px', border: '1px solid rgba(255,255,255,0.2)', background: 'rgba(255,255,255,0.05)', color: 'white' }}
                            />
                        </div>
                        <button type="submit" className="btn-submit" disabled={loading} style={{ height: '45px' }}>
                            {loading ? 'Updating...' : 'Set Office Hours'}
                        </button>
                    </div>
                </form>
            </div>

            {/* Section: List of Existing Slots */}
            <div className="section">
                <h3>Current Office Hours</h3>
                {slots.length === 0 ? (
                    <p className="empty-state">No office hours configured</p>
                ) : (
                    <div className="slots-list" style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))', gap: '1rem' }}>
                        {slots.map(slot => (
                            <div key={slot.id} className="slot-card" style={{ padding: '1rem', borderRadius: '12px', background: 'rgba(255,255,255,0.05)', border: '1px solid rgba(255,255,255,0.1)' }}>
                                <div style={{ fontSize: '1.2rem', fontWeight: 'bold', marginBottom: '0.5rem', color: 'var(--accent-color)' }}>
                                    {slot.dayOfWeek}
                                </div>
                                <div className="slot-time" style={{ marginBottom: '1rem' }}>
                                    🕐 {formatTime(slot.startTime)} - {formatTime(slot.endTime)}
                                </div>
                                <button
                                    className="btn-cancel-slot"
                                    onClick={() => cancelSlot(slot.id)}
                                    disabled={loading}
                                    style={{ width: '100%', padding: '0.5rem', borderRadius: '6px', border: 'none', background: 'rgba(239, 68, 68, 0.2)', color: '#ef4444', fontWeight: 'bold', cursor: 'pointer' }}
                                >
                                    Remove
                                </button>
                            </div>
                        ))}
                    </div>
                )}
            </div>

            {/* Section: Upcoming Appointments */}
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
                                {apt.status === 'CONFIRMED' && (
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

                .status-badge.booked, .status-badge.confirmed {
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

                .day-option:hover {
                    background: rgba(255, 255, 255, 0.1);
                }

                .multi-select-trigger:hover {
                    background: rgba(255, 255, 255, 0.08) !important;
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
