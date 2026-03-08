import React, { useState, useRef, useEffect } from 'react';
import axios from 'axios';
import { getAiUrl } from '../config';
import './Chatbot.css';

const Chatbot = () => {
    const [isOpen, setIsOpen] = useState(false);
    const [messages, setMessages] = useState([
        { role: 'assistant', text: 'Hello! I am your AI assistant. I can help you check doctor availability and answer general questions.' }
    ]);
    const [input, setInput] = useState('');
    const [loading, setLoading] = useState(false);
    const chatEndRef = useRef(null);

    const toggleChat = () => setIsOpen(!isOpen);

    const scrollToBottom = () => {
        chatEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    };

    useEffect(() => {
        scrollToBottom();
    }, [messages]);

    const handleSend = async (e) => {
        e.preventDefault();
        if (!input.trim()) return;

        const userMsg = { role: 'user', text: input };
        setMessages(prev => [...prev, userMsg]);
        setInput('');
        setLoading(true);

        try {
            const token = localStorage.getItem('token');
            const response = await axios.post(`${getAiUrl()}`,
                { prompt: userMsg.text },
                { headers: { Authorization: `Bearer ${token}` } }
            );

            const botMsg = { role: 'assistant', text: response.data.response || "No response received." };
            setMessages(prev => [...prev, botMsg]);
        } catch (error) {
            console.error("Chat error:", error);
            setMessages(prev => [...prev, { role: 'assistant', text: "Sorry, I am having trouble connecting to the system right now." }]);
        } finally {
            setLoading(false);
        }
    };

    return (
        <>
            {/* Floating button */}
            <button className="chatbot-toggle" onClick={toggleChat}>
                💬 AI Assistant
            </button>

            {/* Chat Window */}
            {isOpen && (
                <div className="chatbot-window card fade-in">
                    <div className="chatbot-header">
                        <h4>Medical AI Assistant</h4>
                        <button onClick={toggleChat} className="close-btn">×</button>
                    </div>

                    <div className="chatbot-messages">
                        {messages.map((msg, index) => (
                            <div key={index} className={`chat-message ${msg.role}`}>
                                <div className="message-bubble">{msg.text}</div>
                            </div>
                        ))}
                        {loading && (
                            <div className="chat-message assistant">
                                <div className="message-bubble loading">Thinking...</div>
                            </div>
                        )}
                        <div ref={chatEndRef} />
                    </div>

                    <form onSubmit={handleSend} className="chatbot-input">
                        <input
                            type="text"
                            value={input}
                            onChange={(e) => setInput(e.target.value)}
                            placeholder="Ask me something..."
                            disabled={loading}
                        />
                        <button type="submit" disabled={loading || !input.trim()}>Send</button>
                    </form>
                </div>
            )}

        </>
    );
};

export default Chatbot;
