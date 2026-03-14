const axios = require('axios');
const EventSource = require('eventsource');
const fs = require('fs');
const path = require('path');
const https = require('https');

// GLOBALLY BYPASS SSL CHECKS
process.env.NODE_TLS_REJECT_UNAUTHORIZED = '0';

const LOG_FILE = path.join(__dirname, 'mcp_bridge.log');
const BASE_URL = 'http://localhost:9090'; // AI Server Port
const AUTH_URL = 'http://localhost:9000/oauth2/token'; // Auth Server Token endpoint
const SSE_URL = `${BASE_URL}/sse`;

const agent = new https.Agent({ rejectUnauthorized: false, keepAlive: true });

function log(msg) {
    fs.appendFileSync(LOG_FILE, `[${new Date().toISOString()}] ${msg}\n`);
}

log(`--- Medical MCP Bridge Starting ---`);

let messageEndpoint = null;
let requestBuffer = [];
let eventSource = null;
let authToken = null;

async function fetchToken() {
    log('Fetching OAuth2 token from Auth Server...');
    try {
        const response = await axios.post(AUTH_URL, 'grant_type=client_credentials&scope=openid', {
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'Authorization': 'Basic ' + Buffer.from('mcp-client:secret').toString('base64')
            },
            httpsAgent: agent
        });
        authToken = response.data.access_token;
        log('OAuth2 token fetched successfully.');
        return true;
    } catch (err) {
        log(`!!! Failed to fetch OAuth2 token: ${err.message}`);
        return false;
    }
}

function connect() {
    log(`Connecting to SSE: ${SSE_URL}`);
    if (eventSource) eventSource.close();

    eventSource = new EventSource(SSE_URL, {
        headers: {
            'Authorization': `Bearer ${authToken}`,
            'Cache-Control': 'no-cache',
            'X-Accel-Buffering': 'no'
        }
    });

    eventSource.onopen = () => log('Stream established.');

    eventSource.onmessage = (event) => {
        if (!event.data) return;
        const rawBody = event.data.trim();
        if (!rawBody) return;

        try {
            const message = JSON.parse(rawBody);
            if (message.jsonrpc) {
                const finalOutput = JSON.stringify(message) + '\n';
                process.stdout.write(finalOutput);
                log(`<<< [RECV] ID: ${message.id}`);
            }
        } catch (e) {
            log(`!!! [PARSE ERROR] ${e.message}`);
        }
    };

    eventSource.addEventListener('endpoint', (event) => {
        messageEndpoint = `${BASE_URL}${event.data.trim()}`;
        log(`### Session Ready: ${messageEndpoint}`);
        while (requestBuffer.length > 0) send(requestBuffer.shift());
    });

    eventSource.onerror = (err) => {
        log(`!!! SSE Error: ${err.message || 'Signal lost'}`);
        if (err.status === 401) {
            log('Unauthorized. Exiting.');
            process.exit(1);
        }
    };
}

async function send(req) {
    if (!messageEndpoint || !authToken) {
        requestBuffer.push(req);
        return;
    }
    try {
        log(`>>> [SEND] ${req.method} (${req.id})`);
        await axios.post(messageEndpoint, req, {
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${authToken}`
            },
            httpsAgent: agent
        });
    } catch (err) {
        log(`!!! POST FAIL: ${err.message}`);
    }
}

process.stdin.on('data', (d) => {
    d.toString().split('\n').forEach(line => {
        const trimmed = line.trim();
        if (!trimmed) return;
        try {
            send(JSON.parse(trimmed));
        } catch (e) { }
    });
});

async function start() {
    if (await fetchToken()) {
        connect();
        process.stdin.resume();
        log('Medical Bridge ready.');
    } else {
        log('Auth failure. Exit.');
        process.exit(1);
    }
}

start();
