# API Endpoint Execution - Usage Guide

This guide demonstrates how to execute configured API endpoints using the `/api/v1/endpoints/{id}/execute` endpoint.

## Endpoint

```
POST /api/v1/endpoints/{id}/execute
```

## Request Body Schema

```json
{
  "pathParams": {
    "paramName": "value"
  },
  "queryParams": {
    "key": "value"
  },
  "headers": {
    "Header-Name": "value"
  },
  "requestBody": {
    // Any JSON object or primitive
  },
  "overrideAuthentication": {
    "type": "BASIC|BEARER|API_KEY|OAUTH2|CUSTOM|NONE",
    // Type-specific fields
  },
  "timeoutMs": 30000
}
```

All fields are optional. If not provided, defaults from the endpoint/API configuration will be used.

## Response Schema

```json
{
  "executionId": "uuid",
  "status": "SUCCESS|FAILED|ERROR",
  "httpStatusCode": 200,
  "httpStatusText": "OK",
  "requestTimestamp": "2025-11-16T20:00:00Z",
  "responseTimestamp": "2025-11-16T20:00:01Z",
  "durationMs": 1234,
  "requestUrl": "https://api.example.com/users/123",
  "httpMethod": "GET",
  "responseHeaders": {
    "Content-Type": "application/json",
    "X-RateLimit-Remaining": "99"
  },
  "responseBody": {
    // Parsed JSON or raw string
  },
  "errorMessage": "Error details if status is ERROR"
}
```

## Example 1: Simple GET Request

Execute a GET endpoint with no parameters:

```bash
curl -X POST http://localhost:8080/api/v1/endpoints/{endpoint-id}/execute \
  -H "Content-Type: application/json"
```

Or with empty body:

```bash
curl -X POST http://localhost:8080/api/v1/endpoints/{endpoint-id}/execute \
  -H "Content-Type: application/json" \
  -d '{}'
```

## Example 2: GET with Path Parameters

For an endpoint configured as: `GET /users/{{userId}}/posts/{{postId}}`

```bash
curl -X POST http://localhost:8080/api/v1/endpoints/{endpoint-id}/execute \
  -H "Content-Type: application/json" \
  -d '{
    "pathParams": {
      "userId": "123",
      "postId": "456"
    }
  }'
```

This will execute: `GET /users/123/posts/456`

## Example 3: GET with Query Parameters

```bash
curl -X POST http://localhost:8080/api/v1/endpoints/{endpoint-id}/execute \
  -H "Content-Type: application/json" \
  -d '{
    "queryParams": {
      "page": "1",
      "limit": "10",
      "sort": "createdAt"
    }
  }'
```

This will execute: `GET /resource?page=1&limit=10&sort=createdAt`

## Example 4: POST with Request Body

```bash
curl -X POST http://localhost:8080/api/v1/endpoints/{endpoint-id}/execute \
  -H "Content-Type: application/json" \
  -d '{
    "requestBody": {
      "name": "John Doe",
      "email": "john@example.com",
      "age": 30
    }
  }'
```

## Example 5: Custom Headers

```bash
curl -X POST http://localhost:8080/api/v1/endpoints/{endpoint-id}/execute \
  -H "Content-Type: application/json" \
  -d '{
    "headers": {
      "X-Custom-Header": "custom-value",
      "Accept-Language": "en-US"
    }
  }'
```

## Example 6: Override Authentication - Bearer Token

Override the configured authentication with a different bearer token:

```bash
curl -X POST http://localhost:8080/api/v1/endpoints/{endpoint-id}/execute \
  -H "Content-Type: application/json" \
  -d '{
    "overrideAuthentication": {
      "type": "BEARER",
      "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    }
  }'
```

## Example 7: Override Authentication - Basic Auth

```bash
curl -X POST http://localhost:8080/api/v1/endpoints/{endpoint-id}/execute \
  -H "Content-Type: application/json" \
  -d '{
    "overrideAuthentication": {
      "type": "BASIC",
      "username": "testuser",
      "password": "testpass"
    }
  }'
```

## Example 8: Override Authentication - API Key (Header)

```bash
curl -X POST http://localhost:8080/api/v1/endpoints/{endpoint-id}/execute \
  -H "Content-Type: application/json" \
  -d '{
    "overrideAuthentication": {
      "type": "API_KEY",
      "location": "header",
      "keyName": "X-API-Key",
      "keyValue": "your-api-key-here"
    }
  }'
```

## Example 9: Override Authentication - API Key (Query)

```bash
curl -X POST http://localhost:8080/api/v1/endpoints/{endpoint-id}/execute \
  -H "Content-Type: application/json" \
  -d '{
    "overrideAuthentication": {
      "type": "API_KEY",
      "location": "query",
      "keyName": "api_key",
      "keyValue": "your-api-key-here"
    }
  }'
```

## Example 10: Custom Timeout

Override the default timeout with a custom value (in milliseconds):

```bash
curl -X POST http://localhost:8080/api/v1/endpoints/{endpoint-id}/execute \
  -H "Content-Type: application/json" \
  -d '{
    "timeoutMs": 60000
  }'
```

## Example 11: Complete Request (All Parameters)

```bash
curl -X POST http://localhost:8080/api/v1/endpoints/{endpoint-id}/execute \
  -H "Content-Type: application/json" \
  -d '{
    "pathParams": {
      "userId": "123"
    },
    "queryParams": {
      "include": "profile,posts"
    },
    "headers": {
      "X-Request-ID": "req-123",
      "Accept": "application/json"
    },
    "requestBody": {
      "action": "update",
      "data": {
        "status": "active"
      }
    },
    "overrideAuthentication": {
      "type": "BEARER",
      "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    },
    "timeoutMs": 30000
  }'
```

## Example Response (Success)

```json
{
  "status": "SUCCESS",
  "httpStatusCode": 200,
  "httpStatusText": "OK",
  "requestTimestamp": "2025-11-16T20:15:30.123Z",
  "responseTimestamp": "2025-11-16T20:15:31.456Z",
  "durationMs": 1333,
  "requestUrl": "https://api.example.com/users/123?include=profile,posts",
  "httpMethod": "POST",
  "responseHeaders": {
    "Content-Type": "application/json; charset=utf-8",
    "X-RateLimit-Limit": "100",
    "X-RateLimit-Remaining": "99",
    "X-RateLimit-Reset": "1700162400"
  },
  "responseBody": {
    "id": "123",
    "name": "John Doe",
    "email": "john@example.com",
    "status": "active",
    "profile": {
      "avatar": "https://example.com/avatar.jpg",
      "bio": "Software Developer"
    }
  }
}
```

## Example Response (HTTP Error - 404)

```json
{
  "status": "FAILED",
  "httpStatusCode": 404,
  "httpStatusText": "Not Found",
  "requestTimestamp": "2025-11-16T20:15:30.123Z",
  "responseTimestamp": "2025-11-16T20:15:30.456Z",
  "durationMs": 333,
  "requestUrl": "https://api.example.com/users/999",
  "httpMethod": "GET",
  "responseHeaders": {
    "Content-Type": "application/json"
  },
  "responseBody": {
    "error": "User not found",
    "code": "USER_NOT_FOUND"
  }
}
```

## Example Response (Network/Timeout Error)

```json
{
  "status": "ERROR",
  "requestTimestamp": "2025-11-16T20:15:30.123Z",
  "responseTimestamp": "2025-11-16T20:16:00.123Z",
  "durationMs": 30000,
  "requestUrl": "https://api.example.com/users",
  "httpMethod": "GET",
  "errorMessage": "java.util.concurrent.TimeoutException: Did not observe any item or terminal signal within 30000ms"
}
```

## Features

### Automatic Retry
If the endpoint or API has retry configuration, the executor will automatically retry on:
- 408 (Request Timeout)
- 429 (Too Many Requests)
- 5xx (Server Errors)
- Network errors (connection timeout, etc.)

Retry can be configured with:
- Exponential backoff or fixed delay
- Maximum retry attempts
- Custom retryable status codes

### URL Encoding
All path parameters and query parameters are automatically URL-encoded for security.

### Authentication
Supports multiple authentication types:
- **NONE**: No authentication
- **BASIC**: HTTP Basic Authentication
- **BEARER**: Bearer token (Authorization: Bearer xxx)
- **API_KEY**: API key in header or query parameter
- **OAUTH2**: OAuth 2.0 (client credentials, authorization code)
- **CUSTOM**: Custom headers and query parameters

### Timeout Precedence
Timeout is determined in this order:
1. Request timeout (in execute request)
2. Endpoint timeout (in endpoint configuration)
3. API timeout (in API configuration)
4. Default: 30 seconds

### Response Parsing
- JSON responses are automatically parsed into objects
- Non-JSON responses are returned as strings
- Response headers are captured and returned

### Error Handling
- Network errors, timeouts, and HTTP errors are all captured
- Detailed error messages in the response
- Duration tracking even for failed requests

## Testing Workflow

1. **Create an API** with base URL and authentication
2. **Create an endpoint** under that API with path, method, etc.
3. **Execute the endpoint** using this endpoint with test data
4. **Review the response** to verify behavior
5. **Adjust retry/timeout** configuration as needed

This allows you to test external APIs without writing any code!
