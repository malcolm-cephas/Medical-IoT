export const getBackendUrl = () => {
    const hostname = window.location.hostname;
    // If we're on localhost, assume backend is on 8080. 
    // If we're on a network IP, use that same IP with 8080.
    return `http://${hostname}:8080`;
};

export const getAnalyticsUrl = () => {
    const hostname = window.location.hostname;
    // Python service is usually on 4242 or 8000. 
    // Based on the code, it seems to expect 4242 in many places.
    return `http://${hostname}:4242`;
};
