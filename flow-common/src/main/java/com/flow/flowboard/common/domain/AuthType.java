package com.flow.flowboard.common.domain;

/**
 * Enum representing different types of authentication methods for API credentials.
 * Used to determine how to authenticate requests to external APIs.
 */
public enum AuthType {
    /**
     * No authentication required
     */
    NONE,

    /**
     * API Key authentication (typically sent as header or query parameter)
     * Common headers: X-API-Key, Api-Key
     */
    API_KEY,

    /**
     * Bearer token authentication (OAuth 2.0, JWT)
     * Sent in Authorization header as: "Bearer {token}"
     */
    BEARER_TOKEN,

    /**
     * HTTP Basic authentication
     * Credentials sent as Base64 encoded username:password in Authorization header
     */
    BASIC_AUTH,

    /**
     * OAuth 2.0 client credentials flow
     * Requires client_id, client_secret, token_url, and optional scope
     */
    OAUTH2
}
