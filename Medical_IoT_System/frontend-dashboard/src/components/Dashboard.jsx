import React, { useEffect, useState } from 'react';
import axios from 'axios';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
} from 'chart.js';
import { Line } from 'react-chartjs-2';

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend
);

const Dashboard = () => {
  const [patientId, setPatientId] = useState('123'); // Default Patient
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);

  // Poll for data every 2 seconds
  useEffect(() => {
    const fetchData = async () => {
      try {
        const response = await axios.get(`http://localhost:8080/api/sensor/history/${patientId}`);
        setHistory(response.data);
        setLoading(false);
      } catch (error) {
        console.error("Error fetching data", error);
      }
    };

    fetchData(); // Initial call
    const interval = setInterval(fetchData, 2000); // Polling
    return () => clearInterval(interval);
  }, [patientId]);

  // Chart Configuration
  const chartData = {
    labels: history.map((_, index) => index + 1), // Simple counter for x-axis
    datasets: [
      {
        label: 'Heart Rate (BPM)',
        data: history.map(d => d.heartRate),
        borderColor: 'rgb(255, 99, 132)',
        backgroundColor: 'rgba(255, 99, 132, 0.5)',
      },
      {
        label: 'SpO2 (%)',
        data: history.map(d => d.spo2),
        borderColor: 'rgb(53, 162, 235)',
        backgroundColor: 'rgba(53, 162, 235, 0.5)',
      },
    ],
  };

  const options = {
    responsive: true,
    plugins: {
      legend: { position: 'top' },
      title: { display: true, text: `Real-time Vitals for Patient: ${patientId}` },
    },
  };

  return (
    <div style={{ padding: '20px', fontFamily: 'Arial' }}>
      <h1>Medical IoT Monitor - Doctor Dashboard</h1>
      
      <div style={{ marginBottom: '20px' }}>
        <label>Select Patient ID: </label>
        <input 
          type="text" 
          value={patientId} 
          onChange={(e) => setPatientId(e.target.value)} 
          style={{ padding: '5px' }}
        />
      </div>

      <div style={{ display: 'flex', gap: '20px' }}>
        <div style={{ flex: 2, background: '#fff', padding: '20px', borderRadius: '8px', boxShadow: '0 0 10px rgba(0,0,0,0.1)' }}>
          {loading ? <p>Loading...</p> : <Line options={options} data={chartData} />}
        </div>
        
        <div style={{ flex: 1, background: '#f9f9f9', padding: '20px', borderRadius: '8px', height: '400px', overflowY: 'scroll' }}>
            <h3>Risk Alert Log</h3>
            <ul>
                {history.slice().reverse().map((record, i) => (
                    <li key={i} style={{ 
                        marginBottom: '10px', 
                        color: (record.heartRate > 100 || record.spo2 < 95) ? 'red' : 'green' 
                    }}>
                        <strong>Risk: {(record.heartRate > 100 || record.spo2 < 95) ? 'HIGH' : 'NORMAL'}</strong><br/>
                        HR: {record.heartRate} | SpO2: {record.spo2}%<br/>
                        <small>{new Date().toLocaleTimeString()}</small> 
                    </li>
                ))}
            </ul>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
