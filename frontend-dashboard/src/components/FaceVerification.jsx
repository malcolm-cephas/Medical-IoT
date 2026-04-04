import React, { useEffect, useRef, useState } from 'react';

/**
 * AI-Powered FaceVerification Component
 * 
 * Enhanced UI with biometric scanning animations and real-time feel. 
 * Delegates processing to the Python+OpenCV AI Backend with robust detect-then-encode logic.
 */
const FaceVerification = ({ username, mode, onSuccess, onCancel }) => {
    const videoRef = useRef();
    const canvasRef = useRef();
    const [isCaptureLoading, setIsCaptureLoading] = useState(false);
    const [error, setError] = useState(null);
    const [stream, setStream] = useState(null);
    const [scanningPos, setScanningPos] = useState(0);

    useEffect(() => {
        const startVideo = () => {
            navigator.mediaDevices.getUserMedia({ 
                video: { 
                    width: { ideal: 1280 }, 
                    height: { ideal: 720 },
                    facingMode: "user"
                } 
            })
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

        // Scanning animation loop
        const interval = setInterval(() => {
            setScanningPos(prev => (prev + 2) % 100);
        }, 30);

        return () => {
            if (stream) {
                stream.getTracks().forEach(track => track.stop());
            }
            clearInterval(interval);
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
            
            // Flip horizontal for natural feel if needed, but here we just capture raw
            context.drawImage(video, 0, 0, canvas.width, canvas.height);

            // Convert to Base64
            const imageData = canvas.toDataURL('image/jpeg', 0.95);

            // Pass the raw image to the parent handler
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
                    <span className="dot"></span> SECURE BIOMETRIC LINK ACTIVE
                </div>
                <h3>{mode === 'enroll' ? 'Face ID Enrollment' : 'Identity Verification'}</h3>
                <p className="subtitle">Hello, <strong>{username}</strong>. Position your face within the frame.</p>
                
                <div className="video-container">
                    {error && <div className="error-overlay">⚠️ {error}</div>}
                    <video 
                        ref={videoRef} 
                        autoPlay 
                        muted 
                        playsInline
                    />
                    
                    {/* Biometric Frame Overlay */}
                    <div className="face-frame-overlay">
                        <div className="corner top-left"></div>
                        <div className="corner top-right"></div>
                        <div className="corner bottom-left"></div>
                        <div className="corner bottom-right"></div>
                        
                        {/* Scanning Bar */}
                        <div className="scanning-bar" style={{ top: `${scanningPos}%` }}></div>
                    </div>

                    <div className="ai-status-tag">
                        <span className="pulse-icon">●</span> ENGINE: HAAR + DEEP_ML
                    </div>

                    {isCaptureLoading && <div className="loading-spinner-overlay">
                        <div className="spinner"></div>
                        <span>Analyzing Facial Vectors...</span>
                    </div>}
                </div>

                {/* Hidden canvas for frame extraction */}
                <canvas ref={canvasRef} style={{ display: 'none' }}></canvas>

                <div className="actions">
                    <button 
                        onClick={captureFrame} 
                        disabled={isCaptureLoading || !!error}
                        className="btn-primary-ai"
                    >
                        {isCaptureLoading ? 'Processing...' : (mode === 'enroll' ? 'Capture Face Data' : 'Verify Identity')}
                    </button>
                    <button onClick={onCancel} className="btn-secondary-link">Cancel</button>
                </div>
            </div>

            <style>{`
                .face-verify-modal {
                    position: fixed; top: 0; left: 0; width: 100%; height: 100%;
                    background: rgba(10, 10, 20, 0.95); display: flex; align-items: center; justify-content: center;
                    z-index: 2000; backdrop-filter: blur(15px);
                }
                .face-verify-card {
                    background: #11111d; padding: 2.5rem; border-radius: 30px;
                    width: 550px; text-align: center; border: 1px solid #2a2a40;
                    box-shadow: 0 40px 100px rgba(0,0,0,0.8); color: white;
                    position: relative; overflow: hidden;
                }
                .face-verify-card::before {
                    content: ''; position: absolute; top: -50%; left: -50%; width: 200%; height: 200%;
                    background: radial-gradient(circle, rgba(99, 102, 241, 0.05) 0%, transparent 70%);
                    pointer-events: none;
                }
                .subtitle { color: #94a3b8; font-size: 0.9rem; margin-top: 0.5rem; }
                
                .status-indicator {
                    font-size: 0.65rem; text-transform: uppercase; letter-spacing: 2px;
                    color: #4ade80; margin-bottom: 1.5rem; display: flex; align-items: center; justify-content: center; gap: 8px;
                    font-weight: 800;
                }
                .status-indicator .dot {
                    width: 6px; height: 6px; background: #4ade80; border-radius: 50%;
                    box-shadow: 0 0 10px #4ade80; animation: pulse 1.5s infinite;
                }
                @keyframes pulse { 0% { opacity: 1; transform: scale(1); } 50% { opacity: 0.4; transform: scale(1.2); } 100% { opacity: 1; transform: scale(1); } }
                
                .video-container {
                    position: relative; margin: 2rem 0;
                    background: #000; border-radius: 20px; overflow: hidden;
                    aspect-ratio: 16/9; box-shadow: inset 0 0 40px rgba(0,0,0,1);
                    border: 2px solid #2a2a40;
                }
                video { width: 100%; height: 100%; object-fit: cover; }
                
                /* Face Frame */
                .face-frame-overlay {
                    position: absolute; top: 15%; left: 30%; width: 40%; height: 70%;
                    pointer-events: none;
                }
                .corner {
                    position: absolute; width: 30px; height: 30px;
                    border: 3px solid #6366f1;
                }
                .top-left { top: 0; left: 0; border-right: none; border-bottom: none; border-top-left-radius: 10px; }
                .top-right { top: 0; right: 0; border-left: none; border-bottom: none; border-top-right-radius: 10px; }
                .bottom-left { bottom: 0; left: 0; border-right: none; border-top: none; border-bottom-left-radius: 10px; }
                .bottom-right { bottom: 0; right: 0; border-left: none; border-top: none; border-bottom-right-radius: 10px; }
                
                .scanning-bar {
                    position: absolute; left: 5%; width: 90%; height: 2px;
                    background: linear-gradient(90deg, transparent, #6366f1, transparent);
                    box-shadow: 0 0 15px #6366f1; transition: top 0.05s linear;
                }
                
                .ai-status-tag {
                    position: absolute; bottom: 15px; left: 20px;
                    background: rgba(0,0,0,0.6); padding: 5px 12px; border-radius: 20px;
                    font-size: 0.7rem; color: #6366f1; border: 1px solid rgba(99, 102, 241, 0.3);
                    display: flex; align-items: center; gap: 6px;
                }
                .pulse-icon { font-size: 10px; animation: blink 1s infinite; }
                @keyframes blink { 0% { opacity: 0; } 50% { opacity: 1; } 100% { opacity: 0; } }
                
                .loading-spinner-overlay {
                    position: absolute; top: 0; left: 0; width: 100%; height: 100%;
                    background: rgba(10,10,20,0.85); display: flex; flex-direction: column; align-items: center; justify-content: center;
                    gap: 15px; z-index: 10;
                }
                .spinner {
                    width: 40px; height: 40px; border: 3px solid rgba(99, 102, 241, 0.2);
                    border-top-color: #6366f1; border-radius: 50%; animation: spin 0.8s linear infinite;
                }
                @keyframes spin { to { transform: rotate(360deg); } }
                
                .error-overlay { background: rgba(220, 38, 38, 0.9); position: absolute; top: 0; left: 0; width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; padding: 2rem; text-align: center; font-weight: 600; z-index: 20; }
                
                .btn-primary-ai {
                    background: linear-gradient(135deg, #6366f1, #a855f7);
                    color: white; border: none; padding: 1rem 2.5rem;
                    border-radius: 15px; font-weight: 700; cursor: pointer;
                    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
                    text-transform: uppercase; letter-spacing: 1px;
                }
                .btn-primary-ai:hover:not(:disabled) { transform: translateY(-3px); box-shadow: 0 10px 25px rgba(168, 85, 247, 0.5); }
                .btn-primary-ai:disabled { background: #334155; color: #94a3b8; cursor: not-allowed; }
                
                .btn-secondary-link {
                    background: transparent; color: #94a3b8; border: none; padding: 0.8rem 1.5rem;
                    cursor: pointer; font-weight: 600; transition: color 0.2s;
                }
                .btn-secondary-link:hover { color: white; }
            `}</style>
        </div>
    );
};

export default FaceVerification;

