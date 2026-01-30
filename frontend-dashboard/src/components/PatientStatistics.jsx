import React, { useState, useEffect } from 'react';
import axios from 'axios';

const PatientStatistics = ({ theme }) => {
    const [stats, setStats] = useState({
        totalPatients: 0,
        criticalCount: 0,
        stableCount: 0,
        avgHeartRate: 0,
        avgSpo2: 0,
        avgTemperature: 0,
        avgHumidity: 0,
        highBPCount: 0,
        lowBPCount: 0,
        feverCount: 0
    });
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchStatistics();
        const interval = setInterval(fetchStatistics, 10000); // Update every 10 seconds
        return () => clearInterval(interval);
    }, []);

    const fetchStatistics = async () => {
        try {
            const response = await axios.get('http://localhost:8080/api/patients?page=0&size=100');
            const patients = response.data.patients;

            let critical = 0;
            let stable = 0;
            let totalHR = 0;
            let totalSpo2 = 0;
            let totalTemp = 0;
            let totalHum = 0;
            let highBP = 0;
            let lowBP = 0;
            let fever = 0;

            patients.forEach(patient => {
                // Critical condition check
                if (patient.latestHeartRate > 100 || patient.latestSpo2 < 95) {
                    critical++;
                } else {
                    stable++;
                }

                // Aggregations
                totalHR += patient.latestHeartRate || 0;
                totalSpo2 += patient.latestSpo2 || 0;
                totalTemp += patient.latestTemperature || 0;
                totalHum += patient.latestHumidity || 0;

                // BP checks
                if (patient.latestSystolicBP > 140 || patient.latestDiastolicBP > 90) highBP++;
                if (patient.latestSystolicBP < 90 || patient.latestDiastolicBP < 60) lowBP++;

                // Fever check
                if (patient.latestTemperature > 37.5) fever++;
            });

            const count = patients.length || 1;
            setStats({
                totalPatients: patients.length,
                criticalCount: critical,
                stableCount: stable,
                avgHeartRate: Math.round(totalHR / count),
                avgSpo2: Math.round(totalSpo2 / count),
                avgTemperature: (totalTemp / count).toFixed(1),
                avgHumidity: (totalHum / count).toFixed(1),
                highBPCount: highBP,
                lowBPCount: lowBP,
                feverCount: fever
            });
            setLoading(false);
        } catch (error) {
            console.error("Error fetching statistics", error);
            setLoading(false);
        }
    };

    if (loading) {
        return <div style={{ padding: '2rem', textAlign: 'center' }}>Loading statistics...</div>;
    }

    return (
        <div className="card" style={{ marginBottom: '2rem' }}>
            <div className="card-header">
                <h2>📊 Ward Statistics Overview</h2>
                <p style={{ fontSize: '0.9rem', color: 'var(--text-secondary)', marginTop: '0.5rem' }}>
                    Real-time aggregated patient data
                </p>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem' }}>
                {/* Total Patients */}
                <div style={{
                    padding: '1.5rem',
                    background: theme === 'dark' ? 'rgba(56, 189, 248, 0.1)' : 'rgba(56, 189, 248, 0.05)',
                    border: '2px solid #38bdf8',
                    borderRadius: '8px',
                    textAlign: 'center'
                }}>
                    <div style={{ fontSize: '2.5rem', fontWeight: 'bold', color: '#38bdf8' }}>
                        {stats.totalPatients}
                    </div>
                    <div style={{ fontSize: '0.9rem', color: 'var(--text-secondary)', marginTop: '0.5rem' }}>
                        Total Patients
                    </div>
                </div>

                {/* Critical */}
                <div style={{
                    padding: '1.5rem',
                    background: theme === 'dark' ? 'rgba(248, 113, 113, 0.1)' : 'rgba(248, 113, 113, 0.05)',
                    border: '2px solid var(--danger-color)',
                    borderRadius: '8px',
                    textAlign: 'center'
                }}>
                    <div style={{ fontSize: '2.5rem', fontWeight: 'bold', color: 'var(--danger-color)' }}>
                        {stats.criticalCount}
                    </div>
                    <div style={{ fontSize: '0.9rem', color: 'var(--text-secondary)', marginTop: '0.5rem' }}>
                        Critical Condition
                    </div>
                </div>

                {/* Stable */}
                <div style={{
                    padding: '1.5rem',
                    background: theme === 'dark' ? 'rgba(74, 222, 128, 0.1)' : 'rgba(74, 222, 128, 0.05)',
                    border: '2px solid var(--success-color)',
                    borderRadius: '8px',
                    textAlign: 'center'
                }}>
                    <div style={{ fontSize: '2.5rem', fontWeight: 'bold', color: 'var(--success-color)' }}>
                        {stats.stableCount}
                    </div>
                    <div style={{ fontSize: '0.9rem', color: 'var(--text-secondary)', marginTop: '0.5rem' }}>
                        Stable Patients
                    </div>
                </div>

                {/* Fever Count */}
                <div style={{
                    padding: '1.5rem',
                    background: theme === 'dark' ? 'rgba(251, 146, 60, 0.1)' : 'rgba(251, 146, 60, 0.05)',
                    border: '2px solid #fb923c',
                    borderRadius: '8px',
                    textAlign: 'center'
                }}>
                    <div style={{ fontSize: '2.5rem', fontWeight: 'bold', color: '#fb923c' }}>
                        {stats.feverCount}
                    </div>
                    <div style={{ fontSize: '0.9rem', color: 'var(--text-secondary)', marginTop: '0.5rem' }}>
                        Fever Cases
                    </div>
                </div>
            </div>

            {/* Average Vitals */}
            <div style={{ marginTop: '2rem', padding: '1rem', background: theme === 'dark' ? 'rgba(0,0,0,0.2)' : 'rgba(0,0,0,0.05)', borderRadius: '8px' }}>
                <h3 style={{ fontSize: '1.1rem', marginBottom: '1rem' }}>📈 Average Vitals</h3>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))', gap: '1rem' }}>
                    <div>
                        <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Heart Rate</div>
                        <div style={{ fontSize: '1.5rem', fontWeight: 'bold' }}>{stats.avgHeartRate} BPM</div>
                    </div>
                    <div>
                        <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>SpO2</div>
                        <div style={{ fontSize: '1.5rem', fontWeight: 'bold' }}>{stats.avgSpo2}%</div>
                    </div>
                    <div>
                        <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Temperature</div>
                        <div style={{ fontSize: '1.5rem', fontWeight: 'bold' }}>{stats.avgTemperature}°C</div>
                    </div>
                    <div>
                        <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Humidity</div>
                        <div style={{ fontSize: '1.5rem', fontWeight: 'bold' }}>{stats.avgHumidity}%</div>
                    </div>
                </div>
            </div>

            {/* BP Alerts */}
            <div style={{ marginTop: '1rem', display: 'flex', gap: '1rem' }}>
                <div style={{ flex: 1, padding: '0.75rem', background: theme === 'dark' ? 'rgba(248, 113, 113, 0.1)' : 'rgba(248, 113, 113, 0.05)', borderRadius: '4px', border: '1px solid var(--danger-color)' }}>
                    <strong style={{ color: 'var(--danger-color)' }}>{stats.highBPCount}</strong> patients with high BP
                </div>
                <div style={{ flex: 1, padding: '0.75rem', background: theme === 'dark' ? 'rgba(251, 146, 60, 0.1)' : 'rgba(251, 146, 60, 0.05)', borderRadius: '4px', border: '1px solid #fb923c' }}>
                    <strong style={{ color: '#fb923c' }}>{stats.lowBPCount}</strong> patients with low BP
                </div>
            </div>
        </div>
    );
};

export default PatientStatistics;
