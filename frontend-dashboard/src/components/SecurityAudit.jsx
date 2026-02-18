import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { getBackendUrl } from '../config';

const SecurityAudit = ({ theme }) => {
    const [chain, setChain] = useState([]);
    const [lockdownStatus, setLockdownStatus] = useState({ isLockdown: false, reason: '' });
    const [loading, setLoading] = useState(true);
    const [events, setEvents] = useState([]);

    useEffect(() => {
        fetchData();
        const interval = setInterval(fetchData, 5000);
        return () => clearInterval(interval);
    }, []);

    const fetchData = async () => {
        try {
            const [chainRes, statusRes, eventsRes] = await Promise.all([
                axios.get(`${getBackendUrl()}/api/security/audit-trail`),
                axios.get(`${getBackendUrl()}/api/security/status`),
                axios.get(`${getBackendUrl()}/api/security/events`)
            ]);
            setChain(chainRes.data);
            setLockdownStatus(statusRes.data);
            setEvents(eventsRes.data);
            setLoading(false);
        } catch (error) {
            console.error("Failed to fetch security data", error);
            setLoading(false);
        }
    };

    const toggleLockdown = async () => {
        const url = lockdownStatus.isLockdown 
            ? `${getBackendUrl()}/api/security/unlock` 
            : `${getBackendUrl()}/api/security/lockdown`;
            
        try {
            await axios.post(url, { reason: "Manual Administrator Action" });
            fetchData();
        } catch (error) {
            alert("Failed to toggle lockdown");
        }
    };

    return (
        <div style={{ padding: '1rem' }}>
            {/* Header with Lockdown Control */}
            <div className="card" style={{ 
                marginBottom: '2rem', 
                border: lockdownStatus.isLockdown ? '2px solid #ff4444' : '1px solid var(--card-border)',
                background: lockdownStatus.isLockdown ? 'rgba(255, 68, 68, 0.1)' : 'var(--card-bg)'
            }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div>
                        <h2 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                            🛡️ Security Overview 
                            {lockdownStatus.isLockdown && <span className="badge badge-critical">SYSTEM LOCKDOWN ACTIVE</span>}
                        </h2>
                        <p style={{ color: 'var(--text-secondary)' }}>
                            Immutable audit trail and real-time threat monitoring.
                        </p>
                    </div>
                    <button 
                        onClick={toggleLockdown}
                        className={lockdownStatus.isLockdown ? "btn-primary" : "btn-warning"}
                        style={{ background: lockdownStatus.isLockdown ? '#4CAF50' : '#ff4444', borderColor: 'transparent' }}
                    >
                        {lockdownStatus.isLockdown ? "🔓 Lift Lockdown" : "🔒 Initiate Emergency Lockdown"}
                    </button>
                </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '2rem' }}>
                
                {/* Blockchain Visualizer */}
                <div className="card">
                    <h3>🔗 Blockchain Audit Trail (Immutable Ledger)</h3>
                    <div style={{ 
                        marginTop: '1rem', 
                        maxHeight: '600px', 
                        overflowY: 'auto', 
                        padding: '1rem',
                        background: theme === 'dark' ? '#1a1a1a' : '#f5f5f5',
                        borderRadius: '8px'
                    }}>
                        {chain.length === 0 ? (
                            <p>No transactions recorded yet.</p>
                        ) : (
                            chain.slice(0).reverse().map((block, index) => (
                                <div key={block.hash} style={{ position: 'relative', marginBottom: '2rem' }}>
                                    {/* Link Line */}
                                    {index < chain.length - 1 && (
                                        <div style={{ 
                                            position: 'absolute', 
                                            left: '20px', 
                                            bottom: '-35px', 
                                            width: '2px', 
                                            height: '35px', 
                                            background: '#666' 
                                        }}></div>
                                    )}
                                    
                                    <div style={{ 
                                        padding: '1rem', 
                                        background: 'var(--card-bg)', 
                                        border: '1px solid var(--card-border)',
                                        borderRadius: '8px',
                                        boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
                                    }}>
                                        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
                                            <span style={{ fontWeight: 'bold', fontFamily: 'monospace', color: '#00bcd4' }}>
                                                BLOCK #{chain.length - 1 - index}
                                            </span>
                                            <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
                                                {new Date(block.timestamp).toLocaleString()}
                                            </span>
                                        </div>
                                        
                                        <div style={{ fontFamily: 'monospace', fontSize: '0.85rem', wordBreak: 'break-all' }}>
                                            <div style={{ marginBottom: '0.25rem' }}>
                                                <span style={{ color: '#888' }}>HASH:</span> {block.hash}
                                            </div>
                                            <div style={{ marginBottom: '0.25rem' }}>
                                                <span style={{ color: '#888' }}>PREV:</span> {block.prevHash}
                                            </div>
                                            <div style={{ 
                                                marginTop: '0.5rem', 
                                                padding: '0.5rem', 
                                                background: theme === 'dark' ? '#333' : '#eee', 
                                                borderRadius: '4px',
                                                borderLeft: '3px solid #00bcd4'
                                            }}>
                                                {block.data}
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            ))
                        )}
                    </div>
                </div>

                {/* Security Events & Stats */}
                <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
                    <div className="card">
                        <h3>🚨 Recent Security Events</h3>
                        <div style={{ marginTop: '1rem' }}>
                            {events.length === 0 ? (
                                <p style={{ color: 'var(--text-secondary)' }}>No recent security alerts.</p>
                            ) : (
                                events.map(event => (
                                    <div key={event.id} style={{ 
                                        padding: '0.75rem', 
                                        marginBottom: '0.5rem',
                                        borderLeft: `4px solid ${event.severity === 'CRITICAL' ? '#ff4444' : event.severity === 'WARN' ? '#ffbb33' : '#00C851'}`,
                                        background: theme === 'dark' ? 'rgba(255,255,255,0.05)' : 'rgba(0,0,0,0.02)'
                                    }}>
                                        <div style={{ fontWeight: 'bold', fontSize: '0.9rem' }}>{event.eventType}</div>
                                        <div style={{ fontSize: '0.85rem' }}>{event.description}</div>
                                        <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '0.25rem' }}>
                                            {event.triggeredByIp} • {new Date(event.timestamp).toLocaleString()}
                                        </div>
                                    </div>
                                ))
                            )}
                        </div>
                    </div>

                    <div className="card">
                        <h3>🔑 Access Control Status</h3>
                        <ul style={{ listStyle: 'none', padding: 0, marginTop: '1rem' }}>
                            <li style={{ display: 'flex', justifyContent: 'space-between', padding: '0.5rem 0', borderBottom: '1px solid var(--card-border)' }}>
                                <span>Attribute-Based Encryption</span>
                                <span style={{ color: '#00C851' }}>● Active</span>
                            </li>
                            <li style={{ display: 'flex', justifyContent: 'space-between', padding: '0.5rem 0', borderBottom: '1px solid var(--card-border)' }}>
                                <span>ECDH Key Exchange</span>
                                <span style={{ color: '#00C851' }}>● Active</span>
                            </li>
                            <li style={{ display: 'flex', justifyContent: 'space-between', padding: '0.5rem 0', borderBottom: '1px solid var(--card-border)' }}>
                                <span>Intrusion Prevention</span>
                                <span style={{ color: lockdownStatus.isLockdown ? '#ff4444' : '#00C851' }}>
                                    ● {lockdownStatus.isLockdown ? 'LOCKDOWN' : 'Monitoring'}
                                </span>
                            </li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default SecurityAudit;
