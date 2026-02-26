
import React, { useState } from 'react';
import axios from 'axios';
import { getBackendUrl } from '../config';

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

    /**
     * Handles the form submission to create a new prescription.
     *
     * @param {Event} e - The submit event
     */
    const handleSubmit = async (e) => {
        e.preventDefault(); // Prevent default HTML form submission behavior

        // Validation: Ensure a patient is selected
        if (!selectedPatientId) {
            alert("Please select a patient first.");
            return;
        }

        setLoading(true); // Start loading state
        try {
            // Make a POST request to the backend to save the prescription
            await axios.post(`${getBackendUrl()}/api/prescriptions/add`, {
                doctorId: doctorId, // Pass doctor ID (User context)
                patientId: selectedPatientId, // Pass selected patient ID
                diagnosis,
                medicine,
                notes
            });

            // If successful, show success message
            setSuccess(true);

            // Reset form and close component after a short delay
            setTimeout(() => {
                setSuccess(false);
                setDiagnosis('');
                setMedicine('');
                setNotes('');
                if (onClose) onClose(); // Trigger parent's close callback
            }, 2000);
        } catch (error) {
            console.error("Error creating prescription:", error);
            alert("Failed to save prescription.");
        } finally {
            setLoading(false); // Stop loading state regardless of success or failure
        }
    };

    return (
        <div className="bg-white p-6 rounded-lg shadow-lg border border-gray-200">
            {/* Header Section */}
            <div className="flex justify-between items-center mb-4">
                <h3 className="text-xl font-bold text-indigo-700 flex items-center">
                    <span className="mr-2">💊</span> Prescription Pad
                </h3>
                {selectedPatientId && (
                    <span className="text-sm bg-blue-100 text-blue-800 py-1 px-2 rounded-full">
                        Patient ID: {selectedPatientId}
                    </span>
                )}
            </div>

            {/* Conditional Rendering: Show Success Message or Form */}
            {success ? (
                <div className="bg-green-100 text-green-700 p-4 rounded text-center">
                    ✅ Prescription Sent Successfully!
                </div>
            ) : (
                <form onSubmit={handleSubmit} className="space-y-4">
                    {/* Diagnosis Input Field */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Diagnosis</label>
                        <input
                            type="text"
                            required
                            className="w-full border border-gray-300 rounded-md p-2 focus:ring-indigo-500 focus:border-indigo-500"
                            placeholder="e.g. Acute Bronchitis"
                            value={diagnosis}
                            onChange={(e) => setDiagnosis(e.target.value)}
                        />
                    </div>

                    {/* Medication (Rx) Textarea */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Rx (Medication)</label>
                        <textarea
                            required
                            rows="3"
                            className="w-full border border-gray-300 rounded-md p-2 focus:ring-indigo-500 focus:border-indigo-500 font-mono text-sm"
                            placeholder="e.g. Amoxicillin 500mg - 1 tablet every 8 hours for 7 days"
                            value={medicine}
                            onChange={(e) => setMedicine(e.target.value)}
                        />
                    </div>

                    {/* Notes Textarea */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Notes / Advice</label>
                        <textarea
                            rows="2"
                            className="w-full border border-gray-300 rounded-md p-2 focus:ring-indigo-500 focus:border-indigo-500"
                            placeholder="e.g. Drink plenty of fluids, rest for 3 days."
                            value={notes}
                            onChange={(e) => setNotes(e.target.value)}
                        />
                    </div>

                    {/* Submit Button */}
                    <button
                        type="submit"
                        disabled={loading || !selectedPatientId} // Disable if loading or no patient selected
                        className={`w-full py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white 
              ${loading || !selectedPatientId ? 'bg-gray-400 cursor-not-allowed' : 'bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500'}`}
                    >
                        {loading ? 'Processing...' : 'Issue Prescription'}
                    </button>
                </form>
            )}
        </div>
    );
};

export default PrescriptionPad;
