import React, { useEffect, useRef, useState } from 'react';
import * as faceapi from 'face-api.js';

/**
 * FaceVerification Component
 * 
 * Handles real-time facial recognition using face-api.js.
 * Loads pre-trained models, accesses the webcam, and extracts facial descriptors.
 * 
 * @param {Object} props
 * @param {string} props.username - The username to verify or enroll.
 * @param {string} props.mode - 'enroll' or 'verify'.
 * @param {Function} props.onSuccess - Callback on successful verification/capture.
 * @param {Function} props.onCancel - Callback to close the component.
 */
const FaceVerification = ({ username, mode, onSuccess, onCancel }) => {
    const videoRef = useRef();
    const [modelsLoaded, setModelsLoaded] = useState(false);
    const [isCaptureLoading, setIsCaptureLoading] = useState(false);
    const [error, setError] = useState(null);
    const [stream, setStream] = useState(null);

    useEffect(() => {
        const loadModels = async () => {
            try {
                // Models are stored in the /public/models folder
                const MODEL_URL = '/models';
                await faceapi.nets.tinyFaceDetector.loadFromUri(MODEL_URL);
                await faceapi.nets.faceLandmark68Net.loadFromUri(MODEL_URL);
                await faceapi.nets.faceRecognitionNet.loadFromUri(MODEL_URL);
                setModelsLoaded(true);
            } catch (err) {
                console.error("Failed to load models", err);
                setError("Failed to initialize biometric sensors.");
            }
        };
        loadModels();
    }, []);

    const startVideo = () => {
        navigator.mediaDevices.getUserMedia({ video: {} })
            .then(currentStream => {
                videoRef.current.srcObject = currentStream;
                setStream(currentStream);
            })
            .catch(err => {
                console.error(err);
                setError("Cannot access webcam. Please check permissions.");
            });
    };

    useEffect(() => {
        if (modelsLoaded) {
            startVideo();
        }
        return () => {
            if (stream) {
                stream.getTracks().forEach(track => track.stop());
            }
        };
    }, [modelsLoaded]);

    const handleAction = async () => {
        if (!videoRef.current) return;
        setIsCaptureLoading(true);

        try {
            // Detect face and extract 128-float descriptor
            const detection = await faceapi.detectSingleFace(
                videoRef.current, 
                new faceapi.TinyFaceDetectorOptions()
            ).withFaceLandmarks().withFaceDescriptor();

            if (detection) {
                // Convert Float32Array to standard array for JSON serialization
                const descriptorArray = Array.from(detection.descriptor);
                onSuccess(descriptorArray);
            } else {
                setError("Face not detected. Please ensure you are well-lit and facing the camera.");
            }
        } catch (err) {
            console.error(err);
            setError("Biometric processing failed.");
        } finally {
            setIsCaptureLoading(false);
        }
    };

    return (
        <div className="face-verify-modal">
            <div className="face-verify-card">
                <h3>{mode === 'enroll' ? 'Biometric Enrollment' : 'Face Verification'}</h3>
                <p>Hello, <strong>{username}</strong>. Please look at the camera.</p>
                
                <div className="video-container">
                    {!modelsLoaded && <div className="loading-overlay">Loading Models...</div>}
                    {error && <div className="error-overlay">{error}</div>}
                    <video 
                        ref={videoRef} 
                        autoPlay 
                        muted 
                        width="320" 
                        height="240" 
                        style={{ borderRadius: '8px', border: '2px solid var(--accent-color)' }}
                    />
                </div>

                <div className="actions">
                    <button 
                        onClick={handleAction} 
                        disabled={!modelsLoaded || isCaptureLoading}
                        className="btn-primary"
                    >
                        {isCaptureLoading ? 'Scanning...' : (mode === 'enroll' ? 'Capture Face' : 'Verify Identity')}
                    </button>
                    <button onClick={onCancel} className="btn-secondary">Cancel</button>
                </div>
            </div>

            <style>{`
                .face-verify-modal {
                    position: fixed; top: 0; left: 0; width: 100%; height: 100%;
                    background: rgba(0,0,0,0.85); display: flex; align-items: center; justify-content: center;
                    z-index: 2000; backdrop-filter: blur(5px);
                }
                .face-verify-card {
                    background: var(--card-bg); padding: 2rem; border-radius: 12px;
                    width: 400px; text-align: center; border: 1px solid var(--card-border);
                    box-shadow: 0 10px 25px rgba(0,0,0,0.5);
                }
                .video-container {
                    position: relative; margin: 1.5rem 0;
                    background: #000; border-radius: 8px; overflow: hidden;
                    height: 240px;
                }
                .loading-overlay, .error-overlay {
                    position: absolute; top: 0; left: 0; width: 100%; height: 100%;
                    display: flex; align-items: center; justify-content: center;
                    color: white; font-weight: bold; padding: 1rem;
                }
                .error-overlay { background: rgba(220, 38, 38, 0.7); }
                .actions { display: flex; gap: 1rem; justify-content: center; }
            `}</style>
        </div>
    );
};

export default FaceVerification;
