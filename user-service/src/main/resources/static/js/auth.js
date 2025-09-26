const TOKEN_KEY = 'jwtTokens';
const USER_KEY = 'currentUser';

function saveToken(tokens) {
    localStorage.setItem(TOKEN_KEY, JSON.stringify(tokens));
}

function saveUser(user) {
    if (user) {
        localStorage.setItem(USER_KEY, JSON.stringify(user));
    }
}

function getToken() {
    const tokensStr = localStorage.getItem(TOKEN_KEY);
    return tokensStr ? JSON.parse(tokensStr) : null;
}

function getCurrentUser() {
    const userStr = localStorage.getItem(USER_KEY);
    return userStr ? JSON.parse(userStr) : null;
}

function logout() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    window.location.href = '/login';
}

function isLoggedIn() {
    const tokens = getToken();
    return !!tokens && !!tokens.accessToken;
}

async function refreshAccessToken(refreshToken) {
    try {
        const response = await fetch('/api/auth/refresh', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ refreshToken: refreshToken })
        });

        if (!response.ok) {
            throw new Error('Failed to refresh token.');
        }

        const newTokens = await response.json();
        saveToken(newTokens);
        return newTokens;
    } catch (error) {
        console.error("Refresh token failed, logging out.", error);
        logout();
        throw error;
    }
}

async function fetchWithAuth(url, options = {}) {
    let tokens = getToken();
    if (!tokens) {
        logout();
        return Promise.reject(new Error('No token found, redirecting to login.'));
    }

    let response = await performFetch(url, options, tokens.accessToken);

    if (response.status === 401) {
        console.log('Access token expired or invalid, attempting to refresh...');
        try {
            const newTokens = await refreshAccessToken(tokens.refreshToken);
            console.log('Token refreshed successfully. Retrying original request...');
            response = await performFetch(url, options, newTokens.accessToken);
        } catch (error) {
            return Promise.reject(error);
        }
    }

    return response;
}

async function logout() {
    const tokens = getToken();
    if (tokens && tokens.accessToken) {
        try {
            await performFetch('/auth/logout', { method: 'POST' }, tokens.accessToken);
        } catch (error) {
            console.error("Logout API call failed, but clearing client-side session anyway.", error);
        }
    }

    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    window.location.href = '/login';
}

function performFetch(url, options, accessToken) {
    const headers = { ...options.headers };

    if (accessToken) {
        headers['Authorization'] = `Bearer ${accessToken}`;
    }

    if (!(options.body instanceof FormData)) {
        headers['Content-Type'] = 'application/json';
    }

    return fetch(`/api${url}`, { ...options, headers });
}

function parseJwt(token) {
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join(''));

        return JSON.parse(jsonPayload);
    } catch (e) {
        console.error("Failed to parse JWT", e);
        return null;
    }
}

function getPermissions() {
    const tokens = getToken();
    if (!tokens || !tokens.accessToken) {
        return new Set();
    }
    try {
        const payloadBase64 = tokens.accessToken.split('.')[1];
        const decodedPayload = atob(payloadBase64.replace(/-/g, '+').replace(/_/g, '/'));
        const payload = JSON.parse(decodedPayload);
        return new Set(payload.authorities || []);
    } catch (e) {
        console.error("Error decoding JWT payload:", e);
        return new Set();
    }
}