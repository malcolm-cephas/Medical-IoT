import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { getBackendUrl } from '../config';

const AppointmentBooking = ({ user }) => {
    const [doctors, setDoctors] = useState([]);
    const [selectedDoctor, setSelectedDoctor] = useState(null);
    const [slots, setSlots] = useState([]);
    const [appointments, setAppointments] = useState([]);
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState({ text: '', type: '' });

    const [bookingDetails, setBookingDetails] = useState({
        date: '',
        time: '10:00'
    });

    // ... (fetchDoctors and fetchMyAppointments remain same)

    const fetchDoctorSlots = async (doctorId) => {
        setLoading(true);
        try {
            const response = await axios.get(
                `${getBackendUrl()}/api/patient/all-doctors/${doctorId}/slots`
            );
            setSlots(response.data); // These are now "Office Hours"
            setSelectedDoctor(doctors.find(d => d.username === doctorId));
        } catch (error) {
            console.error('Error fetching office hours:', error);
            setMessage({ text: 'Failed to load doctor office hours', type: 'error' });
        } finally {
            setLoading(false);
        }
    };

    const bookAppointment = async (e) => {
        e.preventDefault();
        if (!bookingDetails.date || !bookingDetails.time) {
            setMessage({ text: 'Please select both date and time', type: 'error' });
            return;
        }

        setLoading(true);
        try {
            const appointmentTime = `${bookingDetails.date}T${bookingDetails.time}:00`;
            const response = await axios.post(
                `${getBackendUrl()}/api/patient/book-appointment`,
                {
                    doctorId: selectedDoctor.id,
                    appointmentTime: appointmentTime
                },
                { headers: { 'X-User-Id': user.username } }
            );
            setMessage({ text: response.data.message, type: 'success' });
            fetchMyAppointments();
        } catch (error) {
            console.error('Error booking appointment:', error);
            setMessage({
                text: error.response?.data?.error || 'Failed to book appointment',
                type: 'error'
            });
        } finally {
            setLoading(false);
        }
    };

    const formatTime = (timeString) => {
        if (!timeString) return 'N/A';
        return timeString.substring(0, 5);
    };

    // ... (cancelAppointment and formatDateTime remain same)

    return (
        <div className="appointment-booking">
            <h2>📅 Book Appointment</h2>

            {message.text && (
                <div className={`message ${message.type}`}>
                    {message.text}
                    <button onClick={() => setMessage({ text: '', type: '' })}>×</button>
                </div>
            )}

            {/* My Appointments */}
            <div className="section">
                <h3>My Appointments</h3>
                {appointments.length === 0 ? (
                    <p className="empty-state">No appointments yet</p>
                ) : (
                    <div className="appointments-list">
                        {appointments.map(apt => (
                            <div key={apt.id} className={`appointment-card status-${apt.status.toLowerCase()}`}>
                                <div className="apt-header">
                                    <span className="doctor-name">Dr. {apt.doctorId}</span>
                                    <span className={`status-badge ${apt.status.toLowerCase()}`}>
                                        {apt.status}
                                    </span>
                                </div>
                                <div className="apt-time">
                                    🕐 {formatDateTime(apt.appointmentTime)}
                                </div>
                                {apt.status === 'CONFIRMED' && (
                                    <button
                                        className="btn-cancel"
                                        onClick={() => cancelAppointment(apt.id)}
                                        disabled={loading}
                                    >
                                        Cancel Appointment
                                    </button>
                                )}
                            </div>
                        ))}
                    </div>
                )}
            </div>

            {/* Available Doctors */}
            <div className="section">
                <h3>Select a Doctor</h3>
                <div className="doctors-grid">
                    {doctors.map(doctor => (
                        <div
                            key={doctor.id}
                            className={`doctor-card ${selectedDoctor?.username === doctor.username ? 'selected' : ''}`}
                            onClick={() => fetchDoctorSlots(doctor.username)}
                        >
                            <div className="doctor-icon">👨‍⚕️</div>
                            <div className="doctor-info">
                                <h4>{doctor.username}</h4>
                                <p className="department">{doctor.department}</p>
                            </div>
                        </div>
                    ))}
                </div>
            </div>

            {/* Booking Form & Office Hours */}
            {selectedDoctor && (
                <div className="booking-container" style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem' }}>
                    <div className="section">
                        <h3>Dr. {selectedDoctor.username}'s Office Hours</h3>
                        {slots.length === 0 ? (
                            <p className="empty-state">No office hours set for this doctor.</p>
                        ) : (
                            <div className="office-hours-list">
                                {slots.map(slot => (
                                    <div key={slot.id} style={{ padding: '0.5rem', borderBottom: '1px solid rgba(255,255,255,0.1)', display: 'flex', justifyContent: 'space-between' }}>
                                        <span style={{ fontWeight: 'bold' }}>{slot.dayOfWeek}</span>
                                        <span>{formatTime(slot.startTime)} - {formatTime(slot.endTime)}</span>
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>

                    <div className="section">
                        <h3>Reserve a Time</h3>
                        <form onSubmit={bookAppointment} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                            <div className="form-group">
                                <label>Date:</label>
                                <input
                                    type="date"
                                    min={new Date().toISOString().split('T')[0]}
                                    value={bookingDetails.date}
                                    onChange={(e) => setBookingDetails({ ...bookingDetails, date: e.target.value })}
                                    required
                                    style={{ padding: '0.75rem', borderRadius: '8px', border: '1px solid rgba(255,255,255,0.2)', background: 'rgba(255,255,255,0.05)', color: 'white' }}
                                />
                            </div>
                            <div className="form-group">
                                <label>Time:</label>
                                <input
                                    type="time"
                                    value={bookingDetails.time}
                                    onChange={(e) => setBookingDetails({ ...bookingDetails, time: e.target.value })}
                                    required
                                    style={{ padding: '0.75rem', borderRadius: '8px', border: '1px solid rgba(255,255,255,0.2)', background: 'rgba(255,255,255,0.05)', color: 'white' }}
                                />
                            </div>
                            <button type="submit" className="btn-book" disabled={loading || slots.length === 0}>
                                {loading ? 'Booking...' : 'Confirm Appointment'}
                            </button>
                        </form>
                    </div>
                </div>
            )}

            <style>{`
                .appointment-booking {
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

                .appointments-list {
                    display: grid;
                    gap: 1rem;
                }

                .appointment-card {
                    padding: 1rem;
                    border-radius: 8px;
                    background: rgba(255, 255, 255, 0.05);
                    border: 1px solid rgba(255, 255, 255, 0.1);
                }

                .apt-header {
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    margin-bottom: 0.5rem;
                }

                .doctor-name {
                    font-weight: 600;
                    font-size: 1.1rem;
                }

                .status-badge {
                    padding: 0.25rem 0.75rem;
                    border-radius: 20px;
                    font-size: 0.85rem;
                    font-weight: 600;
                }

                .status-badge.confirmed {
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

                .doctors-grid {
                    display: grid;
                    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
                    gap: 1rem;
                }

                .doctor-card {
                    padding: 1.5rem;
                    border-radius: 12px;
                    background: rgba(255, 255, 255, 0.05);
                    border: 2px solid rgba(255, 255, 255, 0.1);
                    cursor: pointer;
                    transition: all 0.3s ease;
                    text-align: center;
                }

                .doctor-card:hover {
                    transform: translateY(-4px);
                    border-color: var(--accent-color);
                    box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);
                }

                .doctor-card.selected {
                    border-color: var(--accent-color);
                    background: rgba(var(--accent-rgb), 0.1);
                }

                .doctor-icon {
                    font-size: 3rem;
                    margin-bottom: 0.5rem;
                }

                .department {
                    color: var(--text-secondary);
                    font-size: 0.9rem;
                    margin-top: 0.25rem;
                }

                .slots-grid {
                    display: grid;
                    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
                    gap: 1rem;
                }

                .slot-card {
                    padding: 1rem;
                    border-radius: 8px;
                    background: rgba(255, 255, 255, 0.05);
                    border: 1px solid rgba(255, 255, 255, 0.1);
                    display: flex;
                    flex-direction: column;
                    gap: 0.75rem;
                }

                .slot-time {
                    font-size: 0.9rem;
                    color: var(--text-secondary);
                }

                .btn-book, .btn-cancel {
                    padding: 0.75rem 1.5rem;
                    border-radius: 8px;
                    border: none;
                    font-weight: 600;
                    cursor: pointer;
                    transition: all 0.3s ease;
                }

                .btn-book {
                    background: var(--accent-color);
                    color: white;
                }

                .btn-book:hover:not(:disabled) {
                    transform: translateY(-2px);
                    box-shadow: 0 4px 12px rgba(var(--accent-rgb), 0.4);
                }

                .btn-book:disabled {
                    opacity: 0.5;
                    cursor: not-allowed;
                }

                .btn-cancel {
                    background: rgba(239, 68, 68, 0.2);
                    color: #ef4444;
                }

                .btn-cancel:hover:not(:disabled) {
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

export default AppointmentBooking;
