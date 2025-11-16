package com.flow.flowboard.common.domain;

/**
 * Authentication types supported for API calls
 */
public enum ApiAuthType {
    /**
     * No authentication required
     */
    NONE,

    /**
     * Basic Authentication (username + password)
     * Sends credentials as Base64-encoded "username:password" in Authorization header
     */
    BASIC,

    /**
     * Bearer Token Authentication
     * Sends token in Authorization header: "Bearer {token}"
     */
    BEARER,

    /**
     * API Key Authentication
     * Sends API key in a custom header or query parameter
     */
    API_KEY,

    /**
     * OAuth 2.0 Authentication
     * Uses client credentials flow to obtain access token
     */
    OAUTH2,

    /**
     * Custom Authentication
     * User-defined authentication logic with custom headers/parameters
     */
    CUSTOM
}
