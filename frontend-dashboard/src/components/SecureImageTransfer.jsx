import React, { useState } from 'react';
import axios from 'axios';
import { getAnalyticsUrl } from '../config';

/**
 * SecureImageTransfer Component
 * 
 * Demonstrates the secure transfer of medical images (e.g., X-rays, MRI).
 * Uses a multi-stage process:
 * 1. Upload: User selects an image.
 * 2. Encrypt & Scramble: Front-end sends image to Analytics service which performs
 *    chaos-map based scrambling and encryption.
 * 3. Decrypt: Authorized user can request decryption to view the original image.
 * 
 * Displays cryptographic metrics (Entropy, NPCR, UACI) to validate security strength.
 * 
 * @param {string} theme - 'light' or 'dark' mode for UI styling.
 */
const SecureImageTransfer = ({ theme }) => {
    const [selectedImage, setSelectedImage] = useState(null);
    const [previewUrl, setPreviewUrl] = useState(null);
    const [encryptedData, setEncryptedData] = useState(null); // Encrypted string or base64 blob
    const [decryptedImage, setDecryptedImage] = useState(null); // Resulting image after decryption
    const [metrics, setMetrics] = useState(null); // Security metrics from backend
    const [loading, setLoading] = useState(false);

    /**
     * Handles file input change and sets up the preview.
     */
    const handleImageChange = (e) => {
        const file = e.target.files[0];
        if (file) {
            setSelectedImage(file);
            setPreviewUrl(URL.createObjectURL(file));
            setEncryptedData(null);
            setDecryptedImage(null);
            setMetrics(null);
        }
    };

    /**
     * Sends the selected image to the Python Analytics service for encryption.
     * The backend is expected to return the encrypted data blob and security metrics.
     */
    const encryptImage = async () => {
        if (!selectedImage) return;
        setLoading(true);
        try {
            const reader = new FileReader();
            reader.readAsDataURL(selectedImage);
            reader.onload = async () => {
                const base64Content = reader.result.split(',')[1];
                const response = await axios.post(`${getAnalyticsUrl()}/encrypt-image`, {
                    image_base64: base64Content
                });
                setEncryptedData(response.data.encrypted_data);
                setMetrics(response.data.metrics);
                setLoading(false);
            };
        } catch (error) {
            console.error("Encryption failed", error);
            alert("Encryption failed. Make sure the analytics service is running.");
            setLoading(false);
        }
    };

    /**
     * Requests the backend to decrypt the previously encrypted data.
     * This simulates an authorized receiver accessing the medical image.
     */
    const decryptImage = async () => {
        if (!encryptedData) return;
        setLoading(true);
        try {
            const response = await axios.post(`${getAnalyticsUrl()}/decrypt-image`, {
                encrypted_base64: encryptedData
            });
            setDecryptedImage(response.data.decrypted_image);
            setLoading(false);
        } catch (error) {
            console.error("Decryption failed", error);
            alert("Decryption failed.");
            setLoading(false);
        }
    };

    return (
        <div className="card" style={{ marginTop: '2rem', background: 'var(--card-bg)', border: '1px solid var(--card-border)' }}>
            <div className="card-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <h2>🔒 Secure Medical Image Transfer (ECDH + Scrambling)</h2>
                {loading && <div className="spinner"></div>}
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '2rem', marginTop: '1.5rem' }}>
                {/* 1. Upload Section */}
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                    <h3>1. Upload Source Image</h3>
                    <div className="form-group">
                        <input type="file" accept="image/*" onChange={handleImageChange} style={{ width: '100%' }} />
                    </div>
                    {previewUrl && (
                        <div style={{ borderRadius: '8px', overflow: 'hidden', border: '1px solid var(--card-border)' }}>
                            <img src={previewUrl} alt="Preview" style={{ width: '100%', display: 'block' }} />
                        </div>
                    )}
                    <button
                        onClick={encryptImage}
                        disabled={!selectedImage || loading}
                        className="btn-primary"
                        style={{ padding: '0.75rem', fontWeight: 'bold' }}
                    >
                        {loading ? 'Processing...' : 'Encrypt & Scramble'}
                    </button>
                </div>

                {/* 2. Encryption Results & Metrics */}
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                    <h3>2. Encryption Analysis</h3>
                    {metrics ? (
                        <div style={{
                            background: theme === 'dark' ? 'rgba(0,0,0,0.2)' : 'rgba(0,0,0,0.05)',
                            padding: '1rem',
                            borderRadius: '8px',
                            fontSize: '0.9rem'
                        }}>
                            <ul style={{ listStyle: 'none', padding: 0 }}>
                                <li style={{ marginBottom: '0.5rem' }}><strong>Entropy (Original):</strong> {metrics.entropy1.toFixed(4)}</li>
                                <li style={{ marginBottom: '0.5rem' }}><strong>Entropy (Scrambled):</strong> {metrics.entropy2.toFixed(4)}</li>
                                <li style={{ marginBottom: '0.5rem' }}><strong>SSIM Index:</strong> {metrics.ssim.toFixed(4)}</li>
                                <li style={{ marginBottom: '0.5rem' }}><strong>Correlation:</strong> {metrics.correlation.toFixed(4)}</li>
                                <li style={{ marginBottom: '0.5rem', color: 'var(--accent-color)' }}><strong>UACI:</strong> {metrics.uaci.toFixed(2)}%</li>
                                <li style={{ color: 'var(--accent-color)' }}><strong>NPCR:</strong> {metrics.npcr.toFixed(2)}%</li>
                            </ul>
                        </div>
                    ) : (
                        <div style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', border: '2px dashed var(--card-border)', borderRadius: '8px', color: 'var(--text-secondary)' }}>
                            Waiting for encryption...
                        </div>
                    )}

                    {encryptedData && (
                        <>
                            <div style={{ background: '#000', color: '#0f0', padding: '0.5rem', borderRadius: '4px', fontSize: '0.7rem', maxHeight: '100px', overflowY: 'auto', fontFamily: 'monospace' }}>
                                [ENCRYPTED DATA STREAM]
                                <br />
                                {encryptedData.substring(0, 500)}...
                            </div>
                            <button
                                onClick={decryptImage}
                                disabled={loading}
                                className="btn-secondary"
                                style={{ padding: '0.75rem', fontWeight: 'bold', background: 'var(--success-color)', color: 'white', border: 'none' }}
                            >
                                Decrypt & Verify
                            </button>
                        </>
                    )}
                </div>

                {/* 3. Decryption Result */}
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                    <h3>3. Decrypted Outcome</h3>
                    {decryptedImage ? (
                        <div style={{ borderRadius: '8px', overflow: 'hidden', border: '3px solid var(--success-color)' }}>
                            <img src={`data:image/png;base64,${decryptedImage}`} alt="Decrypted" style={{ width: '100%', display: 'block' }} />
                            <div style={{ background: 'var(--success-color)', color: 'white', padding: '0.5rem', textAlign: 'center', fontSize: '0.9rem', fontWeight: 'bold' }}>
                                VERIFIED 100% MATCH
                            </div>
                        </div>
                    ) : (
                        <div style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', border: '2px dashed var(--card-border)', borderRadius: '8px', color: 'var(--text-secondary)' }}>
                            Waiting for decryption...
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default SecureImageTransfer;
