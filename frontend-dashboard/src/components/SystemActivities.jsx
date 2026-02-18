import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { getBackendUrl } from '../config';

const SystemActivities = ({ theme }) => {
    const [logs, setLogs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [filter, setFilter] = useState('');

    useEffect(() => {
        fetchLogs();
        const interval = setInterval(fetchLogs, 10000); // 10s refresh
        return () => clearInterval(interval);
    }, []);

    const fetchLogs = async () => {
        try {
            const token = localStorage.getItem('token');
            const response = await axios.get(`${getBackendUrl()}/api/admin/logs`, {
                headers: { Authorization: `Bearer ${token}` }
            });
            setLogs(response.data);
            setLoading(false);
        } catch (error) {
            console.error("Failed to fetch system logs", error);
            setLoading(false);
        }
    };

    const filteredLogs = logs.filter(log =>
        (log.username?.toLowerCase() || '').includes(filter.toLowerCase()) ||
        (log.action?.toLowerCase() || '').includes(filter.toLowerCase()) ||
        (log.description?.toLowerCase() || '').includes(filter.toLowerCase())
    );

    const getStatusColor = (status) => {
        switch (status) {
            case 'SUCCESS': return '#00C851';
            case 'FAILURE': return '#ff4444';
            case 'INFO': return '#33b5e5';
            default: return 'inherit';
        }
    };

    return (
        <div className="system-activities card">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
                <h3>🕵️ System Activity Hub</h3>
                <input
                    type="text"
                    placeholder="Search logs (User, Action, Description)..."
                    value={filter}
                    onChange={(e) => setFilter(e.target.value)}
                    style={{
                        padding: '0.5rem 1rem',
                        borderRadius: '20px',
                        border: '1px solid var(--input-border)',
                        width: '300px',
                        background: 'rgba(255,255,255,0.05)',
                        color: 'var(--text-primary)'
                    }}
                />
            </div>

            {loading ? (
                <p>Loading activities...</p>
            ) : (
                <div style={{ overflowX: 'auto' }}>
                    <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                        <thead>
                            <tr style={{ borderBottom: '2px solid var(--card-border)', color: 'var(--text-secondary)' }}>
                                <th style={{ padding: '0.75rem' }}>Timestamp</th>
                                <th style={{ padding: '0.75rem' }}>User</th>
                                <th style={{ padding: '0.75rem' }}>Action</th>
                                <th style={{ padding: '0.75rem' }}>Status</th>
                                <th style={{ padding: '0.75rem' }}>Description</th>
                            </tr>
                        </thead>
                        <tbody>
                            {filteredLogs.map(log => (
                                <tr key={log.id} style={{ borderBottom: '1px solid var(--card-border)', transition: 'background 0.3s' }} className="log-row">
                                    <td style={{ padding: '0.75rem', fontSize: '0.85rem' }}>
                                        {new Date(log.timestamp).toLocaleString()}
                                    </td>
                                    <td style={{ padding: '0.75rem', fontWeight: 'bold' }}>{log.username}</td>
                                    <td style={{ padding: '0.75rem' }}>
                                        <span style={{
                                            padding: '0.2rem 0.5rem',
                                            borderRadius: '4px',
                                            background: 'rgba(56, 189, 248, 0.1)',
                                            color: 'var(--accent-color)',
                                            fontSize: '0.75rem',
                                            fontWeight: 'bold'
                                        }}>
                                            {log.action}
                                        </span>
                                    </td>
                                    <td style={{ padding: '0.75rem' }}>
                                        <span style={{
                                            color: getStatusColor(log.status),
                                            fontSize: '0.8rem',
                                            fontWeight: 'bold'
                                        }}>
                                            ● {log.status}
                                        </span>
                                    </td>
                                    <td style={{ padding: '0.75rem', fontSize: '0.9rem', color: 'var(--text-secondary)' }}>
                                        {log.description}
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                    {filteredLogs.length === 0 && (
                        <p style={{ textAlign: 'center', padding: '2rem', color: 'var(--text-secondary)' }}>
                            No logs found matching your criteria.
                        </p>
                    )}
                </div>
            )}

            <style>{`
                .log-row:hover {
                    background: rgba(255,255,255,0.02);
                }
            `}</style>
        </div>
    );
};

export default SystemActivities;
