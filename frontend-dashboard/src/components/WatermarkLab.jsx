import React, { useState } from 'react';
import axios from 'axios';
import { getBackendUrl } from '../config';
import './WatermarkLab.css';

/**
 * WatermarkLab Component
 * Provides a UI for the Traceable Invisible Watermark Feature.
 * Allows users to embed, verify, and decode watermarks from medical images.
 */
const WatermarkLab = () => {
  const [embedFile, setEmbedFile] = useState(null);
  const [embedDoctorId, setEmbedDoctorId] = useState('');
  const [watermarkedImage, setWatermarkedImage] = useState(null);
  
  const [verifyFile, setVerifyFile] = useState(null);
  const [verifyDoctorId, setVerifyDoctorId] = useState('');
  const [verifyResult, setVerifyResult] = useState(null);

  const [decodeFile, setDecodeFile] = useState(null);
  const [decodeResult, setDecodeResult] = useState(null);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleEmbed = async () => {
    if (!embedFile || !embedDoctorId) return;
    setLoading(true);
    setError(null);
    try {
      const formData = new FormData();
      formData.append('file', embedFile);
      formData.append('doctorId', embedDoctorId);

      const response = await axios.post(`${getBackendUrl()}/api/watermark/embed`, formData, {
        responseType: 'blob',
      });

      const url = URL.createObjectURL(response.data);
      setWatermarkedImage(url);
    } catch (err) {
      setError('Failed to embed watermark. Ensure the file is a valid image.');
    } finally {
      setLoading(false);
    }
  };

  const handleVerify = async () => {
    if (!verifyFile || !verifyDoctorId) return;
    setLoading(true);
    setError(null);
    try {
      const formData = new FormData();
      formData.append('file', verifyFile);
      formData.append('doctorId', verifyDoctorId);

      const response = await axios.post(`${getBackendUrl()}/api/watermark/verify`, formData);
      setVerifyResult(response.data);
    } catch (err) {
      setError('Failed to verify watermark.');
    } finally {
      setLoading(false);
    }
  };

  const handleDecode = async () => {
    if (!decodeFile) return;
    setLoading(true);
    setError(null);
    try {
      const formData = new FormData();
      formData.append('file', decodeFile);

      const response = await axios.post(`${getBackendUrl()}/api/watermark/decode`, formData);
      setDecodeResult(response.data);
    } catch (err) {
      setError('Failed to decode watermark.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="watermark-lab-container">
      <header className="lab-header">
        <h1>Traceability Lab</h1>
        <p className="subtitle">Invisible Medical Image Watermarking & Forensic Analysis</p>
        <div className="paper-citation">
          Inspired by: <em>"Building an Invisible Shield..."</em> — Wenying Wen et al., IEEE TCSVT 2026
        </div>
      </header>

      {error && <div className="error-banner">{error}</div>}

      <div className="lab-grid">
        {/* Section 1: Embed */}
        <div className="lab-card">
          <div className="card-icon">🛡️</div>
          <h2>Embed Identity</h2>
          <p>Invisibly stamp a doctor's 64-bit ID into image pixels.</p>
          
          <div className="input-group">
            <label>Medical Image (.png)</label>
            <input type="file" accept="image/png" onChange={(e) => setEmbedFile(e.target.files[0])} />
          </div>

          <div className="input-group">
            <label>Doctor Username/ID</label>
            <input 
              type="text" 
              placeholder="e.g. doctor123" 
              value={embedDoctorId} 
              onChange={(e) => setEmbedDoctorId(e.target.value)} 
            />
          </div>

          <button onClick={handleEmbed} disabled={loading || !embedFile || !embedDoctorId}>
            {loading ? 'Processing...' : 'Embed Watermark'}
          </button>

          {watermarkedImage && (
            <div className="result-area">
              <p className="success-text">Watermark Embedded Successfully!</p>
              <div className="preview-box">
                <img src={watermarkedImage} alt="Watermarked Preview" />
              </div>
              <a href={watermarkedImage} download={`watermarked_${embedDoctorId}.png`} className="download-btn">
                Download Protected Image
              </a>
            </div>
          )}
        </div>

        {/* Section 2: Verify */}
        <div className="lab-card">
          <div className="card-icon">🔍</div>
          <h2>Verify Ownership</h2>
          <p>Check if a specific doctor's fingerprint exists in the image.</p>

          <div className="input-group">
            <label>Leaked Image</label>
            <input type="file" accept="image/png" onChange={(e) => setVerifyFile(e.target.files[0])} />
          </div>

          <div className="input-group">
            <label>Suspected Doctor ID</label>
            <input 
              type="text" 
              placeholder="e.g. doctor123" 
              value={verifyDoctorId} 
              onChange={(e) => setVerifyDoctorId(e.target.value)} 
            />
          </div>

          <button onClick={handleVerify} className="secondary" disabled={loading || !verifyFile || !verifyDoctorId}>
            {loading ? 'Verifying...' : 'Run Forensic Match'}
          </button>

          {verifyResult && (
            <div className={`result-area ${verifyResult.match ? 'match' : 'no-match'}`}>
              <div className="match-indicator">{verifyResult.match ? '✅ MATCH' : '❌ NO MATCH'}</div>
              <p>{verifyResult.message}</p>
            </div>
          )}
        </div>

        {/* Section 3: Decode */}
        <div className="lab-card">
          <div className="card-icon">🧬</div>
          <h2>Decode Fingerprint</h2>
          <p>Extract the raw 64-bit hidden data from image pixels.</p>

          <div className="input-group">
            <label>Encrypted Image</label>
            <input type="file" accept="image/png" onChange={(e) => setDecodeFile(e.target.files[0])} />
          </div>

          <button onClick={handleDecode} className="accent" disabled={loading || !decodeFile}>
            {loading ? 'Extracting...' : 'Decode 64-bit ID'}
          </button>

          {decodeResult && (
            <div className="result-area bits-area">
              <p>Extracted Fingerprint:</p>
              <code className="binary-stream">{decodeResult.extracted_bits}</code>
              <div className="bit-count">{decodeResult.bit_length} bits recovered</div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default WatermarkLab;
