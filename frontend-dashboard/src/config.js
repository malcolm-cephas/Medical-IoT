export const getBackendUrl = () => {
    const hostname = window.location.hostname;
    const port = import.meta.env.VITE_BACKEND_PORT || '8080';
    return `http://${hostname}:${port}`;
};

export const getAnalyticsUrl = () => {
    const hostname = window.location.hostname;
    const port = import.meta.env.VITE_ANALYTICS_PORT || '4242';
    return `http://${hostname}:${port}`;
};
