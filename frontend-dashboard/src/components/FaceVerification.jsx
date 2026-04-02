import React, { useEffect, useRef, useState } from 'react';

/**
 * AI-Powered FaceVerification Component
 * 
 * Lightweight frontend that captures a high-resolution frame from the webcam
 * and delegates processing to the Python+Ultralytics AI Backend.
 */
const FaceVerification = ({ username, mode, onSuccess, onCancel }) => {
    const videoRef = useRef();
    const canvasRef = useRef();
    const [isCaptureLoading, setIsCaptureLoading] = useState(false);
    const [error, setError] = useState(null);
    const [stream, setStream] = useState(null);

    useEffect(() => {
        const startVideo = () => {
            navigator.mediaDevices.getUserMedia({ video: { width: 1280, height: 720 } })
                .then(currentStream => {
                    if (videoRef.current) {
                        videoRef.current.srcObject = currentStream;
                        setStream(currentStream);
                    }
                })
                .catch(err => {
                    console.error(err);
                    setError("Cannot access webcam. Please check permissions.");
                });
        };
        startVideo();

        return () => {
            if (stream) {
                stream.getTracks().forEach(track => track.stop());
            }
        };
    }, []);

    const captureFrame = async () => {
        if (!videoRef.current || !canvasRef.current) return;
        setIsCaptureLoading(true);
        setError(null);

        try {
            const video = videoRef.current;
            const canvas = canvasRef.current;
            const context = canvas.getContext('2d');

            // Draw current video frame to hidden canvas
            canvas.width = video.videoWidth;
            canvas.height = video.videoHeight;
            context.drawImage(video, 0, 0, canvas.width, canvas.height);

            // Convert to Base64
            const imageData = canvas.toDataURL('image/jpeg', 0.9);

            // Pass the raw image to the parent handler (which calls the Python AI)
            await onSuccess(imageData);
            
        } catch (err) {
            console.error(err);
            setError("Image capture failed: " + err.message);
        } finally {
            setIsCaptureLoading(false);
        }
    };

    return (
        <div className="face-verify-modal">
            <div className="face-verify-card">
                <div className="status-indicator">
                    <span className="dot"></span> AI AI-Assisted Biometrics
                </div>
                <h3>{mode === 'enroll' ? 'Biometric Enrollment' : 'Identity Verification'}</h3>
                <p>Hello, <strong>{username}</strong>. Please face the camera directly.</p>
                
                <div className="video-container">
                    {error && <div className="error-overlay">⚠️ {error}</div>}
                    <video 
                        ref={videoRef} 
                        autoPlay 
                        muted 
                        playsInline
                    />
                    {isCaptureLoading && <div className="loading-spinner-overlay">Analyzing with Haar AI...</div>}
                </div>

                {/* Hidden canvas for frame extraction */}
                <canvas ref={canvasRef} style={{ display: 'none' }}></canvas>

                <div className="actions">
                    <button 
                        onClick={captureFrame} 
                        disabled={isCaptureLoading}
                        className="btn-primary-ai"
                    >
                        {isCaptureLoading ? 'Processing...' : (mode === 'enroll' ? 'Capture Face Data' : 'Verify Identity')}
                    </button>
                    <button onClick={onCancel} className="btn-secondary">Cancel</button>
                </div>
            </div>

            <style>{`
                .face-verify-modal {
                    position: fixed; top: 0; left: 0; width: 100%; height: 100%;
                    background: rgba(0,0,0,0.92); display: flex; align-items: center; justify-content: center;
                    z-index: 2000; backdrop-filter: blur(10px);
                }
                .face-verify-card {
                    background: #1a1a2e; padding: 2.5rem; border-radius: 20px;
                    width: 500px; text-align: center; border: 1px solid #303050;
                    box-shadow: 0 20px 50px rgba(0,0,0,0.6); color: white;
                }
                .status-indicator {
                    font-size: 0.7rem; text-transform: uppercase; letter-spacing: 1px;
                    color: #4ade80; margin-bottom: 1rem; display: flex; align-items: center; justify-content: center; gap: 5px;
                }
                .status-indicator .dot {
                    width: 8px; height: 8px; background: #4ade80; border-radius: 50%;
                    box-shadow: 0 0 10px #4ade80; animation: pulse 2s infinite;
                }
                @keyframes pulse { 0% { opacity: 1; } 50% { opacity: 0.3; } 100% { opacity: 1; } }
                
                .video-container {
                    position: relative; margin: 2rem 0;
                    background: #000; border-radius: 12px; overflow: hidden;
                    aspect-ratio: 16/9; box-shadow: inset 0 0 20px rgba(0,0,0,1);
                    border: 2px solid #303050;
                }
                video { width: 100%; height: 100%; object-fit: cover; }
                
                .loading-spinner-overlay {
                    position: absolute; top: 0; left: 0; width: 100%; height: 100%;
                    background: rgba(0,0,0,0.7); display: flex; align-items: center; justify-content: center;
                    font-size: 0.9rem; color: #4ade80;
                }
                .error-overlay { background: rgba(220, 38, 38, 0.85); position: absolute; top: 0; left: 0; width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; padding: 1rem; }
                
                .btn-primary-ai {
                    background: linear-gradient(135deg, #6366f1, #a855f7);
                    color: white; border: none; padding: 0.8rem 1.5rem;
                    border-radius: 10px; font-weight: 600; cursor: pointer;
                    transition: transform 0.2s, box-shadow 0.2s;
                }
                .btn-primary-ai:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 5px 15px rgba(168, 85, 247, 0.4); }
                .btn-primary-ai:disabled { background: #4b5563; cursor: not-allowed; }
            `}</style>
        </div>
    );
};

export default FaceVerification;
