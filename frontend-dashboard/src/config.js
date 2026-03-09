export const getBackendUrl = () => {
    const hostname = window.location.hostname;
    const port = import.meta.env.VITE_BACKEND_PORT || '8080';
    return `http://${hostname}:${port}`;
};

export const getAuthUrl = () => {
    const hostname = window.location.hostname;
    const port = import.meta.env.VITE_AUTH_PORT || '8080';
    return `http://${hostname}:${port}`;
};

export const getAnalyticsUrl = () => {
    const hostname = window.location.hostname;
    const port = import.meta.env.VITE_ANALYTICS_PORT || '4242';
    return `http://${hostname}:${port}`;
};

export const getAiUrl = () => {
    const hostname = window.location.hostname;
    const port = import.meta.env.VITE_AI_PORT || '8083';
    return `http://${hostname}:${port}/api/chat`;
};

