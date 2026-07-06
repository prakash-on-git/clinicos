# ClinicOS – API Documentation

**Base URL:** `http://localhost:8080`
**Content-Type:** `application/json` (all requests and responses)
**Auth header:** `Authorization: Bearer <access_token>`

---

## Changelog

| Date       | Endpoint                                    | Change                     |
|------------|---------------------------------------------|----------------------------|
| 2026-07-04 | POST /api/v1/auth/register                  | Added – Phase 2            |
| 2026-07-04 | POST /api/v1/auth/login                     | Added – Phase 2            |
| 2026-07-04 | POST /api/v1/auth/refresh                   | Added – Phase 2            |
| 2026-07-04 | POST /api/v1/auth/logout                    | Added – Phase 2            |
| 2026-07-04 | POST /api/v1/auth/forgot-password           | Added – Phase 2            |
| 2026-07-04 | POST /api/v1/auth/reset-password            | Added – Phase 2            |
| 2026-07-04 | POST /api/v1/auth/verify-email              | Added – Phase 2            |
| 2026-07-04 | POST /api/v1/clinics                        | Added – Phase 3            |
| 2026-07-04 | GET /api/v1/clinics/mine                    | Added – Phase 3            |
| 2026-07-04 | GET /api/v1/clinics                         | Added – Phase 3            |
| 2026-07-04 | GET /api/v1/clinics/{id}                    | Added – Phase 3            |
| 2026-07-04 | PUT /api/v1/clinics/{id}                    | Added – Phase 3            |
| 2026-07-04 | DELETE /api/v1/clinics/{id}                 | Added – Phase 3            |
| 2026-07-04 | POST /api/v1/clinics/{id}/emergency-close   | Added – Phase 3            |
| 2026-07-04 | POST /api/v1/clinics/{id}/emergency-reopen  | Added – Phase 3            |
| 2026-07-04 | GET /api/v1/clinics/{id}/hours              | Added – Phase 3            |
| 2026-07-04 | PUT /api/v1/clinics/{id}/hours              | Added – Phase 3            |
| 2026-07-04 | PUT /api/v1/clinics/{id}/hours/{day}        | Added – Phase 3            |
| 2026-07-04 | DELETE /api/v1/clinics/{id}/hours/{day}     | Added – Phase 3            |
| 2026-07-04 | GET /api/v1/clinics/{id}/closures           | Added – Phase 3            |
| 2026-07-04 | POST /api/v1/clinics/{id}/closures          | Added – Phase 3            |
| 2026-07-04 | DELETE /api/v1/clinics/{id}/closures/{cid}  | Added – Phase 3            |
| 2026-07-04 | GET /api/v1/clinics/{id}/settings           | Added – Phase 3            |
| 2026-07-04 | PUT /api/v1/clinics/{id}/settings           | Added – Phase 3            |
| 2026-07-05 | POST /api/v1/clinics/{cid}/doctors          | Added – Phase 4            |
| 2026-07-05 | GET /api/v1/clinics/{cid}/doctors           | Added – Phase 4            |
| 2026-07-05 | GET /api/v1/clinics/{cid}/doctors/{id}      | Added – Phase 4            |
| 2026-07-05 | PUT /api/v1/clinics/{cid}/doctors/{id}      | Added – Phase 4            |
| 2026-07-05 | DELETE /api/v1/clinics/{cid}/doctors/{id}   | Added – Phase 4            |
| 2026-07-05 | PUT .../doctors/{id}/link-user/{uid}        | Added – Phase 4            |
| 2026-07-05 | DELETE .../doctors/{id}/link-user           | Added – Phase 4            |
| 2026-07-05 | GET/PUT/DELETE .../doctors/{id}/schedule    | Added – Phase 4            |
| 2026-07-05 | PUT/DELETE .../doctors/{id}/schedule/{day}  | Added – Phase 4            |
| 2026-07-05 | GET/PUT/DELETE .../doctors/{id}/breaks/{d}  | Added – Phase 4            |
| 2026-07-05 | GET/PUT .../doctors/{id}/overrides          | Added – Phase 4            |
| 2026-07-05 | DELETE .../doctors/{id}/overrides/{date}    | Added – Phase 4            |
| 2026-07-05 | GET/POST/DELETE .../doctors/{id}/leave      | Added – Phase 4            |
| 2026-07-05 | GET .../doctors/{id}/availability           | Added – Phase 4            |
| 2026-07-05 | POST/GET/PUT/DELETE .../treatments          | Added – Phase 4            |
| 2026-07-05 | GET/PUT/DELETE .../doctors/{id}/treatments  | Added – Phase 4            |
| 2026-07-05 | POST /api/v1/clinics/{cid}/patients         | Added – Phase 5            |
| 2026-07-05 | GET /api/v1/clinics/{cid}/patients          | Added – Phase 5            |
| 2026-07-05 | GET /api/v1/clinics/{cid}/patients/{id}     | Added – Phase 5            |
| 2026-07-05 | PUT /api/v1/clinics/{cid}/patients/{id}     | Added – Phase 5            |
| 2026-07-05 | DELETE /api/v1/clinics/{cid}/patients/{id}  | Added – Phase 5            |
| 2026-07-05 | PUT .../patients/{id}/link-user/{uid}       | Added – Phase 5            |
| 2026-07-05 | DELETE .../patients/{id}/link-user          | Added – Phase 5            |
| 2026-07-05 | GET /api/v1/patients/me                     | Added – Phase 5            |
| 2026-07-05 | POST /api/v1/clinics/{cid}/appointments     | Added – Phase 6            |
| 2026-07-05 | GET /api/v1/clinics/{cid}/appointments      | Added – Phase 6            |
| 2026-07-05 | GET /api/v1/clinics/{cid}/appointments/{id} | Added – Phase 6            |
| 2026-07-05 | PATCH .../appointments/{id}/status          | Added – Phase 6            |
| 2026-07-05 | PATCH .../appointments/{id}/cancel          | Added – Phase 6            |
| 2026-07-05 | PATCH .../appointments/{id}/reschedule      | Added – Phase 6            |
| 2026-07-05 | GET .../appointments/doctor/{id}/day        | Added – Phase 6            |
| 2026-07-05 | GET .../appointments/patient/{id}/history   | Added – Phase 6            |
| 2026-07-05 | POST /api/v1/clinics/{cid}/queue/tokens     | Added – Phase 7            |
| 2026-07-05 | POST /api/v1/clinics/{cid}/queue/checkin    | Added – Phase 7            |
| 2026-07-05 | GET /api/v1/clinics/{cid}/queue/today       | Added – Phase 7            |
| 2026-07-05 | GET /api/v1/clinics/{cid}/queue/waiting     | Added – Phase 7            |
| 2026-07-05 | GET /api/v1/clinics/{cid}/queue/current     | Added – Phase 7            |
| 2026-07-05 | PATCH .../queue/{id}/call                   | Added – Phase 7            |
| 2026-07-05 | PATCH .../queue/{id}/start                  | Added – Phase 7            |
| 2026-07-05 | PATCH .../queue/{id}/complete               | Added – Phase 7            |
| 2026-07-05 | PATCH .../queue/{id}/skip                   | Added – Phase 7            |
| 2026-07-05 | PATCH .../queue/{id}/recall                 | Added – Phase 7            |
| 2026-07-05 | PATCH .../queue/{id}/cancel                 | Added – Phase 7            |
| 2026-07-05 | POST /api/v1/clinics/{cid}/billing/invoices | Added – Phase 8            |
| 2026-07-05 | GET /api/v1/clinics/{cid}/billing/invoices  | Added – Phase 8            |
| 2026-07-05 | GET .../billing/invoices/{id}               | Added – Phase 8            |
| 2026-07-05 | PATCH .../billing/invoices/{id}/issue       | Added – Phase 8            |
| 2026-07-05 | POST .../billing/invoices/{id}/payments     | Added – Phase 8            |
| 2026-07-05 | PATCH .../billing/invoices/{id}/cancel      | Added – Phase 8            |
| 2026-07-05 | PATCH .../billing/invoices/{id}/refund      | Added – Phase 8            |
| 2026-07-05 | POST /api/v1/clinics/{cid}/prescriptions    | Added – Phase 9            |
| 2026-07-05 | PUT /api/v1/clinics/{cid}/prescriptions/{id}| Added – Phase 9            |
| 2026-07-05 | GET /api/v1/clinics/{cid}/prescriptions/{id}| Added – Phase 9            |
| 2026-07-05 | GET .../appointments/{id}/prescription      | Added – Phase 9            |
| 2026-07-05 | GET .../patients/{id}/prescriptions         | Added – Phase 9            |
| 2026-07-05 | POST .../appointments/{id}/vitals           | Added – Phase 9            |
| 2026-07-05 | GET .../appointments/{id}/vitals            | Added – Phase 9            |
| 2026-07-05 | GET .../patients/{id}/vitals                | Added – Phase 9            |
| 2026-07-05 | POST .../appointments/{id}/notes            | Added – Phase 9            |
| 2026-07-05 | GET .../appointments/{id}/notes             | Added – Phase 9            |
| 2026-07-05 | GET /api/v1/clinics/{cid}/reports/revenue   | Added – Phase 10           |
| 2026-07-05 | GET .../reports/appointments                | Added – Phase 10           |
| 2026-07-05 | GET .../reports/queue                       | Added – Phase 10           |
| 2026-07-05 | GET .../reports/patients                    | Added – Phase 10           |
| 2026-07-05 | GET .../reports/doctors/{id}                | Added – Phase 10           |

---

## Error Response Format (all endpoints)

Uses RFC 7807 ProblemDetail. Every error has this shape:

```json
{
  "status": 409,
  "detail": "Email is already registered"
}
```

Validation errors include an `errors` map:

```json
{
  "status": 422,
  "title": "Validation Failed",
  "detail": null,
  "errors": {
    "email": "Must be a valid email address",
    "password": "Password must be at least 8 characters"
  }
}
```

---

## Phase 2 – Authentication

All endpoints under `/api/v1/auth/**` are **public** (no JWT required).

---

### POST /api/v1/auth/register

Register a new account. Auto-assigns `CLINIC_ADMIN` role. Returns JWT tokens immediately (user is logged in on register).

**Auth required:** No

**Request Body:**
```json
{
  "fullName": "Prakash Jha",
  "email": "prakash@example.com",
  "password": "password123"
}
```

| Field      | Type   | Required | Constraints          |
|------------|--------|----------|----------------------|
| `fullName` | string | Yes      | 2–255 characters     |
| `email`    | string | Yes      | Valid email format   |
| `password` | string | Yes      | Min 8 characters     |

**Response `201 Created`:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiZW1haWwiOiJwcmFrYXNoQGV4YW1wbGUuY29tIiwiaWF0IjoxNzUxNjMwMDAwLCJleHAiOjE3NTE2MzA5MDB9.abc123",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "userId": 1,
  "email": "prakash@example.com",
  "fullName": "Prakash Jha",
  "roles": ["CLINIC_ADMIN"],
  "emailVerificationToken": "f47ac10b-58cc-4372-a567-0e02b2c3d479"
}
```

> **Note:** `emailVerificationToken` is only present in the register response (dev mode).
> In production this would be sent via email and omitted from the response.
> Use it to call `/verify-email` immediately after registering.

**Error Responses:**

| Status | When |
|--------|------|
| `409 Conflict` | Email already registered |
| `422 Unprocessable Entity` | Validation failed (see errors map) |

---

### POST /api/v1/auth/login

Authenticate with email and password. Returns new JWT tokens.

**Auth required:** No

**Request Body:**
```json
{
  "email": "prakash@example.com",
  "password": "password123"
}
```

| Field      | Type   | Required | Constraints        |
|------------|--------|----------|--------------------|
| `email`    | string | Yes      | Valid email format |
| `password` | string | Yes      | Non-empty          |

**Response `200 OK`:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "660e9411-f3ac-51e5-b827-557766551111",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "userId": 1,
  "email": "prakash@example.com",
  "fullName": "Prakash Jha",
  "roles": ["CLINIC_ADMIN"]
}
```

**Error Responses:**

| Status | When |
|--------|------|
| `401 Unauthorized` | Wrong email or password |
| `422 Unprocessable Entity` | Validation failed |

---

### POST /api/v1/auth/refresh

Exchange an unexpired refresh token for a new access token.
**Token Rotation:** The submitted refresh token is deleted and a brand new one is returned. Store the new one.

**Auth required:** No

**Request Body:**
```json
{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response `200 OK`:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "770f0522-g4bd-62f6-c938-668877662222",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "userId": 1,
  "email": "prakash@example.com",
  "fullName": "Prakash Jha",
  "roles": ["CLINIC_ADMIN"]
}
```

**Error Responses:**

| Status | When |
|--------|------|
| `401 Unauthorized` | Token not found, already used, or expired |
| `422 Unprocessable Entity` | Validation failed |

---

### POST /api/v1/auth/logout

Invalidates the given refresh token. The access token remains valid until it expires (max 15 min) — this is expected behavior for stateless JWTs.

**Auth required:** No (only the refresh token is needed)

**Request Body:**
```json
{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response `200 OK`:**
```json
{
  "message": "Logged out successfully"
}
```

**Error Responses:**

| Status | When |
|--------|------|
| `422 Unprocessable Entity` | Validation failed |

---

### POST /api/v1/auth/forgot-password

Request a password reset. In production this sends an email; in dev mode the reset token is returned directly in the response.

**Auth required:** No

**Request Body:**
```json
{
  "email": "prakash@example.com"
}
```

**Response `200 OK` (email exists — dev mode):**
```json
{
  "message": "Password reset link sent (dev mode: token returned for testing)",
  "resetToken": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

**Response `200 OK` (email does not exist):**
```json
{
  "message": "If this email is registered, a reset link has been sent.",
  "resetToken": null
}
```

> **Security note:** The same `200 OK` is returned whether or not the email exists. This prevents attackers from discovering which emails are registered (user enumeration protection). `resetToken` is omitted from JSON when null (`@JsonInclude(NON_NULL)` not applied here — field is explicit null to signal dev vs prod path).

**Error Responses:**

| Status | When |
|--------|------|
| `422 Unprocessable Entity` | Validation failed |

---

### POST /api/v1/auth/reset-password

Set a new password using the token from `/forgot-password`. Token expires after 1 hour. All active sessions are revoked after reset.

**Auth required:** No

**Request Body:**
```json
{
  "resetToken": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "newPassword": "newSecurePass456"
}
```

| Field         | Type   | Required | Constraints        |
|---------------|--------|----------|--------------------|
| `resetToken`  | string | Yes      | Non-empty          |
| `newPassword` | string | Yes      | Min 8 characters   |

**Response `200 OK`:**
```json
{
  "message": "Password reset successfully. Please log in with your new password."
}
```

**Error Responses:**

| Status | When |
|--------|------|
| `400 Bad Request` | Invalid token or token expired |
| `422 Unprocessable Entity` | Validation failed |

---

### POST /api/v1/auth/verify-email

Verify an email address using the token received after registration. Token is single-use and cleared after verification.

**Auth required:** No

**Request Body:**
```json
{
  "token": "f47ac10b-58cc-4372-a567-0e02b2c3d479"
}
```

**Response `200 OK`:**
```json
{
  "message": "Email verified successfully. You can now log in."
}
```

**Error Responses:**

| Status | When |
|--------|------|
| `400 Bad Request` | Invalid or already used token |
| `409 Conflict` | Email already verified |
| `422 Unprocessable Entity` | Validation failed |

---

## Phase 3 – Clinic Module

All write endpoints require a JWT Bearer token. `CLINIC_ADMIN` can only manage their own clinic. `SUPER_ADMIN` can manage any clinic.

---

### Clinic Response Object

Used by most endpoints below. Fields marked _(list only)_ are omitted in paginated list responses to avoid N+1 queries (they appear in full single-clinic responses).

```json
{
  "id": 1,
  "name": "City Heart Clinic",
  "slug": "city-heart-clinic",
  "description": "Cardiology specialists in Mumbai",
  "logoUrl": null,
  "phone": "+91 9876543210",
  "email": "contact@cityheartclinic.com",
  "website": "https://cityheartclinic.com",
  "addressLine1": "101 Marine Drive",
  "addressLine2": "Floor 3",
  "city": "Mumbai",
  "state": "Maharashtra",
  "postalCode": "400001",
  "country": "India",
  "timezone": "Asia/Kolkata",
  "ownerUserId": 1,
  "alwaysOpen": false,
  "emergencyClosed": false,
  "emergencyCloseReason": null,
  "emergencyClosedAt": null,
  "deleted": false,
  "currentlyOpen": true,
  "createdAt": "2026-07-04T10:00:00",
  "updatedAt": "2026-07-04T10:00:00",
  "businessHours": [ ... ],
  "upcomingClosures": [ ... ],
  "settings": { ... }
}
```

| Field                  | Type             | Notes                                                                 |
|------------------------|------------------|-----------------------------------------------------------------------|
| `id`                   | number           | Database primary key                                                  |
| `slug`                 | string           | URL-safe identifier, auto-generated from name                        |
| `timezone`             | string           | IANA timezone ID (e.g. `Asia/Kolkata`, `America/New_York`)           |
| `alwaysOpen`           | boolean          | 24/7 mode — ignores business hours (unless emergency closed)         |
| `emergencyClosed`      | boolean          | Overrides all hours; clinic appears closed                           |
| `emergencyCloseReason` | string \| null   | Present when `emergencyClosed: true`                                 |
| `emergencyClosedAt`    | datetime \| null | When emergency closure was triggered                                 |
| `currentlyOpen`        | boolean          | Real-time computed — changes minute to minute, never stored in DB    |
| `businessHours`        | array \| null    | All shifts (null in paginated list)                                  |
| `upcomingClosures`     | array \| null    | Future planned closures (null in paginated list)                     |
| `settings`             | object \| null   | Appointment config (null in paginated list)                          |

---

### POST /api/v1/clinics

Create a new clinic. A `CLINIC_ADMIN` can only create one clinic.

**Auth required:** Yes — `CLINIC_ADMIN`

**Request Body:**
```json
{
  "name": "City Heart Clinic",
  "description": "Cardiology specialists in Mumbai",
  "phone": "+91 9876543210",
  "email": "contact@cityheartclinic.com",
  "website": "https://cityheartclinic.com",
  "addressLine1": "101 Marine Drive",
  "addressLine2": "Floor 3",
  "city": "Mumbai",
  "state": "Maharashtra",
  "postalCode": "400001",
  "country": "India",
  "timezone": "Asia/Kolkata"
}
```

| Field          | Type   | Required | Constraints                                         |
|----------------|--------|----------|-----------------------------------------------------|
| `name`         | string | Yes      | Max 255 chars                                       |
| `description`  | string | No       | Max 500 chars                                       |
| `phone`        | string | No       | Max 30 chars                                        |
| `email`        | string | No       | Valid email format                                  |
| `website`      | string | No       | Max 512 chars                                       |
| `addressLine1` | string | No       |                                                     |
| `addressLine2` | string | No       |                                                     |
| `city`         | string | No       | Max 100 chars                                       |
| `state`        | string | No       | Max 100 chars                                       |
| `postalCode`   | string | No       | Max 20 chars                                        |
| `country`      | string | No       | Max 100 chars. Default: `"India"`                   |
| `timezone`     | string | No       | IANA timezone ID. Default: `"Asia/Kolkata"`         |

**Response `201 Created`:** Full Clinic Response Object (with `businessHours: []`, `upcomingClosures: []`, `settings` with defaults)

**Error Responses:**

| Status | When |
|--------|------|
| `401 Unauthorized` | Missing or invalid JWT |
| `403 Forbidden` | Not a `CLINIC_ADMIN` |
| `409 Conflict` | Admin already owns a clinic |
| `400 Bad Request` | Invalid timezone string |
| `422 Unprocessable Entity` | Validation failed |

---

### GET /api/v1/clinics/mine

Get the authenticated admin's own clinic. Convenient shortcut — no need to know the clinic ID.

**Auth required:** Yes — `CLINIC_ADMIN`

**Response `200 OK`:** Full Clinic Response Object

**Error Responses:**

| Status | When |
|--------|------|
| `401 Unauthorized` | Missing or invalid JWT |
| `404 Not Found` | Admin has not created a clinic yet |

---

### GET /api/v1/clinics

Paginated list of all active (non-deleted) clinics. Public endpoint.

**Auth required:** No

**Query Parameters:**

| Param    | Type   | Default | Description                           |
|----------|--------|---------|---------------------------------------|
| `city`   | string | —       | Filter by city (case-insensitive)     |
| `page`   | int    | 0       | Page number (0-indexed)               |
| `size`   | int    | 20      | Results per page                      |
| `sort`   | string | `name`  | Sort field                            |

**Response `200 OK`:**
```json
{
  "content": [
    {
      "id": 1,
      "name": "City Heart Clinic",
      "slug": "city-heart-clinic",
      "city": "Mumbai",
      "currentlyOpen": true,
      "alwaysOpen": false,
      "emergencyClosed": false,
      ...
    }
  ],
  "totalElements": 42,
  "totalPages": 3,
  "size": 20,
  "number": 0
}
```

> `businessHours`, `upcomingClosures`, and `settings` are `null` in list items. Fetch individual clinic with `GET /clinics/{id}` for full details.

---

### GET /api/v1/clinics/{id}

Get a single clinic by ID. Returns full details. Public endpoint.

**Auth required:** No

**Response `200 OK`:** Full Clinic Response Object

**Error Responses:**

| Status | When |
|--------|------|
| `404 Not Found` | Clinic not found or deleted |

---

### PUT /api/v1/clinics/{id}

Partial update — only fields included in the request body are changed. Omit a field to leave it unchanged.

**Auth required:** Yes — `CLINIC_ADMIN` (own clinic only) or `SUPER_ADMIN`

**Request Body:** (all fields optional)
```json
{
  "name": "Updated Clinic Name",
  "description": "New description",
  "phone": "+91 9999999999",
  "email": "new@clinic.com",
  "website": "https://new-site.com",
  "addressLine1": "New Address",
  "addressLine2": null,
  "city": "Delhi",
  "state": "Delhi",
  "postalCode": "110001",
  "country": "India",
  "timezone": "Asia/Kolkata",
  "alwaysOpen": true
}
```

| Field        | Notes                                                            |
|--------------|------------------------------------------------------------------|
| `alwaysOpen` | Toggle 24/7 mode. `true` = ignore business hours (except emergency close) |
| `timezone`   | IANA ID — validated against Java's ZoneId                        |

**Response `200 OK`:** Full Clinic Response Object

**Error Responses:**

| Status | When |
|--------|------|
| `401 Unauthorized` | Missing or invalid JWT |
| `403 Forbidden` | Not the owner or SUPER_ADMIN |
| `404 Not Found` | Clinic not found or deleted |
| `400 Bad Request` | Invalid timezone |

---

### DELETE /api/v1/clinics/{id}

Soft-delete a clinic. Data is retained in the database with `deleted: true`. The owning admin's `clinicId` is cleared so they can create a new one if needed.

**Auth required:** Yes — `CLINIC_ADMIN` (own clinic only) or `SUPER_ADMIN`

**Response `204 No Content`**

**Error Responses:**

| Status | When |
|--------|------|
| `401 Unauthorized` | Missing or invalid JWT |
| `403 Forbidden` | Not the owner or SUPER_ADMIN |
| `404 Not Found` | Clinic not found or already deleted |
| `409 Conflict` | Clinic already deleted |

---

### POST /api/v1/clinics/{id}/emergency-close

Immediately close the clinic regardless of business hours or 24/7 mode. Use for power outages, medical emergencies, sudden staff unavailability. Records who closed and when.

**Auth required:** Yes — `CLINIC_ADMIN` or `SUPER_ADMIN`

**Request Body:**
```json
{
  "reason": "Power outage - no ETA for restoration"
}
```

| Field    | Type   | Required | Constraints   |
|----------|--------|----------|---------------|
| `reason` | string | No       | Max 500 chars |

**Response `200 OK`:** Full Clinic Response Object with `emergencyClosed: true`

```json
{
  "emergencyClosed": true,
  "emergencyCloseReason": "Power outage - no ETA for restoration",
  "emergencyClosedAt": "2026-07-04T14:30:00",
  "currentlyOpen": false,
  ...
}
```

**Error Responses:**

| Status | When |
|--------|------|
| `401 Unauthorized` | Missing or invalid JWT |
| `403 Forbidden` | Not the owner or SUPER_ADMIN |
| `404 Not Found` | Clinic not found |
| `409 Conflict` | Clinic already emergency-closed |

---

### POST /api/v1/clinics/{id}/emergency-reopen

Lift the emergency closure. Clinic returns to its normal schedule (business hours / 24-7 mode). Clears reason, timestamp, and closed-by fields.

**Auth required:** Yes — `CLINIC_ADMIN` or `SUPER_ADMIN`

**Request Body:** None (empty body or `{}`)

**Response `200 OK`:** Full Clinic Response Object with `emergencyClosed: false`

**Error Responses:**

| Status | When |
|--------|------|
| `401 Unauthorized` | Missing or invalid JWT |
| `403 Forbidden` | Not the owner or SUPER_ADMIN |
| `404 Not Found` | Clinic not found |
| `400 Bad Request` | Clinic is not currently emergency-closed |

---

### GET /api/v1/clinics/{id}/hours

Get all business hours for a clinic. Public endpoint.

**Auth required:** No

**Response `200 OK`:**
```json
[
  { "id": 1, "dayOfWeek": "MONDAY",    "openTime": "09:00", "closeTime": "13:00", "shiftLabel": "Morning" },
  { "id": 2, "dayOfWeek": "MONDAY",    "openTime": "17:00", "closeTime": "21:00", "shiftLabel": "Evening" },
  { "id": 3, "dayOfWeek": "TUESDAY",   "openTime": "09:00", "closeTime": "17:00", "shiftLabel": null },
  { "id": 4, "dayOfWeek": "WEDNESDAY", "openTime": "09:00", "closeTime": "17:00", "shiftLabel": null }
]
```

> Days with no rows = clinic closed that day. Multiple rows for the same day = multiple shifts.

| Field        | Type   | Notes                                                   |
|--------------|--------|---------------------------------------------------------|
| `dayOfWeek`  | string | Java DayOfWeek: `MONDAY`…`SUNDAY`                      |
| `openTime`   | string | `HH:mm` format (24-hour, in clinic's local timezone)   |
| `closeTime`  | string | `HH:mm` format. Always after `openTime`                |
| `shiftLabel` | string | Optional label (e.g. "Morning", "Evening", "OPD")      |

---

### PUT /api/v1/clinics/{id}/hours

Replace **all** business hours for the entire week in one call. Deletes existing hours then inserts the submitted list. Send an empty array `[]` to mark the clinic as having no regular hours.

**Auth required:** Yes — `CLINIC_ADMIN` or `SUPER_ADMIN`

**Request Body:**
```json
[
  { "dayOfWeek": "MONDAY",    "openTime": "09:00", "closeTime": "13:00", "shiftLabel": "Morning" },
  { "dayOfWeek": "MONDAY",    "openTime": "17:00", "closeTime": "21:00", "shiftLabel": "Evening" },
  { "dayOfWeek": "TUESDAY",   "openTime": "09:00", "closeTime": "17:00", "shiftLabel": null },
  { "dayOfWeek": "WEDNESDAY", "openTime": "09:00", "closeTime": "17:00", "shiftLabel": null },
  { "dayOfWeek": "THURSDAY",  "openTime": "09:00", "closeTime": "17:00", "shiftLabel": null },
  { "dayOfWeek": "FRIDAY",    "openTime": "09:00", "closeTime": "13:00", "shiftLabel": null },
  { "dayOfWeek": "SATURDAY",  "openTime": "10:00", "closeTime": "14:00", "shiftLabel": "Half Day" }
]
```

| Field        | Type   | Required | Constraints                                    |
|--------------|--------|----------|------------------------------------------------|
| `dayOfWeek`  | string | Yes      | `MONDAY`–`SUNDAY` (case-sensitive)             |
| `openTime`   | string | Yes      | `HH:mm` 24-hour format                         |
| `closeTime`  | string | Yes      | `HH:mm`, must be after `openTime`              |
| `shiftLabel` | string | No       | Optional display name for the shift            |

**Validation rules:**
- `closeTime` must be after `openTime` for each shift
- No overlapping shifts on the same day
- Maximum 6 shifts per day

**Response `200 OK`:** Array of all saved `BusinessHoursResponse` objects (same as GET /hours)

**Error Responses:**

| Status | When |
|--------|------|
| `401 Unauthorized` | Missing or invalid JWT |
| `403 Forbidden` | Not the owner or SUPER_ADMIN |
| `404 Not Found` | Clinic not found |
| `400 Bad Request` | closeTime ≤ openTime, or max 6 shifts exceeded |
| `409 Conflict` | Overlapping shifts on the same day |

---

### PUT /api/v1/clinics/{id}/hours/{day}

Replace shifts for a **single day** only. Other days are untouched. `{day}` must be a `DayOfWeek` name: `MONDAY`, `TUESDAY`, `WEDNESDAY`, `THURSDAY`, `FRIDAY`, `SATURDAY`, `SUNDAY`.

**Auth required:** Yes — `CLINIC_ADMIN` or `SUPER_ADMIN`

**Request Body:** Array of shifts for that day (omit `dayOfWeek` — it comes from the URL):
```json
[
  { "openTime": "09:00", "closeTime": "13:00", "shiftLabel": "Morning OPD" },
  { "openTime": "17:00", "closeTime": "20:00", "shiftLabel": "Evening OPD" }
]
```

Send `[]` to mark the clinic as closed on that day.

**Response `200 OK`:** Array of shifts for that day only

**Error Responses:** Same as PUT /hours

---

### DELETE /api/v1/clinics/{id}/hours/{day}

Remove all shifts for a specific day — clinic is closed that day. Equivalent to `PUT /hours/{day}` with `[]`.

**Auth required:** Yes — `CLINIC_ADMIN` or `SUPER_ADMIN`

**Response `204 No Content`**

**Error Responses:**

| Status | When |
|--------|------|
| `401 Unauthorized` | Missing or invalid JWT |
| `403 Forbidden` | Not the owner or SUPER_ADMIN |
| `404 Not Found` | Clinic not found |

---

### GET /api/v1/clinics/{id}/closures

Get planned closure dates for a clinic. Public endpoint.

**Auth required:** No

**Query Parameters:**

| Param      | Type    | Default | Description                                              |
|------------|---------|---------|----------------------------------------------------------|
| `upcoming` | boolean | `true`  | `true` = today + future only; `false` = all history too |

**Response `200 OK`:**
```json
[
  { "id": 1, "closureDate": "2026-08-15", "reason": "Independence Day" },
  { "id": 2, "closureDate": "2026-10-02", "reason": "Gandhi Jayanti" },
  { "id": 3, "closureDate": "2026-12-25", "reason": "Christmas" }
]
```

---

### POST /api/v1/clinics/{id}/closures

Add a planned closure date (holiday, staff training, etc.).

**Auth required:** Yes — `CLINIC_ADMIN` or `SUPER_ADMIN`

**Request Body:**
```json
{
  "closureDate": "2026-08-15",
  "reason": "Independence Day"
}
```

| Field         | Type   | Required | Constraints                        |
|---------------|--------|----------|------------------------------------|
| `closureDate` | string | Yes      | `yyyy-MM-dd` format                |
| `reason`      | string | No       | Optional description               |

**Response `201 Created`:**
```json
{ "id": 5, "closureDate": "2026-08-15", "reason": "Independence Day" }
```

**Error Responses:**

| Status | When |
|--------|------|
| `401 Unauthorized` | Missing or invalid JWT |
| `403 Forbidden` | Not the owner or SUPER_ADMIN |
| `404 Not Found` | Clinic not found |
| `409 Conflict` | A closure already exists for that date |
| `422 Unprocessable Entity` | Validation failed (missing date) |

---

### DELETE /api/v1/clinics/{id}/closures/{closureId}

Remove a planned closure date. Use to cancel a previously scheduled day off.

**Auth required:** Yes — `CLINIC_ADMIN` or `SUPER_ADMIN`

**Response `204 No Content`**

**Error Responses:**

| Status | When |
|--------|------|
| `401 Unauthorized` | Missing or invalid JWT |
| `403 Forbidden` | Not the owner or SUPER_ADMIN |
| `404 Not Found` | Clinic or closure date not found |

---

### GET /api/v1/clinics/{id}/settings

Get appointment configuration for a clinic. Public endpoint.

**Auth required:** No

**Response `200 OK`:**
```json
{
  "appointmentDurationMins": 20,
  "advanceBookingDays": 30,
  "cancellationWindowHours": 24,
  "maxPatientsPerDay": 0,
  "allowWalkIns": true,
  "autoConfirmAppointments": true
}
```

| Field                     | Type    | Notes                                                |
|---------------------------|---------|------------------------------------------------------|
| `appointmentDurationMins` | int     | Slot duration in minutes (5–240)                     |
| `advanceBookingDays`      | int     | How far in advance patients can book (1–365)         |
| `cancellationWindowHours` | int     | Min hours before appointment to allow cancel (0–168) |
| `maxPatientsPerDay`       | int     | Daily cap. `0` = unlimited                           |
| `allowWalkIns`            | boolean | Whether walk-in patients are accepted                |
| `autoConfirmAppointments` | boolean | Auto-confirm vs. require manual confirmation         |

---

### PUT /api/v1/clinics/{id}/settings

Update appointment configuration. All fields are required (full replace).

**Auth required:** Yes — `CLINIC_ADMIN` or `SUPER_ADMIN`

**Request Body:**
```json
{
  "appointmentDurationMins": 30,
  "advanceBookingDays": 14,
  "cancellationWindowHours": 48,
  "maxPatientsPerDay": 50,
  "allowWalkIns": false,
  "autoConfirmAppointments": false
}
```

| Field                     | Type    | Required | Constraints                  |
|---------------------------|---------|----------|------------------------------|
| `appointmentDurationMins` | int     | Yes      | 5–240                        |
| `advanceBookingDays`      | int     | Yes      | 1–365                        |
| `cancellationWindowHours` | int     | Yes      | 0–168                        |
| `maxPatientsPerDay`       | int     | Yes      | ≥ 0 (0 = unlimited)          |
| `allowWalkIns`            | boolean | Yes      |                              |
| `autoConfirmAppointments` | boolean | Yes      |                              |

**Response `200 OK`:** `ClinicSettingsResponse` object (same as GET /settings)

**Error Responses:**

| Status | When |
|--------|------|
| `401 Unauthorized` | Missing or invalid JWT |
| `403 Forbidden` | Not the owner or SUPER_ADMIN |
| `404 Not Found` | Clinic not found |
| `422 Unprocessable Entity` | Validation failed |

---

---

## Phase 4 – Doctor Module

All doctor endpoints live under `/api/v1/clinics/{clinicId}/…`.
All GET endpoints are public. Write endpoints require `CLINIC_ADMIN` or `SUPER_ADMIN`.

Key rules enforced across all doctor endpoints:
- All schedule times and break times must align to **10-minute boundaries** (`:00`, `:10`, `:20`, `:30`, `:40`, `:50`).
- All treatment durations must be **positive multiples of 10** (10, 20, 30, 60, 90, …).
- Max **4 shifts per day** per doctor.
- Breaks must fall within a scheduled working window.
- One override per doctor per date (upsert replaces the previous one).

---

### POST /api/v1/clinics/{clinicId}/doctors — Create Doctor

**Auth:** CLINIC_ADMIN / SUPER_ADMIN

**Request:**
```json
{
  "fullName": "Dr. Ayaan Khan",
  "email": "ayaan@example.com",
  "phone": "+919876543210",
  "qualification": "MBBS, MD",
  "specialization": "Cardiology",
  "bio": "10 years experience in cardiac care",
  "avatarUrl": "https://cdn.example.com/ayaan.jpg",
  "registrationNumber": "MCI-12345",
  "consultationFee": 500.00,
  "userId": null
}
```

| Field | Required | Notes |
|-------|----------|-------|
| `fullName` | Yes | Doctor's full name; auto-generates a URL slug |
| `email` | No | Unique within the clinic |
| `phone` | No | |
| `qualification` | No | e.g. MBBS, MD |
| `specialization` | No | e.g. Cardiology |
| `bio` | No | |
| `avatarUrl` | No | |
| `registrationNumber` | No | Medical council registration number |
| `consultationFee` | No | Default fee; can be overridden per treatment |
| `userId` | No | Link an existing user account to this doctor profile |

**Response 201:**
```json
{
  "id": 1,
  "clinicId": 1,
  "userId": null,
  "fullName": "Dr. Ayaan Khan",
  "slug": "dr-ayaan-khan",
  "email": "ayaan@example.com",
  "phone": "+919876543210",
  "qualification": "MBBS, MD",
  "specialization": "Cardiology",
  "consultationFee": 500.00,
  "active": true,
  "deleted": false,
  "treatments": [],
  "createdAt": "2026-07-05T10:00:00",
  "updatedAt": "2026-07-05T10:00:00"
}
```

| Error | When |
|-------|------|
| `404 Not Found` | Clinic not found |
| `403 Forbidden` | Not the clinic owner |
| `409 Conflict` | Email already used in this clinic, or userId already linked to another doctor |

---

### GET /api/v1/clinics/{clinicId}/doctors — List Doctors

**Auth:** Public

**Query params:**
| Param | Default | Notes |
|-------|---------|-------|
| `activeOnly` | `true` | `false` includes inactive doctors |
| `page` | `0` | |
| `size` | `20` | |
| `sort` | `fullName` | |

**Response 200:** Paginated list (Spring Page format)
```json
{
  "content": [ { ... doctor summary ... } ],
  "totalElements": 5,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

---

### GET /api/v1/clinics/{clinicId}/doctors/{doctorId} — Get Doctor

**Auth:** Public

**Response 200:** Full doctor object including assigned treatments. Slug can also be used via a separate `/api/v1/doctors/by-slug/{slug}` endpoint (if added later).

---

### PUT /api/v1/clinics/{clinicId}/doctors/{doctorId} — Update Doctor

**Auth:** CLINIC_ADMIN / SUPER_ADMIN

All fields are optional (patch semantics):
```json
{
  "fullName": "Dr. Ayaan Khan",
  "email": "newemail@example.com",
  "phone": "+91...",
  "qualification": "MBBS, MD, DM",
  "specialization": "Interventional Cardiology",
  "bio": "...",
  "avatarUrl": "...",
  "registrationNumber": "...",
  "consultationFee": 600.00,
  "active": false
}
```

Setting `"active": false` hides the doctor from scheduling (no slots generated).

| Error | When |
|-------|------|
| `409 Conflict` | New email already taken in this clinic |

---

### DELETE /api/v1/clinics/{clinicId}/doctors/{doctorId} — Delete Doctor

**Auth:** CLINIC_ADMIN / SUPER_ADMIN
Soft-delete. The doctor's historical data is preserved.

**Response:** `204 No Content`

---

### PUT /api/v1/clinics/{clinicId}/doctors/{doctorId}/link-user/{userId}

**Auth:** CLINIC_ADMIN / SUPER_ADMIN

Links a registered user account to this doctor profile so the doctor can log in and see their own schedule.

| Error | When |
|-------|------|
| `409 Conflict` | User already linked to another doctor |

---

### DELETE /api/v1/clinics/{clinicId}/doctors/{doctorId}/link-user

Removes the user-doctor link. The user account is not deleted.

---

### GET /api/v1/clinics/{clinicId}/doctors/{doctorId}/schedule — Get Weekly Schedule

**Auth:** Public

Returns all shifts across all days of the week.

```json
[
  { "id": 1, "dayOfWeek": "MONDAY", "startTime": "09:00", "endTime": "13:00", "shiftLabel": "Morning" },
  { "id": 2, "dayOfWeek": "MONDAY", "startTime": "14:00", "endTime": "18:00", "shiftLabel": "Afternoon" },
  { "id": 3, "dayOfWeek": "WEDNESDAY", "startTime": "09:00", "endTime": "17:00", "shiftLabel": null }
]
```

---

### PUT /api/v1/clinics/{clinicId}/doctors/{doctorId}/schedule — Replace Full Schedule

**Auth:** CLINIC_ADMIN / SUPER_ADMIN

Replaces the doctor's **entire** weekly schedule. Send all days you want active; anything not included is deleted.

```json
[
  { "dayOfWeek": "MONDAY",    "startTime": "09:00", "endTime": "13:00", "shiftLabel": "Morning" },
  { "dayOfWeek": "MONDAY",    "startTime": "14:00", "endTime": "18:00", "shiftLabel": "Afternoon" },
  { "dayOfWeek": "WEDNESDAY", "startTime": "09:00", "endTime": "17:00" },
  { "dayOfWeek": "FRIDAY",    "startTime": "09:00", "endTime": "13:00" }
]
```

| Validation | Rule |
|------------|------|
| Max shifts per day | 4 |
| Time alignment | Must be at `:00`, `:10`, `:20`, `:30`, `:40`, or `:50` |
| Order | `endTime` must be after `startTime` |
| Overlaps | No two shifts on the same day may overlap |

| Error | When |
|-------|------|
| `400 Bad Request` | Time not on 10-min boundary, endTime ≤ startTime |
| `409 Conflict` | Overlapping shifts on same day, or more than 4 shifts per day |

---

### PUT /api/v1/clinics/{clinicId}/doctors/{doctorId}/schedule/{day} — Replace One Day

**Auth:** CLINIC_ADMIN / SUPER_ADMIN

Replaces shifts for a single day only. Other days are untouched.
`{day}` = `MONDAY`, `TUESDAY`, `WEDNESDAY`, `THURSDAY`, `FRIDAY`, `SATURDAY`, `SUNDAY`

```json
[
  { "startTime": "09:00", "endTime": "13:00", "shiftLabel": "Morning" },
  { "startTime": "14:00", "endTime": "17:00", "shiftLabel": "Afternoon" }
]
```

---

### DELETE /api/v1/clinics/{clinicId}/doctors/{doctorId}/schedule/{day} — Clear One Day

**Auth:** CLINIC_ADMIN / SUPER_ADMIN

Removes all shifts for that weekday. Also clears all recurring breaks for that day.

**Response:** `204 No Content`

---

### GET /api/v1/clinics/{clinicId}/doctors/{doctorId}/breaks — Get All Breaks

**Auth:** Public

Returns all recurring breaks across all days of the week.

```json
[
  { "id": 1, "dayOfWeek": "MONDAY", "breakStart": "13:00", "breakEnd": "14:00", "breakType": "LUNCH", "label": "Lunch break" },
  { "id": 2, "dayOfWeek": "MONDAY", "breakStart": "15:30", "breakEnd": "15:40", "breakType": "TEA",   "label": "Tea break" }
]
```

**Break types:** `LUNCH`, `TEA`, `PRAYER`, `PERSONAL`, `BREAK`

---

### PUT /api/v1/clinics/{clinicId}/doctors/{doctorId}/breaks/{day} — Replace Day Breaks

**Auth:** CLINIC_ADMIN / SUPER_ADMIN

Replaces all recurring breaks for a specific day. Breaks must fall entirely within the doctor's scheduled working windows for that day.

```json
[
  { "breakStart": "13:00", "breakEnd": "14:00", "breakType": "LUNCH", "label": "Lunch" },
  { "breakStart": "16:30", "breakEnd": "16:40", "breakType": "TEA" }
]
```

| Validation | Rule |
|------------|------|
| Time alignment | Must be at 10-minute boundaries |
| Within schedule | Break must fall within a working window for that day |
| No overlaps | Breaks on the same day must not overlap |

---

### DELETE /api/v1/clinics/{clinicId}/doctors/{doctorId}/breaks/{day}

**Auth:** CLINIC_ADMIN / SUPER_ADMIN
Removes all recurring breaks for that weekday.

**Response:** `204 No Content`

---

### GET /api/v1/clinics/{clinicId}/doctors/{doctorId}/overrides — List Upcoming Overrides

**Auth:** Public

Returns all day overrides for today or later.

```json
[
  {
    "id": 1,
    "overrideDate": "2026-07-10",
    "overrideType": "LATE_START",
    "startTime": "10:00",
    "endTime": null,
    "reason": "Morning conference"
  }
]
```

---

### PUT /api/v1/clinics/{clinicId}/doctors/{doctorId}/overrides — Set Day Override

**Auth:** CLINIC_ADMIN / SUPER_ADMIN

Sets (or replaces) a one-day override. The date comes from the request body field `overrideDate`.

**Override types and required fields:**

| Type | startTime | endTime | Effect |
|------|-----------|---------|--------|
| `DAY_OFF` | — | — | Doctor has no slots that day |
| `LATE_START` | Required | — | All working windows start no earlier than this time |
| `EARLY_END` | — | Required | All working windows end no later than this time |
| `CUSTOM_HOURS` | Required | Required | Weekly schedule is fully replaced with this single window |

Use `CUSTOM_HOURS` when the doctor arrives late AND leaves early on the same day.

```json
{
  "overrideDate": "2026-07-10",
  "overrideType": "LATE_START",
  "startTime": "10:30",
  "reason": "Morning conference"
}
```

```json
{
  "overrideDate": "2026-07-15",
  "overrideType": "DAY_OFF",
  "reason": "Doctor attending wedding"
}
```

```json
{
  "overrideDate": "2026-07-20",
  "overrideType": "CUSTOM_HOURS",
  "startTime": "11:00",
  "endTime": "15:00",
  "reason": "Half day — family event"
}
```

**Note:** `LATE_START` with time `10:17` will be rounded up to `10:20` when computing slots.

| Error | When |
|-------|------|
| `400 Bad Request` | Wrong fields for the override type |

---

### DELETE /api/v1/clinics/{clinicId}/doctors/{doctorId}/overrides/{date}

**Auth:** CLINIC_ADMIN / SUPER_ADMIN
Removes the override for a specific date. The doctor reverts to their normal weekly schedule.

`{date}` format: `yyyy-MM-dd`

**Response:** `204 No Content`

---

### GET /api/v1/clinics/{clinicId}/doctors/{doctorId}/leave — Get Leave

**Auth:** Public

**Query params:**
| Param | Default | Notes |
|-------|---------|-------|
| `upcomingOnly` | `true` | `false` includes past leave records |

```json
[
  { "id": 1, "leaveDate": "2026-07-25", "leaveType": "SICK",     "reason": "Flu" },
  { "id": 2, "leaveDate": "2026-07-26", "leaveType": "SICK",     "reason": "Flu" },
  { "id": 3, "leaveDate": "2026-08-05", "leaveType": "VACATION", "reason": "Annual leave" }
]
```

**Leave types:** `SICK`, `VACATION`, `CONFERENCE`, `EMERGENCY`, `PERSONAL`, `OTHER`

---

### POST /api/v1/clinics/{clinicId}/doctors/{doctorId}/leave — Add Leave

**Auth:** CLINIC_ADMIN / SUPER_ADMIN

Send a list of dates (useful for multi-day vacations). Dates that already exist are skipped silently. If **all** dates conflict, returns `409`.

```json
{
  "leaveDates": ["2026-08-01", "2026-08-02", "2026-08-03", "2026-08-04", "2026-08-05"],
  "leaveType": "VACATION",
  "reason": "Summer break"
}
```

**Response 201:** List of successfully created leave records (skipped dates are omitted, not an error).

| Error | When |
|-------|------|
| `409 Conflict` | All provided dates were already marked as leave |

---

### DELETE /api/v1/clinics/{clinicId}/doctors/{doctorId}/leave/{date}

**Auth:** CLINIC_ADMIN / SUPER_ADMIN
Removes a single leave date (e.g. doctor recovered early).

`{date}` format: `yyyy-MM-dd`

**Response:** `204 No Content`

---

### GET /api/v1/clinics/{clinicId}/doctors/{doctorId}/availability — Compute Available Slots

**Auth:** Public

**Query param:** `date=yyyy-MM-dd` (required)

Runs the full 11-step availability algorithm and returns all free 10-minute slot start times.

**Example:** `GET /api/v1/clinics/1/doctors/1/availability?date=2026-07-07`

**Response 200 (available):**
```json
{
  "doctorId": 1,
  "doctorName": "Dr. Ayaan Khan",
  "clinicId": 1,
  "date": "2026-07-07",
  "dayOfWeek": "MONDAY",
  "available": true,
  "workingWindows": [
    {
      "effectiveStart": "09:00",
      "effectiveEnd": "13:00",
      "overrideApplied": false,
      "overrideType": null
    },
    {
      "effectiveStart": "14:00",
      "effectiveEnd": "18:00",
      "overrideApplied": false,
      "overrideType": null
    }
  ],
  "breaks": [
    { "breakStart": "13:00", "breakEnd": "14:00", "breakType": "LUNCH", "label": "Lunch break" }
  ],
  "slots": ["09:00","09:10","09:20","09:30","09:40","09:50","10:00","10:10","10:20","10:30","10:40","10:50","11:00","11:10","11:20","11:30","11:40","11:50","12:00","12:10","12:20","12:30","12:40","12:50","14:00","14:10","...","17:50"],
  "totalSlots": 48
}
```

**Response 200 (unavailable):**
```json
{
  "doctorId": 1,
  "doctorName": "Dr. Ayaan Khan",
  "clinicId": 1,
  "date": "2026-07-10",
  "dayOfWeek": "THURSDAY",
  "available": false,
  "unavailableReason": "DOCTOR_ON_LEAVE",
  "slots": [],
  "totalSlots": 0
}
```

**unavailableReason values:**
| Value | Meaning |
|-------|---------|
| `DOCTOR_INACTIVE` | Doctor's `active` flag is false |
| `CLINIC_CLOSED` | Clinic has `emergencyClosed=true`, has a closure date for that day, or has no business hours for that weekday |
| `DOCTOR_ON_LEAVE` | Doctor has a leave record for that date |
| `DAY_OVERRIDE_DAY_OFF` | A `DAY_OFF` override exists for that date |
| `NO_SCHEDULE_FOR_DAY` | Doctor has no shifts scheduled for that weekday (after all overrides applied) |

---

### POST /api/v1/clinics/{clinicId}/treatments — Create Treatment Type

**Auth:** CLINIC_ADMIN / SUPER_ADMIN

Adds a treatment to the clinic-level catalog (e.g. "General Consultation", "ECG", "Root Canal").

```json
{
  "name": "General Consultation",
  "description": "Standard outpatient consultation",
  "defaultDurationMins": 20,
  "defaultFee": 500.00,
  "colorHex": "#4CAF50"
}
```

| Field | Required | Notes |
|-------|----------|-------|
| `name` | Yes | Unique within the clinic |
| `description` | No | |
| `defaultDurationMins` | Yes | **Must be a positive multiple of 10** |
| `defaultFee` | No | Default fee; can be overridden per doctor |
| `colorHex` | No | Hex color for calendar display, e.g. `#4CAF50` |

**Response 201:**
```json
{
  "id": 1,
  "clinicId": 1,
  "name": "General Consultation",
  "description": "Standard outpatient consultation",
  "defaultDurationMins": 20,
  "defaultFee": 500.00,
  "colorHex": "#4CAF50",
  "active": true,
  "createdAt": "2026-07-05T10:00:00",
  "updatedAt": "2026-07-05T10:00:00"
}
```

| Error | When |
|-------|------|
| `400 Bad Request` | Duration not a multiple of 10 |
| `409 Conflict` | Treatment name already exists in this clinic |

---

### GET /api/v1/clinics/{clinicId}/treatments — List Treatment Types

**Auth:** Public

**Query params:** `activeOnly=true` (default)

---

### GET /api/v1/clinics/{clinicId}/treatments/{treatmentId} — Get Treatment Type

**Auth:** Public

---

### PUT /api/v1/clinics/{clinicId}/treatments/{treatmentId} — Update Treatment Type

**Auth:** CLINIC_ADMIN / SUPER_ADMIN
All fields optional (patch semantics). `active: false` hides the treatment.

```json
{
  "name": "Extended Consultation",
  "defaultDurationMins": 30,
  "defaultFee": 700.00,
  "active": true
}
```

---

### DELETE /api/v1/clinics/{clinicId}/treatments/{treatmentId} — Delete Treatment Type

**Auth:** CLINIC_ADMIN / SUPER_ADMIN
Soft-delete.

**Response:** `204 No Content`

---

### GET /api/v1/clinics/{clinicId}/doctors/{doctorId}/treatments — Get Doctor's Treatments

**Auth:** Public

Shows which treatments a doctor offers, with effective duration and fee (custom override if set, otherwise clinic default).

```json
[
  {
    "id": 1,
    "treatmentTypeId": 1,
    "treatmentName": "General Consultation",
    "colorHex": "#4CAF50",
    "customDurationMins": null,
    "customFee": null,
    "effectiveDurationMins": 20,
    "effectiveFee": 500.00,
    "active": true
  },
  {
    "id": 2,
    "treatmentTypeId": 2,
    "treatmentName": "ECG",
    "colorHex": "#2196F3",
    "customDurationMins": 30,
    "customFee": 800.00,
    "effectiveDurationMins": 30,
    "effectiveFee": 800.00,
    "active": true
  }
]
```

---

### PUT /api/v1/clinics/{clinicId}/doctors/{doctorId}/treatments/{treatmentId} — Assign Treatment to Doctor

**Auth:** CLINIC_ADMIN / SUPER_ADMIN

Links a treatment type to a doctor. Optionally overrides the duration or fee for this doctor specifically. If already linked, updates the existing assignment (upsert).

```json
{
  "customDurationMins": 30,
  "customFee": 800.00,
  "active": true
}
```

All fields are optional. Omit `customDurationMins` / `customFee` to use the clinic defaults.

| Error | When |
|-------|------|
| `400 Bad Request` | `customDurationMins` not a positive multiple of 10 |
| `404 Not Found` | Treatment type doesn't belong to this clinic |

---

### DELETE /api/v1/clinics/{clinicId}/doctors/{doctorId}/treatments/{treatmentId}

**Auth:** CLINIC_ADMIN / SUPER_ADMIN
Removes the treatment assignment from this doctor.

**Response:** `204 No Content`

---

---

## Phase 5 – Patient Module

Patients are clinic-scoped (multi-tenant). The same person visiting two clinics
has two separate records. Patient data is **private** — all endpoints require a valid JWT.

Real-life scenarios covered:
- Walk-in registration by receptionist (no user account needed)
- Patient self-service portal (link user account → `GET /patients/me`)
- Duplicate prevention: unique phone per clinic
- Blood group, allergies, chronic conditions, current medications
- Emergency contact (name, phone, relationship)
- Search by name, phone, or email
- Soft delete (medical records preserved for legal compliance)

---

### POST /api/v1/clinics/{clinicId}/patients — Register Patient

**Auth:** CLINIC_ADMIN / SUPER_ADMIN

```json
{
  "firstName": "Riya",
  "lastName": "Sharma",
  "phone": "+919876543210",
  "email": "riya@example.com",
  "dateOfBirth": "1990-05-15",
  "gender": "FEMALE",
  "bloodGroup": "B+",
  "allergies": "Penicillin",
  "chronicConditions": "Asthma",
  "currentMedications": "Salbutamol inhaler",
  "emergencyContactName": "Raj Sharma",
  "emergencyContactPhone": "+919876543211",
  "emergencyContactRelation": "Spouse",
  "address": "42, Park Street, Mumbai 400001",
  "notes": "Patient prefers morning appointments",
  "userId": null
}
```

| Field | Required | Notes |
|-------|----------|-------|
| `firstName` | Yes | |
| `lastName` | No | |
| `phone` | Yes | Unique per clinic — prevents duplicate registration |
| `email` | No | |
| `dateOfBirth` | No | Format: `yyyy-MM-dd`, must be in the past |
| `gender` | No | `MALE`, `FEMALE`, `OTHER`, `PREFER_NOT_TO_SAY` |
| `bloodGroup` | No | One of: `A+`, `A-`, `B+`, `B-`, `AB+`, `AB-`, `O+`, `O-` |
| `allergies` | No | Free text |
| `chronicConditions` | No | Free text |
| `currentMedications` | No | Free text |
| `emergencyContact*` | No | Name, phone, and relationship of emergency contact |
| `address` | No | Free text |
| `notes` | No | Internal staff notes — not visible to patient |
| `userId` | No | Link to existing user account at registration time |

**Response 201:**
```json
{
  "id": 1,
  "clinicId": 1,
  "userId": null,
  "firstName": "Riya",
  "lastName": "Sharma",
  "fullName": "Riya Sharma",
  "phone": "+919876543210",
  "email": "riya@example.com",
  "dateOfBirth": "1990-05-15",
  "gender": "FEMALE",
  "bloodGroup": "B+",
  "allergies": "Penicillin",
  "chronicConditions": "Asthma",
  "currentMedications": "Salbutamol inhaler",
  "emergencyContactName": "Raj Sharma",
  "emergencyContactPhone": "+919876543211",
  "emergencyContactRelation": "Spouse",
  "address": "42, Park Street, Mumbai 400001",
  "notes": "Patient prefers morning appointments",
  "active": true,
  "deleted": false,
  "createdAt": "2026-07-05T10:00:00",
  "updatedAt": "2026-07-05T10:00:00"
}
```

| Error | When |
|-------|------|
| `401 Unauthorized` | No JWT provided |
| `403 Forbidden` | Not the clinic owner or SUPER_ADMIN |
| `404 Not Found` | Clinic not found |
| `409 Conflict` | Phone already registered in this clinic, or userId already linked to another patient |
| `422 Unprocessable Entity` | Validation failed (invalid blood group, future dateOfBirth, etc.) |

---

### GET /api/v1/clinics/{clinicId}/patients — List / Search Patients

**Auth:** Any valid JWT (CLINIC_ADMIN, DOCTOR, RECEPTIONIST, etc.)

**Query params:**

| Param | Default | Notes |
|-------|---------|-------|
| `q` | — | Optional search term; matches name, phone, or email (case-insensitive) |
| `activeOnly` | `true` | `false` includes inactive patients |
| `page` | `0` | |
| `size` | `20` | |
| `sort` | `firstName` | |

**Example requests:**
```
GET /api/v1/clinics/1/patients
GET /api/v1/clinics/1/patients?q=riya
GET /api/v1/clinics/1/patients?q=9876543210
GET /api/v1/clinics/1/patients?q=riya@example.com&activeOnly=false
```

**Response 200:** Paginated (Spring Page format). List items omit `notes` to keep the payload lean.
```json
{
  "content": [
    {
      "id": 1,
      "clinicId": 1,
      "firstName": "Riya",
      "lastName": "Sharma",
      "fullName": "Riya Sharma",
      "phone": "+919876543210",
      "email": "riya@example.com",
      "gender": "FEMALE",
      "bloodGroup": "B+",
      "active": true,
      "deleted": false,
      "createdAt": "2026-07-05T10:00:00",
      "updatedAt": "2026-07-05T10:00:00"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

---

### GET /api/v1/clinics/{clinicId}/patients/{patientId} — Get Patient

**Auth:** Any valid JWT

Returns the full patient profile including all medical fields, emergency contact, and staff notes.

---

### PUT /api/v1/clinics/{clinicId}/patients/{patientId} — Update Patient

**Auth:** CLINIC_ADMIN / SUPER_ADMIN

All fields are optional (patch semantics):
```json
{
  "bloodGroup": "O+",
  "allergies": "Penicillin, Aspirin",
  "chronicConditions": "Asthma, Hypertension",
  "currentMedications": "Salbutamol, Amlodipine 5mg",
  "emergencyContactName": "Raj Sharma",
  "emergencyContactPhone": "+919876543211",
  "emergencyContactRelation": "Spouse",
  "active": false
}
```

Setting `"active": false` marks the patient as inactive. They won't appear in the default
active list but their records are preserved.

| Error | When |
|-------|------|
| `409 Conflict` | New phone already used by another patient in this clinic |

---

### DELETE /api/v1/clinics/{clinicId}/patients/{patientId} — Delete Patient

**Auth:** CLINIC_ADMIN / SUPER_ADMIN

Soft-delete only. Patient records are never hard-deleted (medical record retention requirements).

**Response:** `204 No Content`

---

### PUT /api/v1/clinics/{clinicId}/patients/{patientId}/link-user/{userId}

**Auth:** CLINIC_ADMIN / SUPER_ADMIN

Links a registered user account to this patient profile. Once linked, the patient can
log in and call `GET /api/v1/patients/me` to view their own profile.

| Error | When |
|-------|------|
| `409 Conflict` | User already linked to another patient profile |

---

### DELETE /api/v1/clinics/{clinicId}/patients/{patientId}/link-user

**Auth:** CLINIC_ADMIN / SUPER_ADMIN

Removes the user-patient link. The user account is unaffected.

**Response 200:** Updated patient profile (userId will be null).

---

### GET /api/v1/patients/me — Patient Self-View

**Auth:** Any valid JWT

A patient logs in with their own user account and views their profile.
Returns the patient record linked to the caller's `userId`.
Returns `404` if the user account has no linked patient profile.

```
GET /api/v1/patients/me
Authorization: Bearer <patient_jwt>
```

**Response 200:** Full patient profile (same shape as GET by ID).

---

---

## Phase 6 – Appointment System

All appointment endpoints live under `/api/v1/clinics/{clinicId}/appointments`.
All endpoints require authentication. Write endpoints require `CLINIC_ADMIN` or `SUPER_ADMIN`.

**Appointment status flow:**
```
PENDING → CONFIRMED → IN_PROGRESS → COMPLETED
                   ↘ NO_SHOW
PENDING/CONFIRMED → CANCELLED  (via /cancel endpoint)
PENDING/CONFIRMED → RESCHEDULED (via /reschedule — creates a new appointment)
```

**Booking validation (8 steps):**
1. Clinic exists
2. Doctor is active and belongs to this clinic
3. Patient belongs to this clinic
4. Treatment type (if given) belongs to this clinic
5. Date is today or future
6. Doctor is available on that date (runs the full 11-step availability algorithm)
7. Every 10-min chunk of the appointment fits in a free slot (validates working hours, breaks, and existing bookings)
8. DB-level conflict check as a safety net (protects against race conditions)

**Note:** `GET /doctors/{id}/availability?date=` now returns only truly free slots — booked appointments are automatically subtracted.

---

### POST /api/v1/clinics/{clinicId}/appointments — Book Appointment

**Auth:** CLINIC_ADMIN / SUPER_ADMIN

```json
{
  "patientId": 1,
  "doctorId": 1,
  "treatmentTypeId": 2,
  "appointmentDate": "2026-07-10",
  "startTime": "09:30",
  "reason": "Chest pain, shortness of breath",
  "notes": ""
}
```

| Field | Required | Notes |
|-------|----------|-------|
| `patientId` | Yes | Must belong to this clinic |
| `doctorId` | Yes | Must be active and belong to this clinic |
| `treatmentTypeId` | No | If provided, determines duration (doctor's effective duration used) |
| `appointmentDate` | Yes | Today or future. Format: `yyyy-MM-dd` |
| `startTime` | Yes | Must be a free slot from `GET /doctors/{id}/availability`. Format: `HH:mm` |
| `reason` | No | Patient's chief complaint / reason for visit |
| `notes` | No | Initial internal notes |

**Duration resolution:**
- `treatmentTypeId` provided + doctor has custom duration → use doctor's custom duration
- `treatmentTypeId` provided, no custom → use treatment type's default duration
- No `treatmentTypeId` → 10 minutes (one slot)

**Response 201:**
```json
{
  "id": 1,
  "clinicId": 1,
  "doctorId": 1,
  "patientId": 1,
  "treatmentTypeId": 2,
  "doctorName": "Dr. Ayaan Khan",
  "patientName": "Riya Sharma",
  "treatmentName": "ECG",
  "appointmentDate": "2026-07-10",
  "startTime": "09:30",
  "endTime": "10:00",
  "durationMins": 30,
  "status": "PENDING",
  "reason": "Chest pain, shortness of breath",
  "createdAt": "2026-07-05T10:00:00",
  "updatedAt": "2026-07-05T10:00:00"
}
```

| Error | When |
|-------|------|
| `400 Bad Request` | startTime not on 10-min boundary, date in the past |
| `404 Not Found` | Clinic, doctor, patient, or treatment not found |
| `409 Conflict` | Doctor unavailable (leave/closed/no-schedule), slot not free, slot already booked |

---

### GET /api/v1/clinics/{clinicId}/appointments — List / Search Appointments

**Auth:** Any valid JWT

**Query params (all optional, combinable):**

| Param | Notes |
|-------|-------|
| `doctorId` | Filter by doctor |
| `patientId` | Filter by patient |
| `date` | Specific date (`yyyy-MM-dd`) |
| `status` | `PENDING`, `CONFIRMED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`, `NO_SHOW`, `RESCHEDULED` |
| `page`, `size` | Pagination |

**Example requests:**
```
GET /api/v1/clinics/1/appointments?date=2026-07-10
GET /api/v1/clinics/1/appointments?doctorId=1&date=2026-07-10&status=CONFIRMED
GET /api/v1/clinics/1/appointments?patientId=5
GET /api/v1/clinics/1/appointments?status=PENDING
```

**Response 200:** Paginated (Spring Page format).

---

### GET /api/v1/clinics/{clinicId}/appointments/{appointmentId} — Get Appointment

**Auth:** Any valid JWT

Full appointment detail including denormalized doctor name, patient name, and treatment name.

**Response 200:**
```json
{
  "id": 1,
  "clinicId": 1,
  "doctorId": 1,
  "patientId": 1,
  "treatmentTypeId": 2,
  "doctorName": "Dr. Ayaan Khan",
  "patientName": "Riya Sharma",
  "treatmentName": "ECG",
  "appointmentDate": "2026-07-10",
  "startTime": "09:30",
  "endTime": "10:00",
  "durationMins": 30,
  "status": "CONFIRMED",
  "reason": "Chest pain, shortness of breath",
  "notes": null,
  "cancellationReason": null,
  "rescheduledFromId": null,
  "createdAt": "2026-07-05T10:00:00",
  "updatedAt": "2026-07-05T10:30:00"
}
```

Null fields (`cancellationReason`, `rescheduledFromId`, `notes`) are omitted from the response when not set (`@JsonInclude(NON_NULL)`).

| Error | When |
|-------|------|
| `401 Unauthorized` | No JWT |
| `404 Not Found` | Appointment not found in this clinic |

---

### PATCH /api/v1/clinics/{clinicId}/appointments/{appointmentId}/status — Update Status

**Auth:** CLINIC_ADMIN / SUPER_ADMIN

Advance the appointment lifecycle. Invalid transitions return `409`.

```json
{ "status": "CONFIRMED", "notes": "Confirmed via phone" }
{ "status": "IN_PROGRESS" }
{ "status": "COMPLETED", "notes": "Follow-up in 2 weeks" }
{ "status": "NO_SHOW" }
```

| Transition | When to use |
|------------|-------------|
| `PENDING → CONFIRMED` | Receptionist confirms the booking |
| `CONFIRMED → IN_PROGRESS` | Doctor has called the patient in |
| `CONFIRMED → COMPLETED` | Direct completion (skip IN_PROGRESS) |
| `IN_PROGRESS → COMPLETED` | Consultation finished |
| `CONFIRMED → NO_SHOW` | Patient didn't arrive |

**Response 200:** Updated appointment object with the new status and `updatedAt`.

```json
{
  "id": 1,
  "clinicId": 1,
  "doctorId": 1,
  "patientId": 3,
  "doctorName": "Dr. Ayaan Khan",
  "patientName": "Riya Sharma",
  "treatmentName": "General Consultation",
  "appointmentDate": "2026-07-10",
  "startTime": "09:30",
  "endTime": "09:50",
  "durationMins": 20,
  "status": "CONFIRMED",
  "reason": "Routine check-up",
  "notes": "Confirmed via phone",
  "createdAt": "2026-07-05T10:00:00",
  "updatedAt": "2026-07-05T10:30:00"
}
```

| Error | When |
|-------|------|
| `409 Conflict` | Invalid transition (e.g. PENDING → COMPLETED), or appointment already in a terminal state |

---

### PATCH /api/v1/clinics/{clinicId}/appointments/{appointmentId}/cancel — Cancel

**Auth:** CLINIC_ADMIN / SUPER_ADMIN

Frees the slot immediately — it will appear in `GET /doctors/{id}/availability` for the same date.

```json
{ "reason": "Patient requested cancellation" }
```

The body and `reason` field are both optional. Send `{}` or omit the body entirely if no reason.

**Response 200:**
```json
{
  "id": 1,
  "clinicId": 1,
  "doctorId": 1,
  "patientId": 1,
  "doctorName": "Dr. Ayaan Khan",
  "patientName": "Riya Sharma",
  "appointmentDate": "2026-07-10",
  "startTime": "09:30",
  "endTime": "10:00",
  "durationMins": 30,
  "status": "CANCELLED",
  "reason": "Chest pain, shortness of breath",
  "cancellationReason": "Patient requested cancellation",
  "cancelledAt": "2026-07-05T11:00:00",
  "createdAt": "2026-07-05T10:00:00",
  "updatedAt": "2026-07-05T11:00:00"
}
```

| Error | When |
|-------|------|
| `409 Conflict` | Appointment is already cancelled, completed, no-show, or rescheduled |

---

### PATCH /api/v1/clinics/{clinicId}/appointments/{appointmentId}/reschedule — Reschedule

**Auth:** CLINIC_ADMIN / SUPER_ADMIN

Moves an appointment to a new date/time while preserving the audit trail:
- Old appointment → `RESCHEDULED` (slot freed)
- New appointment → `PENDING` (new slot booked, same patient/doctor/treatment/duration)
- New appointment has `rescheduledFromId` pointing to the old one

```json
{
  "newDate": "2026-07-15",
  "newStartTime": "11:00",
  "reason": "Patient asked to reschedule"
}
```

Runs the same 8-step validation as a new booking on the new date/time.

**Response 200:** The new appointment record (status `PENDING`, with `rescheduledFromId` pointing to the old one):
```json
{
  "id": 8,
  "clinicId": 1,
  "doctorId": 1,
  "patientId": 3,
  "treatmentTypeId": 2,
  "doctorName": "Dr. Ayaan Khan",
  "patientName": "Riya Sharma",
  "treatmentName": "ECG",
  "appointmentDate": "2026-07-15",
  "startTime": "11:00",
  "endTime": "11:30",
  "durationMins": 30,
  "status": "PENDING",
  "reason": "Chest pain, shortness of breath",
  "rescheduledFromId": 1,
  "createdAt": "2026-07-05T12:00:00",
  "updatedAt": "2026-07-05T12:00:00"
}
```

The old appointment (id=1) is now `status: "RESCHEDULED"` and its slot on the original date is freed.

| Error | When |
|-------|------|
| `409 Conflict` | Old appointment already cancelled/completed/rescheduled, or new slot not available |
| `400 Bad Request` | newStartTime not on 10-min boundary, newDate in the past |

---

### GET /api/v1/clinics/{clinicId}/appointments/doctor/{doctorId}/day?date=yyyy-MM-dd

**Auth:** Any valid JWT

Doctor's full schedule for a specific date, ordered by start time. Includes all statuses
(PENDING, CONFIRMED, COMPLETED, CANCELLED, etc.) — the receptionist needs to see the
full picture including cancelled gaps.

**Example:** `GET /api/v1/clinics/1/appointments/doctor/1/day?date=2026-07-10`

**Response 200:**
```json
[
  {
    "id": 1,
    "clinicId": 1,
    "doctorId": 1,
    "patientId": 3,
    "doctorName": "Dr. Ayaan Khan",
    "patientName": "Riya Sharma",
    "treatmentName": "General Consultation",
    "appointmentDate": "2026-07-10",
    "startTime": "09:00",
    "endTime": "09:20",
    "durationMins": 20,
    "status": "COMPLETED",
    "reason": "Routine check-up",
    "createdAt": "2026-07-05T10:00:00",
    "updatedAt": "2026-07-10T09:22:00"
  },
  {
    "id": 2,
    "clinicId": 1,
    "doctorId": 1,
    "patientId": 7,
    "doctorName": "Dr. Ayaan Khan",
    "patientName": "Arjun Mehta",
    "treatmentName": "ECG",
    "appointmentDate": "2026-07-10",
    "startTime": "09:30",
    "endTime": "10:00",
    "durationMins": 30,
    "status": "CONFIRMED",
    "reason": "Chest pain",
    "createdAt": "2026-07-06T09:00:00",
    "updatedAt": "2026-07-06T09:05:00"
  },
  {
    "id": 3,
    "clinicId": 1,
    "doctorId": 1,
    "patientId": 12,
    "doctorName": "Dr. Ayaan Khan",
    "patientName": "Priya Nair",
    "appointmentDate": "2026-07-10",
    "startTime": "10:00",
    "endTime": "10:10",
    "durationMins": 10,
    "status": "CANCELLED",
    "cancellationReason": "Patient called to cancel",
    "cancelledAt": "2026-07-09T18:00:00",
    "createdAt": "2026-07-06T11:00:00",
    "updatedAt": "2026-07-09T18:00:00"
  }
]
```

---

### GET /api/v1/clinics/{clinicId}/appointments/patient/{patientId}/history

**Auth:** Any valid JWT

Patient's full appointment history for this clinic, most recent first.
Useful for the receptionist "patient card" view — shows all past visits at a glance.

**Example:** `GET /api/v1/clinics/1/appointments/patient/3/history`

**Response 200:**
```json
[
  {
    "id": 5,
    "clinicId": 1,
    "doctorId": 1,
    "patientId": 3,
    "doctorName": "Dr. Ayaan Khan",
    "patientName": "Riya Sharma",
    "treatmentName": "General Consultation",
    "appointmentDate": "2026-07-10",
    "startTime": "09:00",
    "endTime": "09:20",
    "durationMins": 20,
    "status": "CONFIRMED",
    "reason": "Follow-up",
    "createdAt": "2026-07-05T10:00:00",
    "updatedAt": "2026-07-05T10:00:00"
  },
  {
    "id": 2,
    "clinicId": 1,
    "doctorId": 1,
    "patientId": 3,
    "doctorName": "Dr. Ayaan Khan",
    "patientName": "Riya Sharma",
    "treatmentName": "ECG",
    "appointmentDate": "2026-06-20",
    "startTime": "10:00",
    "endTime": "10:30",
    "durationMins": 30,
    "status": "COMPLETED",
    "reason": "Chest pain investigation",
    "notes": "ECG normal, follow up in 3 weeks",
    "createdAt": "2026-06-15T09:00:00",
    "updatedAt": "2026-06-20T10:35:00"
  },
  {
    "id": 1,
    "clinicId": 1,
    "doctorId": 1,
    "patientId": 3,
    "doctorName": "Dr. Ayaan Khan",
    "patientName": "Riya Sharma",
    "appointmentDate": "2026-06-05",
    "startTime": "09:30",
    "endTime": "09:40",
    "durationMins": 10,
    "status": "RESCHEDULED",
    "reason": "Chest pain",
    "createdAt": "2026-06-01T08:00:00",
    "updatedAt": "2026-06-04T17:00:00"
  }
]
```

| Error | When |
|-------|------|
| `404 Not Found` | Patient not found in this clinic |

---

---

## Phase 7 – Queue Management

All queue endpoints live under `/api/v1/clinics/{clinicId}/queue`.
All endpoints require authentication. Write operations require `CLINIC_ADMIN` or `SUPER_ADMIN`.

**Token number logic:** Sequential per doctor per day — resets to 1 each morning automatically.
A multi-doctor clinic has separate token sequences per doctor (Token #3 at Dr. Khan is different from Token #3 at Dr. Patel).

**Status flow:**
```
WAITING → CALLED → IN_PROGRESS → COMPLETED
                ↘ SKIPPED → WAITING  (re-added at end via /recall)
WAITING / CALLED / SKIPPED → CANCELLED  (patient left clinic)
```

**Two entry points:**
- **Walk-in** (`POST /queue/tokens`) — patient arrives without appointment
- **Check-in** (`POST /queue/checkin`) — patient arrives for a booked appointment; token is linked to appointment and appointment is auto-confirmed

**Appointment sync:** Completing a queue token (`/complete`) automatically marks the linked appointment as `COMPLETED`. Starting a consultation (`/start`) marks it `IN_PROGRESS`.

---

### POST /api/v1/clinics/{clinicId}/queue/tokens — Generate Walk-in Token

**Auth:** CLINIC_ADMIN / SUPER_ADMIN

For patients who walk in without an appointment.

```json
{
  "patientId": 3,
  "doctorId": 1,
  "notes": "Fever and cough since 2 days"
}
```

| Field | Required | Notes |
|-------|----------|-------|
| `patientId` | Yes | Must belong to this clinic |
| `doctorId` | Yes | Must be active and belong to this clinic |
| `notes` | No | Receptionist notes at check-in |

**Response 201:**
```json
{
  "id": 7,
  "clinicId": 1,
  "doctorId": 1,
  "patientId": 3,
  "appointmentId": null,
  "doctorName": "Dr. Ayaan Khan",
  "patientName": "Riya Sharma",
  "tokenNumber": 7,
  "queueDate": "2026-07-05",
  "status": "WAITING",
  "notes": "Fever and cough since 2 days",
  "tokensAhead": 3,
  "estimatedWaitMins": 30,
  "createdAt": "2026-07-05T10:15:00",
  "updatedAt": "2026-07-05T10:15:00"
}
```

| Field | Notes |
|-------|-------|
| `tokenNumber` | Sequential per doctor per day — display this to patient |
| `tokensAhead` | How many WAITING patients are ahead (for wait display) |
| `estimatedWaitMins` | `tokensAhead × 10` minutes — rough estimate |

| Error | When |
|-------|------|
| `404 Not Found` | Doctor or patient not found |
| `400 Bad Request` | Doctor or patient doesn't belong to this clinic |

---

### POST /api/v1/clinics/{clinicId}/queue/checkin — Appointment Check-in

**Auth:** CLINIC_ADMIN / SUPER_ADMIN

For patients arriving for a booked appointment. Doctor and patient are taken from the appointment automatically.

```json
{
  "appointmentId": 5,
  "notes": "Patient arrived on time"
}
```

| Field | Required | Notes |
|-------|----------|-------|
| `appointmentId` | Yes | Must belong to this clinic |
| `notes` | No | Optional additional notes |

- Auto-generates a token linked to the appointment (`appointmentId` is set on the token)
- Auto-confirms the appointment (`PENDING → CONFIRMED`) if not already confirmed
- Calling `/complete` on the token will also mark the appointment `COMPLETED`

**Response 201:** Same shape as walk-in token response, with `appointmentId` populated.

| Error | When |
|-------|------|
| `404 Not Found` | Appointment not found in this clinic |
| `409 Conflict` | Appointment is cancelled/completed/rescheduled, or patient already checked in |

---

### GET /api/v1/clinics/{clinicId}/queue/today?doctorId= — Today's Full Queue

**Auth:** Any valid JWT

Returns all tokens for today (all statuses, all doctors). Pass `?doctorId=` to filter to one doctor.

**Response 200:**
```json
[
  {
    "id": 1,
    "clinicId": 1,
    "doctorId": 1,
    "patientId": 3,
    "doctorName": "Dr. Ayaan Khan",
    "patientName": "Riya Sharma",
    "tokenNumber": 1,
    "queueDate": "2026-07-05",
    "status": "COMPLETED",
    "completedAt": "2026-07-05T09:25:00",
    "createdAt": "2026-07-05T09:00:00",
    "updatedAt": "2026-07-05T09:25:00"
  },
  {
    "id": 4,
    "clinicId": 1,
    "doctorId": 1,
    "patientId": 7,
    "doctorName": "Dr. Ayaan Khan",
    "patientName": "Arjun Mehta",
    "tokenNumber": 4,
    "queueDate": "2026-07-05",
    "status": "IN_PROGRESS",
    "calledAt": "2026-07-05T10:10:00",
    "startedAt": "2026-07-05T10:12:00",
    "createdAt": "2026-07-05T09:30:00",
    "updatedAt": "2026-07-05T10:12:00"
  },
  {
    "id": 7,
    "clinicId": 1,
    "doctorId": 1,
    "patientId": 3,
    "doctorName": "Dr. Ayaan Khan",
    "patientName": "Priya Nair",
    "tokenNumber": 7,
    "queueDate": "2026-07-05",
    "status": "WAITING",
    "tokensAhead": 2,
    "estimatedWaitMins": 20,
    "createdAt": "2026-07-05T10:15:00",
    "updatedAt": "2026-07-05T10:15:00"
  }
]
```

---

### GET /api/v1/clinics/{clinicId}/queue/waiting?doctorId= — Waiting Patients Only

**Auth:** Any valid JWT

Returns only `WAITING` tokens — the "who is next" list. Each entry includes `tokensAhead` and `estimatedWaitMins`.

---

### GET /api/v1/clinics/{clinicId}/queue/current?doctorId= — Currently Being Seen

**Auth:** Any valid JWT

Returns only `IN_PROGRESS` tokens. In a typical clinic one doctor has at most one IN_PROGRESS token at a time.

---

### PATCH /api/v1/clinics/{clinicId}/queue/{tokenId}/call — Call Patient

**Auth:** CLINIC_ADMIN / SUPER_ADMIN

`WAITING → CALLED`. Records `calledAt` and `calledBy`.

Announce: *"Token #7, please proceed to Dr. Khan's room."*

No request body required.

**Response 200:**
```json
{
  "id": 7,
  "tokenNumber": 7,
  "status": "CALLED",
  "doctorName": "Dr. Ayaan Khan",
  "patientName": "Priya Nair",
  "calledAt": "2026-07-05T10:30:00",
  "queueDate": "2026-07-05",
  "createdAt": "2026-07-05T10:15:00",
  "updatedAt": "2026-07-05T10:30:00"
}
```

| Error | When |
|-------|------|
| `409 Conflict` | Token is not in WAITING state |

---

### PATCH /api/v1/clinics/{clinicId}/queue/{tokenId}/start — Start Consultation

**Auth:** CLINIC_ADMIN / SUPER_ADMIN

`CALLED → IN_PROGRESS`. Records `startedAt`.
Also sets the linked appointment to `IN_PROGRESS` (if any).

No request body required.

**Response 200:** Updated token with `status: "IN_PROGRESS"` and `startedAt`.

| Error | When |
|-------|------|
| `409 Conflict` | Token is not in CALLED state |

---

### PATCH /api/v1/clinics/{clinicId}/queue/{tokenId}/complete — Complete

**Auth:** CLINIC_ADMIN / SUPER_ADMIN

`IN_PROGRESS → COMPLETED`. Records `completedAt`.
Also marks the linked appointment `COMPLETED` (if any).

No request body required.

**Response 200:**
```json
{
  "id": 7,
  "tokenNumber": 7,
  "status": "COMPLETED",
  "doctorName": "Dr. Ayaan Khan",
  "patientName": "Priya Nair",
  "calledAt": "2026-07-05T10:30:00",
  "startedAt": "2026-07-05T10:32:00",
  "completedAt": "2026-07-05T10:48:00",
  "queueDate": "2026-07-05",
  "createdAt": "2026-07-05T10:15:00",
  "updatedAt": "2026-07-05T10:48:00"
}
```

| Error | When |
|-------|------|
| `409 Conflict` | Token is not in IN_PROGRESS state |

---

### PATCH /api/v1/clinics/{clinicId}/queue/{tokenId}/skip — Skip Patient

**Auth:** CLINIC_ADMIN / SUPER_ADMIN

`CALLED → SKIPPED`. Patient didn't respond when called. Frees the slot for the next patient.
Use `/recall` if the patient comes back.

No request body required.

**Response 200:** Updated token with `status: "SKIPPED"`.

| Error | When |
|-------|------|
| `409 Conflict` | Token is not in CALLED state |

---

### PATCH /api/v1/clinics/{clinicId}/queue/{tokenId}/recall — Recall Skipped Patient

**Auth:** CLINIC_ADMIN / SUPER_ADMIN

`SKIPPED → WAITING`. Patient has returned.

They get a **new token number** at the end of the queue — fair to patients who kept waiting.

No request body required.

**Response 200:** Updated token with `status: "WAITING"` and a new (higher) `tokenNumber`.

| Error | When |
|-------|------|
| `409 Conflict` | Token is not in SKIPPED state |

---

### PATCH /api/v1/clinics/{clinicId}/queue/{tokenId}/cancel — Cancel Token

**Auth:** CLINIC_ADMIN / SUPER_ADMIN

`WAITING / CALLED / SKIPPED → CANCELLED`. Patient left the clinic without being seen.

No request body required.

**Response 200:** Updated token with `status: "CANCELLED"`.

| Error | When |
|-------|------|
| `409 Conflict` | Token is already completed or cancelled |

---

---

## Phase 8 – Billing

**Base path:** `/api/v1/clinics/{clinicId}/billing`

### Invoice lifecycle

```
DRAFT ──issue──► ISSUED ──payment──► PARTIALLY_PAID ──payment──► PAID ──refund──► REFUNDED
  │                │
  └──cancel──►  CANCELLED
               ISSUED can also be cancelled if no payment yet.
```

Invoice numbers are auto-generated per clinic per year: `INV-2026-00001`, `INV-2026-00002`, …

---

### POST /api/v1/clinics/{clinicId}/billing/invoices — Create Invoice

**Auth:** CLINIC_ADMIN / SUPER_ADMIN

Creates a **DRAFT** invoice. Line items are included in the same request.
Invoice number is assigned automatically. An invoice can optionally be linked to an appointment (prevents duplicate billing for the same visit).

**Request body:**

```json
{
  "patientId": 12,
  "doctorId": 3,
  "appointmentId": 55,
  "invoiceDate": "2026-07-05",
  "dueDate": "2026-07-12",
  "items": [
    {
      "treatmentTypeId": 7,
      "description": "General Consultation",
      "quantity": 1,
      "unitPrice": 500.00
    },
    {
      "description": "Blood Pressure Monitoring",
      "quantity": 1,
      "unitPrice": 150.00
    }
  ],
  "discountPercent": 10,
  "taxPercent": 18,
  "notes": "Corporate patient – 10% discount applies"
}
```

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `patientId` | Long | Yes | Must belong to this clinic |
| `doctorId` | Long | Yes | Must belong to this clinic |
| `appointmentId` | Long | No | Links invoice to appointment; rejects CANCELLED/NO_SHOW appointments; prevents duplicate invoices per appointment |
| `invoiceDate` | date | No | Defaults to today |
| `dueDate` | date | No | When payment is due |
| `items` | array | Yes | At least one item required |
| `items[].treatmentTypeId` | Long | No | Optional — for reporting |
| `items[].description` | string | Yes | Display label on the invoice |
| `items[].quantity` | decimal | Yes | Must be > 0 |
| `items[].unitPrice` | decimal | Yes | Must be ≥ 0 |
| `discountPercent` | decimal | No | 0–100; mutually exclusive with `discountAmount` |
| `discountAmount` | decimal | No | Fixed monetary discount; mutually exclusive with `discountPercent` |
| `taxPercent` | decimal | No | GST/VAT %; applied after discount |
| `notes` | string | No | Internal notes |

**How totals are calculated:**

```
subtotal       = Σ (quantity × unitPrice)
discountAmount = subtotal × discountPercent / 100   (or fixed discountAmount)
taxAmount      = (subtotal − discountAmount) × taxPercent / 100
totalAmount    = subtotal − discountAmount + taxAmount
amountDue      = totalAmount   (no payments yet)
```

**Response 201:**

```json
{
  "id": 1,
  "clinicId": 1,
  "patientId": 12,
  "doctorId": 3,
  "appointmentId": 55,
  "patientName": "Ravi Kumar",
  "doctorName": "Dr. Priya Nair",
  "invoiceNumber": "INV-2026-00001",
  "invoiceDate": "2026-07-05",
  "dueDate": "2026-07-12",
  "status": "DRAFT",
  "subtotal": 650.00,
  "discountPercent": 10.00,
  "discountAmount": 65.00,
  "taxPercent": 18.00,
  "taxAmount": 105.30,
  "totalAmount": 690.30,
  "amountPaid": 0.00,
  "amountDue": 690.30,
  "notes": "Corporate patient – 10% discount applies",
  "items": [
    {
      "id": 1,
      "treatmentTypeId": 7,
      "description": "General Consultation",
      "quantity": 1.00,
      "unitPrice": 500.00,
      "totalPrice": 500.00
    },
    {
      "id": 2,
      "description": "Blood Pressure Monitoring",
      "quantity": 1.00,
      "unitPrice": 150.00,
      "totalPrice": 150.00
    }
  ],
  "payments": [],
  "createdAt": "2026-07-05T10:30:00",
  "updatedAt": "2026-07-05T10:30:00"
}
```

| Error | When |
|-------|------|
| `404 Not Found` | Clinic, patient, doctor, or appointment not found |
| `403 Forbidden` | Doctor or patient does not belong to this clinic |
| `409 Conflict` | Appointment is CANCELLED or NO_SHOW, or already has an invoice |
| `400 Bad Request` | Both `discountPercent` and `discountAmount` provided, or discount exceeds subtotal |

---

### GET /api/v1/clinics/{clinicId}/billing/invoices — List Invoices

**Auth:** Any authenticated user

Returns a paginated list of invoices. All query params are optional.

**Query params:**

| Param | Type | Notes |
|-------|------|-------|
| `patientId` | Long | Filter by patient |
| `doctorId` | Long | Filter by doctor |
| `status` | string | `DRAFT`, `ISSUED`, `PAID`, `PARTIALLY_PAID`, `CANCELLED`, `REFUNDED` |
| `fromDate` | date (ISO) | Invoice date ≥ fromDate |
| `toDate` | date (ISO) | Invoice date ≤ toDate |
| `page` | int | Default 0 |
| `size` | int | Default 20 |

**Example:** `GET /api/v1/clinics/1/billing/invoices?status=ISSUED&fromDate=2026-07-01`

**Response 200:**

```json
{
  "content": [
    {
      "id": 1,
      "invoiceNumber": "INV-2026-00001",
      "patientName": "Ravi Kumar",
      "doctorName": "Dr. Priya Nair",
      "invoiceDate": "2026-07-05",
      "status": "ISSUED",
      "totalAmount": 690.30,
      "amountPaid": 0.00,
      "amountDue": 690.30
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

> Line items and payment history are omitted in list responses. Fetch the single endpoint for full detail.

---

### GET /api/v1/clinics/{clinicId}/billing/invoices/{invoiceId} — Get Invoice

**Auth:** Any authenticated user

Returns the full invoice including all line items and payment history.

**Response 200:**

```json
{
  "id": 1,
  "clinicId": 1,
  "patientId": 12,
  "doctorId": 3,
  "appointmentId": 55,
  "patientName": "Ravi Kumar",
  "doctorName": "Dr. Priya Nair",
  "invoiceNumber": "INV-2026-00001",
  "invoiceDate": "2026-07-05",
  "dueDate": "2026-07-12",
  "status": "PARTIALLY_PAID",
  "subtotal": 650.00,
  "discountPercent": 10.00,
  "discountAmount": 65.00,
  "taxPercent": 18.00,
  "taxAmount": 105.30,
  "totalAmount": 690.30,
  "amountPaid": 400.00,
  "amountDue": 290.30,
  "items": [
    {
      "id": 1,
      "treatmentTypeId": 7,
      "description": "General Consultation",
      "quantity": 1.00,
      "unitPrice": 500.00,
      "totalPrice": 500.00
    },
    {
      "id": 2,
      "description": "Blood Pressure Monitoring",
      "quantity": 1.00,
      "unitPrice": 150.00,
      "totalPrice": 150.00
    }
  ],
  "payments": [
    {
      "id": 1,
      "invoiceId": 1,
      "amount": 400.00,
      "paymentMethod": "UPI",
      "paymentDate": "2026-07-05",
      "transactionReference": "TXN89234",
      "createdAt": "2026-07-05T11:00:00"
    }
  ],
  "issuedAt": "2026-07-05T10:45:00",
  "createdAt": "2026-07-05T10:30:00",
  "updatedAt": "2026-07-05T11:00:00"
}
```

| Error | When |
|-------|------|
| `404 Not Found` | Invoice not found for this clinic |

---

### PATCH /api/v1/clinics/{clinicId}/billing/invoices/{invoiceId}/issue — Issue Invoice

**Auth:** CLINIC_ADMIN / SUPER_ADMIN

`DRAFT → ISSUED`. Finalizes the invoice and marks it as presented to the patient.
No further item changes are allowed after issuing.

No request body required.

**Response 200:** Updated invoice with `status: "ISSUED"` and `issuedAt` timestamp.

| Error | When |
|-------|------|
| `404 Not Found` | Invoice not found |
| `409 Conflict` | Invoice is not in DRAFT status |

---

### POST /api/v1/clinics/{clinicId}/billing/invoices/{invoiceId}/payments — Record Payment

**Auth:** CLINIC_ADMIN / SUPER_ADMIN

Records a payment against an **ISSUED** invoice.
- Automatically transitions to `PARTIALLY_PAID` if amount_due > 0 after payment.
- Automatically transitions to `PAID` if amount_due = 0 after payment.
- Multiple payments are allowed (split across UPI, cash, etc.).

**Request body:**

```json
{
  "amount": 400.00,
  "paymentMethod": "UPI",
  "paymentDate": "2026-07-05",
  "transactionReference": "TXN89234",
  "notes": "Patient paid via PhonePe"
}
```

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `amount` | decimal | Yes | Must be > 0 and ≤ `amountDue` |
| `paymentMethod` | string | Yes | `CASH`, `CARD`, `UPI`, `NET_BANKING`, `INSURANCE`, `OTHER` |
| `paymentDate` | date | No | Defaults to today |
| `transactionReference` | string | No | UPI transaction ID, card auth code, cheque number |
| `notes` | string | No | Internal note |

**Response 200:** Full updated invoice with new `amountPaid`, `amountDue`, `status`, and the new entry in `payments[]`.

| Error | When |
|-------|------|
| `404 Not Found` | Invoice not found |
| `409 Conflict` | Invoice is DRAFT (must issue first), CANCELLED, or already PAID |
| `400 Bad Request` | Payment amount exceeds amount due |

---

### PATCH /api/v1/clinics/{clinicId}/billing/invoices/{invoiceId}/cancel — Cancel Invoice

**Auth:** CLINIC_ADMIN / SUPER_ADMIN

`DRAFT / ISSUED → CANCELLED`. Voids the invoice (duplicate, patient not seen, etc.).
Cannot cancel a PAID, PARTIALLY_PAID, REFUNDED, or already CANCELLED invoice.

**Request body (optional):**

```json
{
  "reason": "Patient did not attend — duplicate entry"
}
```

**Response 200:** Updated invoice with `status: "CANCELLED"`, `cancelledAt`, and `cancellationReason`.

| Error | When |
|-------|------|
| `404 Not Found` | Invoice not found |
| `409 Conflict` | Invoice is in a terminal state (PAID, CANCELLED, REFUNDED) |

---

### PATCH /api/v1/clinics/{clinicId}/billing/invoices/{invoiceId}/refund — Refund Invoice

**Auth:** CLINIC_ADMIN / SUPER_ADMIN

`PAID / PARTIALLY_PAID → REFUNDED`. Use when money has been returned to the patient.
This is a status flag only — actual payment reversal happens outside the system (e.g. UPI refund).

No request body required.

**Response 200:** Updated invoice with `status: "REFUNDED"`.

| Error | When |
|-------|------|
| `404 Not Found` | Invoice not found |
| `409 Conflict` | Invoice is not PAID or PARTIALLY_PAID |

---

## Phase 9 – Prescriptions & Medical Records

Clinical documentation created by the doctor during and after a consultation.

### Changelog rows

| Date       | Endpoint                                                   | Change          |
|------------|------------------------------------------------------------|-----------------|
| 2026-07-05 | POST /api/v1/clinics/{cid}/prescriptions                  | Added – Phase 9 |
| 2026-07-05 | PUT /api/v1/clinics/{cid}/prescriptions/{id}              | Added – Phase 9 |
| 2026-07-05 | GET /api/v1/clinics/{cid}/prescriptions/{id}              | Added – Phase 9 |
| 2026-07-05 | GET /api/v1/clinics/{cid}/appointments/{id}/prescription  | Added – Phase 9 |
| 2026-07-05 | GET /api/v1/clinics/{cid}/patients/{id}/prescriptions     | Added – Phase 9 |
| 2026-07-05 | POST /api/v1/clinics/{cid}/appointments/{id}/vitals       | Added – Phase 9 |
| 2026-07-05 | GET /api/v1/clinics/{cid}/appointments/{id}/vitals        | Added – Phase 9 |
| 2026-07-05 | GET /api/v1/clinics/{cid}/patients/{id}/vitals            | Added – Phase 9 |
| 2026-07-05 | POST /api/v1/clinics/{cid}/appointments/{id}/notes        | Added – Phase 9 |
| 2026-07-05 | GET /api/v1/clinics/{cid}/appointments/{id}/notes         | Added – Phase 9 |

---

## Prescriptions

---

### POST /api/v1/clinics/{clinicId}/prescriptions — Create Prescription

**Auth:** CLINIC_ADMIN / SUPER_ADMIN

Creates a prescription with medicines in one request.
Optionally links to an appointment — only one prescription allowed per appointment.
Walk-in prescriptions (no `appointmentId`) are also supported.

**Request body:**

```json
{
  "patientId": 12,
  "doctorId": 3,
  "appointmentId": 55,
  "diagnosis": "Hypertension Stage 1",
  "instructions": "Avoid salt. Exercise 30 min daily. Monitor BP at home.",
  "followUpDate": "2026-08-05",
  "medicines": [
    {
      "medicineName": "Amlodipine",
      "dosage": "5mg",
      "frequency": "Once daily",
      "durationDays": 30,
      "route": "Oral",
      "notes": "Take at bedtime"
    },
    {
      "medicineName": "Telmisartan",
      "dosage": "40mg",
      "frequency": "Once daily in the morning",
      "durationDays": 30,
      "route": "Oral"
    }
  ]
}
```

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `patientId` | Long | Yes | Must belong to this clinic |
| `doctorId` | Long | Yes | Must belong to this clinic |
| `appointmentId` | Long | No | If provided, must belong to this clinic; only one prescription per appointment |
| `diagnosis` | string | No | Free text — primary diagnosis |
| `instructions` | string | No | Patient-facing instructions |
| `followUpDate` | date | No | `yyyy-MM-dd` |
| `medicines` | array | No | Can be empty; added all at once |
| `medicines[].medicineName` | string | Yes | |
| `medicines[].dosage` | string | Yes | e.g. "5mg", "10ml" |
| `medicines[].frequency` | string | Yes | e.g. "Once daily after meals" |
| `medicines[].durationDays` | int | No | |
| `medicines[].route` | string | No | "Oral", "Topical", "IV", etc. |
| `medicines[].notes` | string | No | Extra instructions for this medicine |

**Response 201:**

```json
{
  "id": 1,
  "clinicId": 1,
  "doctorId": 3,
  "patientId": 12,
  "appointmentId": 55,
  "doctorName": "Dr. Priya Nair",
  "patientName": "Ravi Kumar",
  "diagnosis": "Hypertension Stage 1",
  "instructions": "Avoid salt. Exercise 30 min daily. Monitor BP at home.",
  "followUpDate": "2026-08-05",
  "medicines": [
    {
      "id": 1,
      "medicineName": "Amlodipine",
      "dosage": "5mg",
      "frequency": "Once daily",
      "durationDays": 30,
      "route": "Oral",
      "notes": "Take at bedtime"
    },
    {
      "id": 2,
      "medicineName": "Telmisartan",
      "dosage": "40mg",
      "frequency": "Once daily in the morning",
      "durationDays": 30,
      "route": "Oral"
    }
  ],
  "createdAt": "2026-07-05T12:00:00",
  "updatedAt": "2026-07-05T12:00:00"
}
```

| Error | When |
|-------|------|
| `404 Not Found` | Clinic, patient, doctor, or appointment not found |
| `403 Forbidden` | Doctor or patient does not belong to this clinic |
| `409 Conflict` | A prescription already exists for this appointment (use PUT to update) |

---

### PUT /api/v1/clinics/{clinicId}/prescriptions/{prescriptionId} — Update Prescription

**Auth:** CLINIC_ADMIN / SUPER_ADMIN

Updates an existing prescription.
- Sending a `medicines` array **replaces all** existing medicines.
- Omitting `medicines` (null) leaves existing medicines unchanged.
- Sending an empty `medicines: []` clears all medicines.

**Request body:**

```json
{
  "diagnosis": "Hypertension Stage 1 — controlled",
  "instructions": "Continue medications. Reduce sodium intake.",
  "followUpDate": "2026-09-05",
  "medicines": [
    {
      "medicineName": "Amlodipine",
      "dosage": "5mg",
      "frequency": "Once daily",
      "durationDays": 60,
      "route": "Oral"
    }
  ]
}
```

All fields optional. Only provided fields are applied.

**Response 200:** Full updated prescription (same shape as POST 201).

| Error | When |
|-------|------|
| `404 Not Found` | Prescription not found for this clinic |

---

### GET /api/v1/clinics/{clinicId}/prescriptions/{prescriptionId} — Get Prescription

**Auth:** Any authenticated user

Full prescription with medicines list.

**Response 200:** Same shape as POST 201 response.

| Error | When |
|-------|------|
| `404 Not Found` | Prescription not found for this clinic |

---

### GET /api/v1/clinics/{clinicId}/appointments/{appointmentId}/prescription — Get by Appointment

**Auth:** Any authenticated user

Get the prescription written for a specific appointment.

**Response 200:** Same shape as POST 201 response.

| Error | When |
|-------|------|
| `404 Not Found` | No prescription found for this appointment |

---

### GET /api/v1/clinics/{clinicId}/patients/{patientId}/prescriptions — Patient Prescription History

**Auth:** Any authenticated user

Paginated list of all prescriptions for a patient, newest first.

**Query params:** `page` (default 0), `size` (default 20)

**Response 200:**

```json
{
  "content": [
    {
      "id": 1,
      "appointmentId": 55,
      "doctorName": "Dr. Priya Nair",
      "diagnosis": "Hypertension Stage 1",
      "followUpDate": "2026-08-05",
      "medicines": [ ... ],
      "createdAt": "2026-07-05T12:00:00",
      "updatedAt": "2026-07-05T12:00:00"
    }
  ],
  "totalElements": 4,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

| Error | When |
|-------|------|
| `404 Not Found` | Patient not found |

---

## Vital Signs

---

### POST /api/v1/clinics/{clinicId}/appointments/{appointmentId}/vitals — Record Vitals

**Auth:** CLINIC_ADMIN / SUPER_ADMIN

Records vital signs for an appointment (upsert — one record per appointment).
Subsequent calls to this endpoint overwrite the previous values completely.
All fields are optional — record whatever measurements were taken.

**Request body:**

```json
{
  "systolicBp": 145,
  "diastolicBp": 92,
  "pulseBpm": 78,
  "temperatureCelsius": 37.1,
  "weightKg": 72.50,
  "heightCm": 170.00,
  "spo2Percent": 98,
  "notes": "Patient was anxious, recheck BP after 10 min rest"
}
```

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `systolicBp` | int | No | mmHg, 50–300 |
| `diastolicBp` | int | No | mmHg, 30–200 |
| `pulseBpm` | int | No | Beats per minute, 20–300 |
| `temperatureCelsius` | decimal | No | Body temperature |
| `weightKg` | decimal | No | Must be > 0 |
| `heightCm` | decimal | No | Must be > 0 |
| `spo2Percent` | int | No | Blood oxygen saturation, 0–100 |
| `notes` | string | No | Observation notes |

At least one field must be non-null.

**Response 200:**

```json
{
  "id": 1,
  "clinicId": 1,
  "patientId": 12,
  "appointmentId": 55,
  "patientName": "Ravi Kumar",
  "systolicBp": 145,
  "diastolicBp": 92,
  "pulseBpm": 78,
  "temperatureCelsius": 37.1,
  "weightKg": 72.50,
  "heightCm": 170.00,
  "spo2Percent": 98,
  "notes": "Patient was anxious, recheck BP after 10 min rest",
  "recordedAt": "2026-07-05T09:45:00",
  "createdAt": "2026-07-05T09:45:00",
  "updatedAt": "2026-07-05T09:45:00"
}
```

| Error | When |
|-------|------|
| `404 Not Found` | Clinic or appointment not found |
| `400 Bad Request` | All vital fields are null |

---

### GET /api/v1/clinics/{clinicId}/appointments/{appointmentId}/vitals — Get Vitals for Appointment

**Auth:** Any authenticated user

**Response 200:** Same shape as POST 200 response.

| Error | When |
|-------|------|
| `404 Not Found` | No vitals recorded for this appointment |

---

### GET /api/v1/clinics/{clinicId}/patients/{patientId}/vitals — Patient Vitals History

**Auth:** Any authenticated user

Paginated vitals history for a patient — useful for trend charts (BP over time, weight tracking).
Ordered by `recordedAt` descending.

**Query params:** `page` (default 0), `size` (default 20)

**Response 200:**

```json
{
  "content": [
    {
      "id": 3,
      "appointmentId": 72,
      "systolicBp": 138,
      "diastolicBp": 88,
      "pulseBpm": 74,
      "weightKg": 71.80,
      "recordedAt": "2026-07-05T09:45:00"
    },
    {
      "id": 1,
      "appointmentId": 55,
      "systolicBp": 145,
      "diastolicBp": 92,
      "pulseBpm": 78,
      "weightKg": 72.50,
      "recordedAt": "2026-06-01T10:00:00"
    }
  ],
  "totalElements": 2,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

| Error | When |
|-------|------|
| `404 Not Found` | Patient not found |

---

## Clinical Notes (SOAP)

---

### POST /api/v1/clinics/{clinicId}/appointments/{appointmentId}/notes — Save Clinical Note

**Auth:** CLINIC_ADMIN / SUPER_ADMIN

Creates or updates the SOAP note for an appointment (upsert — one note per appointment).
- A **null** field leaves the existing value unchanged.
- An **empty string** (`""`) clears the section.
- First call creates the record; subsequent calls update it in place.

**Request body:**

```json
{
  "subjective": "Patient presents with persistent headache for 3 days. Reports pain score 6/10. No fever.",
  "objective": "BP 145/92 mmHg. Pulse 78 bpm. No papilloedema. Cranial nerves intact.",
  "assessment": "Hypertension Stage 1. Tension headache secondary to elevated BP.",
  "plan": "Start Amlodipine 5mg OD. Advise low-sodium diet. Follow up in 4 weeks. Return immediately if visual disturbance or severe headache."
}
```

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `subjective` | string | No | S — patient's reported symptoms |
| `objective` | string | No | O — examination findings, test results |
| `assessment` | string | No | A — diagnosis / differential |
| `plan` | string | No | P — treatment plan, referrals, follow-up |

**Response 200:**

```json
{
  "id": 1,
  "clinicId": 1,
  "doctorId": 3,
  "patientId": 12,
  "appointmentId": 55,
  "doctorName": "Dr. Priya Nair",
  "patientName": "Ravi Kumar",
  "subjective": "Patient presents with persistent headache for 3 days. Reports pain score 6/10. No fever.",
  "objective": "BP 145/92 mmHg. Pulse 78 bpm. No papilloedema. Cranial nerves intact.",
  "assessment": "Hypertension Stage 1. Tension headache secondary to elevated BP.",
  "plan": "Start Amlodipine 5mg OD. Advise low-sodium diet. Follow up in 4 weeks.",
  "createdAt": "2026-07-05T12:10:00",
  "updatedAt": "2026-07-05T12:10:00"
}
```

| Error | When |
|-------|------|
| `404 Not Found` | Clinic, appointment, or doctor not found |

---

### GET /api/v1/clinics/{clinicId}/appointments/{appointmentId}/notes — Get Clinical Note

**Auth:** Any authenticated user

**Response 200:** Same shape as POST 200 response.

| Error | When |
|-------|------|
| `404 Not Found` | No clinical note found for this appointment |

---

## Phase 10 – Reporting & Analytics

Read-only endpoints that aggregate data across all modules. No new database tables — all metrics are computed from existing data.
All endpoints require **CLINIC_ADMIN or SUPER_ADMIN**.

**Base path:** `/api/v1/clinics/{clinicId}/reports`

**Date range defaults:** `fromDate` defaults to the first day of the current month; `toDate` defaults to today.

**Changelog rows:**

| Date       | Endpoint                                          | Change           |
|------------|---------------------------------------------------|------------------|
| 2026-07-05 | GET /api/v1/clinics/{cid}/reports/revenue         | Added – Phase 10 |
| 2026-07-05 | GET /api/v1/clinics/{cid}/reports/appointments    | Added – Phase 10 |
| 2026-07-05 | GET /api/v1/clinics/{cid}/reports/queue           | Added – Phase 10 |
| 2026-07-05 | GET /api/v1/clinics/{cid}/reports/patients        | Added – Phase 10 |
| 2026-07-05 | GET /api/v1/clinics/{cid}/reports/doctors/{id}    | Added – Phase 10 |

---

### GET /api/v1/clinics/{clinicId}/reports/revenue — Revenue Report

**Auth:** CLINIC_ADMIN / SUPER_ADMIN

Revenue summary for a date range based on invoice dates and payment dates.
CANCELLED and REFUNDED invoices are excluded from `totalInvoiced`/`totalCollected` but REFUNDED invoices are counted separately.

**Query params:**

| Param | Type | Default | Notes |
|-------|------|---------|-------|
| `fromDate` | date (ISO) | First day of current month | Invoice date ≥ fromDate |
| `toDate` | date (ISO) | Today | Invoice date ≤ toDate |

**Example:** `GET /api/v1/clinics/1/reports/revenue?fromDate=2026-07-01&toDate=2026-07-31`

**Response 200:**

```json
{
  "fromDate": "2026-07-01",
  "toDate": "2026-07-31",
  "totalInvoiced": 125000.00,
  "totalCollected": 98000.00,
  "totalOutstanding": 22000.00,
  "totalRefunded": 5000.00,
  "invoiceCount": 45,
  "byDoctor": [
    {
      "doctorId": 3,
      "doctorName": "Dr. Priya Nair",
      "invoiced": 75000.00,
      "collected": 60000.00
    },
    {
      "doctorId": 5,
      "doctorName": "Dr. Arjun Mehta",
      "invoiced": 50000.00,
      "collected": 38000.00
    }
  ],
  "byPaymentMethod": {
    "CASH": 40000.00,
    "UPI": 45000.00,
    "CARD": 13000.00
  }
}
```

| Field | Notes |
|-------|-------|
| `totalInvoiced` | Sum of `totalAmount` for non-cancelled, non-refunded invoices |
| `totalCollected` | Sum of `amountPaid` for the same set |
| `totalOutstanding` | Sum of `amountDue` (unpaid balance) |
| `totalRefunded` | Sum of `totalAmount` for REFUNDED invoices |
| `invoiceCount` | Count of non-cancelled invoices |
| `byDoctor` | Per-doctor revenue breakdown |
| `byPaymentMethod` | Collected amounts grouped by payment method (from payment records) |

| Error | When |
|-------|------|
| `404 Not Found` | Clinic not found |

---

### GET /api/v1/clinics/{clinicId}/reports/appointments — Appointment Report

**Auth:** CLINIC_ADMIN / SUPER_ADMIN

Appointment volume and outcome statistics for a date range.

**Query params:** `fromDate`, `toDate` (same defaults as revenue)

**Response 200:**

```json
{
  "fromDate": "2026-07-01",
  "toDate": "2026-07-31",
  "total": 120,
  "completed": 95,
  "confirmed": 5,
  "pending": 3,
  "inProgress": 0,
  "cancelled": 12,
  "noShow": 5,
  "rescheduled": 0,
  "completionRate": 82.6,
  "byDoctor": [
    {
      "doctorId": 3,
      "doctorName": "Dr. Priya Nair",
      "total": 80,
      "completed": 65,
      "cancelled": 8,
      "noShow": 3
    }
  ]
}
```

| Field | Notes |
|-------|-------|
| `completionRate` | `completed / (total − rescheduled) × 100`; rescheduled excluded because they produce a new appointment |

| Error | When |
|-------|------|
| `404 Not Found` | Clinic not found |

---

### GET /api/v1/clinics/{clinicId}/reports/queue — Queue Report

**Auth:** CLINIC_ADMIN / SUPER_ADMIN

Queue throughput and timing statistics. Wait time and consultation time are computed from actual timestamps on completed tokens.

**Query params:** `fromDate`, `toDate` (same defaults as revenue)

**Response 200:**

```json
{
  "fromDate": "2026-07-01",
  "toDate": "2026-07-31",
  "totalTokens": 110,
  "completed": 95,
  "skipped": 8,
  "cancelled": 7,
  "waiting": 0,
  "called": 0,
  "inProgress": 0,
  "averageWaitMinutes": 23.4,
  "averageConsultationMinutes": 13.8,
  "skipRate": 7.3,
  "byDoctor": [
    {
      "doctorId": 3,
      "doctorName": "Dr. Priya Nair",
      "total": 75,
      "completed": 65,
      "skipped": 5,
      "avgWaitMinutes": 21.0,
      "avgConsultationMinutes": 12.5
    }
  ]
}
```

| Field | Notes |
|-------|-------|
| `averageWaitMinutes` | Avg minutes from token creation → being called (only COMPLETED tokens with all timestamps present) |
| `averageConsultationMinutes` | Avg minutes from consultation start → completion |
| `skipRate` | `skipped / totalTokens × 100` |

| Error | When |
|-------|------|
| `404 Not Found` | Clinic not found |

---

### GET /api/v1/clinics/{clinicId}/reports/patients — Patient Report

**Auth:** CLINIC_ADMIN / SUPER_ADMIN

Patient registration and demographics snapshot.

**Query params:** `fromDate`, `toDate` — used only for `newRegistrations` count. `totalActive` and `byGender` reflect all-time data.

**Response 200:**

```json
{
  "fromDate": "2026-07-01",
  "toDate": "2026-07-31",
  "totalActive": 340,
  "newRegistrations": 28,
  "byGender": {
    "MALE": 145,
    "FEMALE": 182,
    "OTHER": 8,
    "PREFER_NOT_TO_SAY": 5,
    "UNKNOWN": 0
  }
}
```

| Field | Notes |
|-------|-------|
| `totalActive` | All non-deleted, active patients in the clinic (all time) |
| `newRegistrations` | Patients first registered within the date range |
| `byGender` | All-time gender distribution; `UNKNOWN` = patients with no gender recorded |

| Error | When |
|-------|------|
| `404 Not Found` | Clinic not found |

---

### GET /api/v1/clinics/{clinicId}/reports/doctors/{doctorId} — Doctor Performance

**Auth:** CLINIC_ADMIN / SUPER_ADMIN

Combined performance summary for a single doctor — appointments, revenue, queue timing, and top treatments.

**Query params:** `fromDate`, `toDate` (same defaults as revenue)

**Response 200:**

```json
{
  "doctorId": 3,
  "doctorName": "Dr. Priya Nair",
  "specialization": "Cardiologist",
  "fromDate": "2026-07-01",
  "toDate": "2026-07-31",
  "appointmentsTotal": 80,
  "appointmentsCompleted": 65,
  "appointmentsCancelled": 8,
  "appointmentsNoShow": 3,
  "revenueGenerated": 75000.00,
  "revenueCollected": 60000.00,
  "tokensCompleted": 65,
  "averageWaitMinutes": 21.0,
  "averageConsultationMinutes": 12.5,
  "topTreatments": [
    {
      "treatmentTypeId": 7,
      "treatmentName": "General Consultation",
      "count": 45
    },
    {
      "treatmentTypeId": 12,
      "treatmentName": "ECG",
      "count": 18
    }
  ]
}
```

| Field | Notes |
|-------|-------|
| `tokensCompleted` | Completed queue tokens (may differ from `appointmentsCompleted` for walk-ins) |
| `averageWaitMinutes` | Computed from queue timestamps for this doctor |
| `topTreatments` | Top 5 treatments by completed appointment count |

| Error | When |
|-------|------|
| `404 Not Found` | Clinic or doctor not found |
| `403 Forbidden` | Doctor does not belong to this clinic |

---

## Phase 11 – Notifications

Outbound SMS and email alerts triggered by key events. All notifications are non-fatal — a failure never rolls back the main operation. Every dispatch attempt (SENT / FAILED / SKIPPED) is persisted in `notification_logs`.

**Migration:** `V10__notification_schema.sql`
**Tables:** `notification_logs`, `notification_preferences`

### Architecture

- `NotificationProvider` interface — two stub implementations: `SmsNotificationProvider` (`@Component("smsProvider")`) and `EmailNotificationProvider` (`@Component("emailProvider")`). Replace the body with your real provider (Twilio, SendGrid, etc.) without touching the rest of the pipeline.
- `NotificationService` — called by `AppointmentService`, `QueueService`, and `BillingService` after state changes.
- `NotificationScheduler` — `@Scheduled(cron = "0 0 8 * * *")` runs every day at 08:00 and sends 24h reminders for tomorrow's PENDING/CONFIRMED appointments.
- Per-clinic preferences (`notification_preferences`) — 14 boolean flags with sensible defaults (all enabled except `paymentReceivedSms`). Row is lazily created on first use.

### Notification events

| Event | Channels |
|-------|---------|
| Appointment booked | SMS + Email |
| Appointment confirmed | SMS + Email |
| Appointment reminder (24h before) | SMS + Email |
| Appointment cancelled | SMS + Email |
| Appointment rescheduled | SMS + Email |
| Token called | SMS only |
| Invoice issued | Email only |
| Payment received | SMS + Email |

### Auth

All endpoints require `CLINIC_ADMIN` or `SUPER_ADMIN`.

---

### GET /api/v1/clinics/{clinicId}/notifications/logs

Paginated audit log of all notification dispatch attempts for a clinic.

**Query parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `type` | enum | Filter by type: `APPOINTMENT_BOOKED`, `APPOINTMENT_CONFIRMED`, `APPOINTMENT_REMINDER`, `APPOINTMENT_CANCELLED`, `APPOINTMENT_RESCHEDULED`, `TOKEN_CALLED`, `INVOICE_ISSUED`, `PAYMENT_RECEIVED` |
| `channel` | enum | `SMS` or `EMAIL` |
| `status` | enum | `SENT`, `FAILED`, `SKIPPED` |
| `from` | ISO datetime | Lower bound on `createdAt` |
| `to` | ISO datetime | Upper bound on `createdAt` |
| `page` | int | 0-based page index (default: 0) |
| `size` | int | Page size (default: 20) |

**Example request:**
```
GET /api/v1/clinics/1/notifications/logs?channel=SMS&status=FAILED&size=10
Authorization: Bearer <token>
```

**Example response (200 OK):**
```json
{
  "content": [
    {
      "id": 42,
      "clinicId": 1,
      "type": "APPOINTMENT_BOOKED",
      "channel": "SMS",
      "recipient": "+919876543210",
      "message": "Hi Ravi, your appointment with Dr. Priya Nair is confirmed for 10 Jul 2026 at 10:00 AM. Thank you!",
      "status": "FAILED",
      "errorReason": "Provider returned false",
      "referenceId": 101,
      "referenceType": "APPOINTMENT",
      "createdAt": "2026-07-10T09:45:00"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 10,
  "number": 0
}
```

---

### GET /api/v1/clinics/{clinicId}/notifications/preferences

Returns the clinic's current notification preference flags. If no preferences exist yet, a row with all defaults is created.

**Example request:**
```
GET /api/v1/clinics/1/notifications/preferences
Authorization: Bearer <token>
```

**Example response (200 OK):**
```json
{
  "clinicId": 1,
  "appointmentBookedSms": true,
  "appointmentBookedEmail": true,
  "appointmentReminderSms": true,
  "appointmentReminderEmail": true,
  "appointmentConfirmedSms": true,
  "appointmentConfirmedEmail": true,
  "appointmentCancelledSms": true,
  "appointmentCancelledEmail": true,
  "appointmentRescheduledSms": true,
  "appointmentRescheduledEmail": true,
  "tokenCalledSms": true,
  "invoiceIssuedEmail": true,
  "paymentReceivedSms": false,
  "paymentReceivedEmail": true
}
```

---

### PUT /api/v1/clinics/{clinicId}/notifications/preferences

Patch-style update — only fields provided (non-null) are changed. Omit a field to leave it unchanged.

**Example request:**
```
PUT /api/v1/clinics/1/notifications/preferences
Authorization: Bearer <token>
Content-Type: application/json

{
  "paymentReceivedSms": true,
  "appointmentReminderEmail": false
}
```

**Example response (200 OK):**
```json
{
  "clinicId": 1,
  "appointmentBookedSms": true,
  "appointmentBookedEmail": true,
  "appointmentReminderSms": true,
  "appointmentReminderEmail": false,
  "appointmentConfirmedSms": true,
  "appointmentConfirmedEmail": true,
  "appointmentCancelledSms": true,
  "appointmentCancelledEmail": true,
  "appointmentRescheduledSms": true,
  "appointmentRescheduledEmail": true,
  "tokenCalledSms": true,
  "invoiceIssuedEmail": true,
  "paymentReceivedSms": true,
  "paymentReceivedEmail": true
}
```

---

## Phase 12 – Patient Portal

Patients can create their own login accounts and view their own data without needing a clinic admin role.

**Migration:** `V11__patient_portal_schema.sql`
**New role:** `PATIENT` (seeded into `roles` table)
**New columns:** `patients.sms_opt_out`, `patients.email_opt_out`

### Architecture

- `POST /api/v1/auth/patient/register` — creates a `PATIENT` user account linked to an existing patient record (looked up by clinic + phone number)
- All `/api/v1/me/**` endpoints require a PATIENT JWT. The patient record is resolved from `userId` in the token — no `patientId` in the path, so spoofing is impossible.
- Patient opt-out flags (`smsOptOut`, `emailOptOut`) are checked by `NotificationService` before dispatching.

### Auth

All `/me` endpoints require `PATIENT` role (obtained by calling `POST /auth/patient/register` then `POST /auth/login`).

---

### POST /api/v1/auth/patient/register

Registers a portal account for an existing patient. The patient must already be in the clinic system (added by staff). The account is immediately usable — no email verification step (clinic staff already verified identity at registration).

**Request body:**
```json
{
  "clinicId": 1,
  "phone": "+919876543210",
  "email": "ravi@example.com",
  "password": "mypassword"
}
```

**Response (201 Created):** Same `AuthResponse` as `POST /auth/login` — includes `accessToken`, `refreshToken`, `expiresIn`.

**Errors:**
- `404` — no patient found with that phone in that clinic
- `409` — patient already has a portal account
- `409` — email already registered

---

### GET /api/v1/me/profile

Returns the authenticated patient's own profile (no internal notes or admin-only fields).

**Example request:**
```
GET /api/v1/me/profile
Authorization: Bearer <patient-token>
```

**Example response (200 OK):**
```json
{
  "id": 42,
  "clinicId": 1,
  "firstName": "Ravi",
  "lastName": "Kumar",
  "phone": "+919876543210",
  "email": "ravi@example.com",
  "dateOfBirth": "1990-05-15",
  "gender": "MALE",
  "bloodGroup": "O+",
  "allergies": "Penicillin",
  "chronicConditions": "Diabetes Type 2",
  "currentMedications": "Metformin 500mg",
  "address": "12 MG Road, Bangalore",
  "smsOptOut": false,
  "emailOptOut": false
}
```

---

### GET /api/v1/me/appointments

Patient's own appointment history across all dates, most recent first. Paginated.

**Query parameters:** `page`, `size`, `sort` (standard Spring Pageable)

**Example request:**
```
GET /api/v1/me/appointments?page=0&size=10
Authorization: Bearer <patient-token>
```

**Example response (200 OK):**
```json
{
  "content": [
    {
      "id": 101,
      "clinicId": 1,
      "doctorId": 5,
      "doctorName": "Dr. Priya Nair",
      "appointmentDate": "2026-07-10",
      "startTime": "10:00",
      "endTime": "10:20",
      "durationMins": 20,
      "status": "CONFIRMED",
      "reason": "Follow-up check"
    }
  ],
  "totalElements": 5,
  "totalPages": 1,
  "size": 10,
  "number": 0
}
```

---

### GET /api/v1/me/prescriptions

Patient's own prescription history, most recent first. Paginated. Includes medicines.

**Example request:**
```
GET /api/v1/me/prescriptions?page=0&size=10
Authorization: Bearer <patient-token>
```

**Example response (200 OK):**
```json
{
  "content": [
    {
      "id": 7,
      "clinicId": 1,
      "patientId": 42,
      "doctorId": 5,
      "diagnosis": "Type 2 Diabetes follow-up",
      "medicines": [
        {
          "medicineName": "Metformin",
          "dosage": "500mg",
          "frequency": "Twice daily",
          "durationDays": 30,
          "instructions": "Take after meals"
        }
      ],
      "createdAt": "2026-07-10T11:00:00"
    }
  ],
  "totalElements": 3,
  "totalPages": 1,
  "size": 10,
  "number": 0
}
```

---

### GET /api/v1/me/invoices

Patient's own invoice history for their clinic, most recent first. Paginated.

**Example request:**
```
GET /api/v1/me/invoices?page=0&size=10
Authorization: Bearer <patient-token>
```

**Example response (200 OK):**
```json
{
  "content": [
    {
      "id": 15,
      "invoiceNumber": "INV-2026-00015",
      "invoiceDate": "2026-07-10",
      "totalAmount": 1200.00,
      "amountPaid": 1200.00,
      "amountDue": 0.00,
      "status": "PAID"
    }
  ],
  "totalElements": 2,
  "totalPages": 1,
  "size": 10,
  "number": 0
}
```

---

### PUT /api/v1/me/notification-preferences

Opt in or out of SMS and email notifications. Null fields are ignored (patch semantics).

**Example request:**
```
PUT /api/v1/me/notification-preferences
Authorization: Bearer <patient-token>
Content-Type: application/json

{
  "smsOptOut": true,
  "emailOptOut": false
}
```

**Example response (200 OK):** Returns updated patient profile (same as `GET /me/profile`), with `smsOptOut: true, emailOptOut: false` reflected.

---

## Phase 13 – Multi-Tenant SaaS & Subscription Billing

Phase 13 adds a subscription and billing layer so ClinicOS can operate as a SaaS platform where each clinic pays a monthly plan.

### Plans

| Tier       | Max Doctors | Max Patients/Month | Price/Month |
|------------|-------------|--------------------|-------------|
| FREE       | 1           | 50                 | ₹0          |
| PRO        | 10          | Unlimited          | ₹2,999      |
| ENTERPRISE | Unlimited   | Unlimited          | ₹9,999      |

New clinics are automatically assigned the FREE plan.

---

### GET /api/v1/plans

List all active subscription plans. Public — no authentication required.

**Request**

```
GET /api/v1/plans
```

**Response 200**

```json
[
  {
    "id": 1,
    "tier": "FREE",
    "displayName": "Free Plan",
    "maxDoctors": 1,
    "maxPatientsPerMonth": 50,
    "priceMonthly": 0.00
  },
  {
    "id": 2,
    "tier": "PRO",
    "displayName": "Pro Plan",
    "maxDoctors": 10,
    "priceMonthly": 2999.00
  },
  {
    "id": 3,
    "tier": "ENTERPRISE",
    "displayName": "Enterprise Plan",
    "priceMonthly": 9999.00
  }
]
```

> Note: `maxDoctors` and `maxPatientsPerMonth` are omitted when `null` (unlimited), per `@JsonInclude(NON_NULL)`.

---

### GET /api/v1/clinics/{clinicId}/subscription

Get the current subscription for a clinic. Auto-assigns FREE plan if no subscription exists.

**Auth:** `CLINIC_ADMIN` or `SUPER_ADMIN`

**Response 200**

```json
{
  "id": 1,
  "clinicId": 42,
  "plan": {
    "id": 1,
    "tier": "FREE",
    "displayName": "Free Plan",
    "maxDoctors": 1,
    "maxPatientsPerMonth": 50,
    "priceMonthly": 0.00
  },
  "status": "ACTIVE",
  "startedAt": "2026-07-05T10:30:00",
  "nextBillingDate": "2026-08-05"
}
```

---

### POST /api/v1/clinics/{clinicId}/subscription

Subscribe or upgrade a clinic to a plan.

**Auth:** `CLINIC_ADMIN` or `SUPER_ADMIN`

**Request Body**

```json
{
  "tier": "PRO"
}
```

| Field | Type     | Required | Description                        |
|-------|----------|----------|------------------------------------|
| tier  | enum     | Yes      | `FREE`, `PRO`, or `ENTERPRISE`     |

**Response 200** — same as GET subscription above, with updated plan details.

**Errors**

| Status | Scenario                      |
|--------|-------------------------------|
| 400    | `tier` is null or invalid     |
| 404    | Plan tier not found           |

---

### GET /api/v1/admin/clinics

SUPER_ADMIN: paginated list of all clinics with their subscription status.

**Auth:** `SUPER_ADMIN`

**Query Params:** `page`, `size`, `sort` (Spring Pageable)

**Response 200**

```json
{
  "content": [
    {
      "id": 42,
      "name": "Apollo Dental",
      "slug": "apollo-dental",
      "city": "Mumbai",
      "email": "admin@apollo.com",
      "deleted": false,
      "planTier": "PRO",
      "subscriptionStatus": "ACTIVE",
      "subscribedAt": "2026-07-01T09:00:00"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

---

### GET /api/v1/admin/revenue

SUPER_ADMIN: platform-level revenue dashboard showing MRR breakdown by plan.

**Auth:** `SUPER_ADMIN`

**Response 200**

```json
{
  "totalActiveClinics": 15,
  "monthlyRecurringRevenue": 44985.00,
  "activeClinicsByPlan": {
    "FREE": 10,
    "PRO": 4,
    "ENTERPRISE": 1
  },
  "revenueByPlan": {
    "FREE": 0.00,
    "PRO": 11996.00,
    "ENTERPRISE": 9999.00
  }
}
```

---

### Usage Enforcement

When creating a **doctor** (`POST /api/v1/clinics/{cid}/doctors`):
- If the clinic is at its plan's doctor limit → `402 Payment Required`
- If the clinic is at ≥ 80% of its limit → warning logged (no error)

When registering a **patient** (`POST /api/v1/clinics/{cid}/patients`):
- If the clinic hit its monthly patient limit → `402 Payment Required`
- If the clinic is at ≥ 80% of its monthly limit → warning logged (no error)

**402 Error Example**

```json
{
  "type": "about:blank",
  "title": "Payment Required",
  "status": 402,
  "detail": "Doctor limit reached for your FREE plan (1 doctors). Please upgrade to add more doctors.",
  "instance": "/api/v1/clinics/42/doctors"
}
```

---

## Phase 14 – Audit Logs & Compliance

Every CREATE, UPDATE, and DELETE on clinical entities is now recorded in an immutable audit log. The log captures the actor, the IP address, and a full JSON snapshot of the entity before and after the change.

**Entities audited:** CLINIC, DOCTOR, PATIENT, APPOINTMENT, INVOICE, PRESCRIPTION

---

### GET /api/v1/clinics/{clinicId}/audit-logs

Query the audit trail for a clinic. Only `CLINIC_ADMIN` (own clinic) and `SUPER_ADMIN` can access.

**Auth:** Bearer token — `CLINIC_ADMIN` or `SUPER_ADMIN`

**Path params**

| Param | Type | Description |
|-------|------|-------------|
| `clinicId` | Long | Clinic to query |

**Query params** (all optional)

| Param | Type | Example | Description |
|-------|------|---------|-------------|
| `entityType` | String | `PATIENT` | Filter by entity type (CLINIC, DOCTOR, PATIENT, APPOINTMENT, INVOICE, PRESCRIPTION) |
| `action` | String | `UPDATE` | Filter by action: CREATE, UPDATE, DELETE |
| `changedBy` | Long | `3` | Filter by user ID of the actor |
| `from` | ISO DateTime | `2025-01-01T00:00:00` | Start of time range (inclusive) |
| `to` | ISO DateTime | `2025-12-31T23:59:59` | End of time range (inclusive) |
| `page` | int | `0` | Page number (default 0) |
| `size` | int | `20` | Page size (default 20) |

**Response 200**

```json
{
  "content": [
    {
      "id": 1,
      "clinicId": 5,
      "entityType": "PATIENT",
      "entityId": 42,
      "action": "UPDATE",
      "changedBy": 3,
      "beforeState": {
        "id": 42,
        "firstName": "Rahul",
        "phone": "9876543210"
      },
      "afterState": {
        "id": 42,
        "firstName": "Rahul",
        "phone": "9000000001"
      },
      "ipAddress": "192.168.1.10",
      "createdAt": "2025-06-15T10:32:00"
    }
  ],
  "totalElements": 120,
  "totalPages": 6,
  "size": 20,
  "number": 0
}
```

**Notes**
- `beforeState` is `null` for CREATE actions (no prior state)
- `afterState` is `null` for DELETE actions
- `beforeState`/`afterState` are embedded JSON objects, not escaped strings
- Results are ordered by `createdAt DESC` by default (use `sort` Pageable param to change)
- Audit writes are non-fatal — a failed audit write never blocks the business operation

---

---

## Phase 15 – OpenAPI 3.0 / Swagger UI (Live Interactive Docs)

**Goal:** Replace the static `api.md` with auto-generated, always-up-to-date interactive API documentation that any developer or interviewer can open in a browser and test directly without Postman.

**Why this matters:** Every production API ships with a live Swagger UI. Without it, a reviewer cannot explore your API without reading markdown. With it, the project is self-documenting.

### What gets added

**Dependency:** `springdoc-openapi-starter-webmvc-ui` — single dependency, zero XML config.

**Global API metadata** (`OpenApiConfig.java`):
```java
@Bean
public OpenAPI clinicosOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("ClinicOS API")
            .version("1.0.0")
            .description("Multi-tenant clinic management platform — Appointment, Queue, Billing, EMR, Subscriptions"))
        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
        .components(new Components()
            .addSecuritySchemes("bearerAuth",
                new SecurityScheme().type(HTTP).scheme("bearer").bearerFormat("JWT")));
}
```

**Controller-level annotations** (added to all 16 controllers):
```java
@Tag(name = "Appointments", description = "Book, reschedule, cancel, and track appointments")
@Operation(summary = "Book appointment", description = "Runs 8-step validation before creating the slot")
@ApiResponse(responseCode = "201", description = "Appointment booked")
@ApiResponse(responseCode = "409", description = "Slot conflict or doctor unavailable")
```

**DTO-level schema annotations**:
```java
@Schema(description = "Appointment date — cannot be in the past", example = "2026-08-15")
private LocalDate appointmentDate;

@Schema(description = "Start time — must be on 10-minute boundary", example = "09:30")
private LocalTime startTime;
```

**Accessible at:** `GET /swagger-ui/index.html` — no auth required (public endpoint).

**Grouped by tag in Swagger UI:**
- Auth, Clinics, Doctors, Patients, Appointments, Queue, Billing, Prescriptions, Vitals, Clinical Notes, Notifications, Audit Logs, Patient Portal, Subscriptions, Platform Admin, Reports

### New files
- `src/main/java/com/prakash/clinicos/config/OpenApiConfig.java`
- Annotations added to all controller and DTO files

### application.properties additions
```properties
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.operationsSorter=method
springdoc.swagger-ui.tagsSorter=alpha
springdoc.swagger-ui.tryItOutEnabled=true
```

---

## Phase 16 – Test Suite (Unit + Integration + Coverage Gate)

**Goal:** Prove the system works. A codebase with no tests signals "this was built to demo, not to ship." This phase makes the project shippable by covering the most critical, non-obvious business logic.

**Why this matters:** Interviewers look for tests. The availability algorithm, double-booking prevention, and subscription enforcement are exactly the kind of logic tests prove. JaCoCo enforces that coverage never drops below the threshold.

### Test structure

```
src/test/java/com/prakash/clinicos/
├── availability/
│   └── DoctorAvailabilityServiceTest.java       ← 20+ unit tests
├── appointment/
│   └── AppointmentServiceIntegrationTest.java   ← Testcontainers + real DB
├── billing/
│   └── BillingServiceTest.java                  ← Invoice math, discount/tax
├── subscription/
│   └── SubscriptionServiceTest.java             ← Plan limit enforcement
├── auth/
│   └── AuthServiceTest.java                     ← Registration, JWT, refresh
├── audit/
│   └── AuditServiceTest.java                    ← Non-fatal failure guarantee
└── common/
    └── AbstractIntegrationTest.java             ← Testcontainers base class
```

### DoctorAvailabilityServiceTest — the crown jewel

```java
@ExtendWith(MockitoExtension.class)
class DoctorAvailabilityServiceTest {

    // Scenario 1: Doctor has no schedule → unavailable
    @Test void doctorWithNoSchedule_isUnavailable() { ... }

    // Scenario 2: Doctor has leave on requested date → unavailable
    @Test void doctorOnLeave_isUnavailable() { ... }

    // Scenario 3: Clinic is emergency closed → unavailable
    @Test void clinicEmergencyClosed_makesAllDoctorsUnavailable() { ... }

    // Scenario 4: Break window correctly removes slots
    @Test void breakWindow_removesCorrectSlotsFromList() { ... }

    // Scenario 5: Existing appointment blocks those slots
    @Test void existingAppointment_removesOccupiedSlotsFromAvailability() { ... }

    // Scenario 6: Day override overrides weekly schedule
    @Test void dayOverride_takesOverWeeklySchedule() { ... }

    // Scenario 7: Clinic closure date → unavailable regardless of doctor schedule
    @Test void clinicClosureDate_makesClinicUnavailable() { ... }

    // Scenario 8: A 30-min appointment blocks 3 consecutive 10-min slots
    @Test void durationAwareBooking_blocks3Slots_for30MinTreatment() { ... }
}
```

### Integration test with Testcontainers

```java
@SpringBootTest
@Testcontainers
class AppointmentServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("clinicos_test");

    // Tests actual DB writes, Flyway migrations, transactional rollback
    @Test void bookAppointment_persists_andBlocksSlot() { ... }
    @Test void concurrentBooking_onlySingleBookingSucceeds() { ... }  // race condition proof
    @Test void reschedule_createsNewRecord_andMarksOldAsRescheduled() { ... }
}
```

### Billing math tests

```java
class BillingServiceTest {
    @Test void discount_percent_appliesBeforeTax() { ... }
    @Test void discount_cannotExceedSubtotal_throws400() { ... }
    @Test void tax_appliedOnDiscountedAmount_notSubtotal() { ... }
    @Test void invoiceNumber_incrementsPerClinicPerYear() { ... }
    @Test void addPayment_exceedingAmountDue_throws400() { ... }
    @Test void fullPayment_setsStatusToPaid() { ... }
}
```

### JaCoCo coverage gate (pom.xml)

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <executions>
        <execution>
            <goals><goal>check</goal></goals>
            <configuration>
                <rules>
                    <rule>
                        <limits>
                            <!-- Build fails if service layer drops below 80% -->
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.80</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

**Coverage report generated at:** `target/site/jacoco/index.html`

### Dependencies added to pom.xml
```xml
<!-- Testcontainers for real Postgres in tests -->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
```

---

## Phase 17 – Docker & Local Development Environment

**Goal:** Anyone — an interviewer, a collaborator, a recruiter with a laptop — can clone the repo and run the entire system with a single command. No "install Postgres, create database, set env vars" setup ritual.

**Why this matters:** Without this, the project only runs on your machine. With it, it's a real deployable artifact.

### Dockerfile (multi-stage build)

```dockerfile
# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN ./mvnw dependency:resolve -q
COPY src ./src
RUN ./mvnw package -DskipTests -q

# Stage 2: Runtime — lean image, no JDK
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S clinicos && adduser -S clinicos -G clinicos
USER clinicos
COPY --from=builder /app/target/clinicos-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Why multi-stage:** The final image contains only the JRE and the JAR — no Maven, no source code, no JDK. Image size drops from ~700MB to ~180MB.

### docker-compose.yml

```yaml
version: '3.9'

services:
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: clinicos
      POSTGRES_USER: clinicos_user
      POSTGRES_PASSWORD: clinicos_pass
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U clinicos_user -d clinicos"]
      interval: 10s
      retries: 5

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s

  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/clinicos
      SPRING_DATASOURCE_USERNAME: clinicos_user
      SPRING_DATASOURCE_PASSWORD: clinicos_pass
      SPRING_DATA_REDIS_HOST: redis
      JWT_SECRET: ${JWT_SECRET}
      TWILIO_ACCOUNT_SID: ${TWILIO_ACCOUNT_SID}
      TWILIO_AUTH_TOKEN: ${TWILIO_AUTH_TOKEN}
      OPENROUTER_API_KEY: ${OPENROUTER_API_KEY}
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_healthy

volumes:
  postgres_data:
```

### .env.example (committed — actual .env is gitignored)

```bash
JWT_SECRET=replace_with_64_char_secret
TWILIO_ACCOUNT_SID=ACxxxxxxxxxxxxxxxxx
TWILIO_AUTH_TOKEN=xxxxxxxxxxxxxxx
OPENROUTER_API_KEY=sk-or-xxxxxxxxxx
RAZORPAY_KEY_ID=rzp_test_xxxxxxxxxx
RAZORPAY_KEY_SECRET=xxxxxxxxxx
```

### Makefile (developer ergonomics)

```makefile
up:       ## Start all services
    docker compose up -d

down:     ## Stop all services
    docker compose down

logs:     ## Tail app logs
    docker compose logs -f app

test:     ## Run tests with coverage
    ./mvnw test jacoco:report

build:    ## Build production Docker image
    docker build -t clinicos:latest .

db-shell: ## Open Postgres shell
    docker compose exec postgres psql -U clinicos_user -d clinicos
```

**Single command to run:** `cp .env.example .env && make up`

---

## Phase 18 – CI/CD Pipeline with GitHub Actions

**Goal:** Every push to `main` automatically runs tests, checks coverage, builds the Docker image, and deploys to production. This is the professional development lifecycle that every engineering team uses.

**Why this matters:** A repo with a green CI badge signals discipline. It means the code on `main` is always tested and always deployable.

### .github/workflows/ci.yml

```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  test:
    name: Test & Coverage
    runs-on: ubuntu-latest

    services:
      postgres:
        image: postgres:16
        env:
          POSTGRES_DB: clinicos_test
          POSTGRES_USER: clinicos_user
          POSTGRES_PASSWORD: clinicos_pass
        ports:
          - 5432:5432
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-retries 5

    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: maven

      - name: Run tests
        run: ./mvnw verify jacoco:report
        env:
          SPRING_DATASOURCE_URL: jdbc:postgresql://localhost:5432/clinicos_test
          SPRING_DATASOURCE_USERNAME: clinicos_user
          SPRING_DATASOURCE_PASSWORD: clinicos_pass

      - name: Upload coverage to Codecov
        uses: codecov/codecov-action@v4
        with:
          file: target/site/jacoco/jacoco.xml

      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: test-results
          path: target/surefire-reports/

  build-and-push:
    name: Build & Push Docker Image
    runs-on: ubuntu-latest
    needs: test
    if: github.ref == 'refs/heads/main'

    steps:
      - uses: actions/checkout@v4

      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@v3

      - name: Login to Docker Hub
        uses: docker/login-action@v3
        with:
          username: ${{ secrets.DOCKER_USERNAME }}
          password: ${{ secrets.DOCKER_TOKEN }}

      - name: Build and push
        uses: docker/build-push-action@v5
        with:
          push: true
          tags: |
            prakashjha/clinicos:latest
            prakashjha/clinicos:${{ github.sha }}
          cache-from: type=gha
          cache-to: type=gha,mode=max

  deploy:
    name: Deploy to Production
    runs-on: ubuntu-latest
    needs: build-and-push
    if: github.ref == 'refs/heads/main'

    steps:
      - name: Deploy to Railway
        run: |
          curl -X POST ${{ secrets.RAILWAY_DEPLOY_WEBHOOK }}
```

### Branch protection rules (configured in GitHub Settings)

- `main` requires: passing CI, 1 reviewer approval, no force push
- PRs must be up to date before merging
- Status checks required: `Test & Coverage`, `Build & Push Docker Image`

### Result

Every PR shows: ✅ Tests passed | ✅ Coverage 83% | ✅ Docker build successful — before a single line merges to main.

---

## Phase 19 – Redis Caching Layer

**Goal:** Cache the most expensive read operations — especially the availability algorithm which hits 7 tables — so repeated reads within the same day don't hammer the database. Redis also enables distributed rate limiting and session sharing if the app scales horizontally.

**Why this matters:** In an interview, this lets you discuss distributed caching, cache invalidation strategies, and horizontal scaling. Those are senior-level topics.

### What gets cached

| Cache name | Key | TTL | Evicted when |
|-----------|-----|-----|-------------|
| `availability` | `{doctorId}:{date}` | 5 min | Appointment booked/cancelled, doctor schedule changed, doctor leave updated |
| `clinic` | `{clinicId}` | 10 min | Clinic updated, business hours changed |
| `doctor` | `{doctorId}` | 10 min | Doctor updated, schedule changed |
| `plans` | `all` | 1 hour | Plan data updated (rarely changes) |

### Configuration

```java
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        return RedisCacheManager.builder(factory)
            .withCacheConfiguration("availability",
                RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofMinutes(5))
                    .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer())))
            .withCacheConfiguration("clinic",
                RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(10)))
            .withCacheConfiguration("doctor",
                RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(10)))
            .withCacheConfiguration("plans",
                RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(1)))
            .build();
    }
}
```

### Caching the availability algorithm

```java
// DoctorAvailabilityService.java
@Cacheable(value = "availability", key = "#doctorId + ':' + #date")
public DoctorAvailabilityResponse computeAvailability(Long doctorId, LocalDate date) {
    // The 7-table query only runs on cache miss
    ...
}

// Cache evicted when a booking is made — AppointmentService.bookAppointment()
@CacheEvict(value = "availability", key = "#req.doctorId + ':' + #req.appointmentDate")
public AppointmentResponse bookAppointment(...) { ... }

// Cache evicted when schedule changes — DoctorScheduleService
@CacheEvict(value = "availability", allEntries = true)  // doctor schedule changed → all dates invalid
public void updateDoctorSchedule(...) { ... }
```

### Rate limiting with Redis (new)

```java
// RateLimitFilter.java — applied to auth endpoints
// 10 login attempts per IP per minute, then 429 Too Many Requests
// Uses Redis INCR + EXPIRE for atomic sliding window counter
```

### application.properties additions

```properties
spring.data.redis.host=${REDIS_HOST:localhost}
spring.data.redis.port=${REDIS_PORT:6379}
spring.cache.type=redis
```

---

## Phase 20 – Real Notification Providers (Twilio SMS + SendGrid Email)

**Goal:** Replace the stub notification providers (which only write to logs) with real integrations. After this phase, a patient actually receives an SMS when their appointment is booked and an email when their invoice is issued.

**Why this matters:** You can say "I integrated Twilio for transactional SMS" — not "I have a stub." The notification preference system, opt-out logic, and failure-safe design are already implemented; this phase wires in the real API calls.

### SMS — Twilio integration

```java
// TwilioSmsProvider.java — replaces SmsNotificationProvider stub
@Service
@Qualifier("smsProvider")
@Slf4j
public class TwilioSmsProvider implements NotificationProvider {

    private final TwilioRestClient client;
    private final String fromNumber;

    public TwilioSmsProvider(
            @Value("${twilio.account-sid}") String accountSid,
            @Value("${twilio.auth-token}") String authToken,
            @Value("${twilio.from-number}") String fromNumber) {
        this.client = new TwilioRestClient.Builder(accountSid, authToken).build();
        this.fromNumber = fromNumber;
    }

    @Override
    public void send(String to, String message) {
        Message.creator(new PhoneNumber(to), new PhoneNumber(fromNumber), message)
               .create(client);
    }
}
```

### Email — SendGrid integration

```java
// SendGridEmailProvider.java — replaces EmailNotificationProvider stub
@Service
@Qualifier("emailProvider")
public class SendGridEmailProvider implements NotificationProvider {

    private final SendGrid sendGrid;

    @Override
    public void send(String to, String subject, String htmlBody) {
        Email from = new Email("noreply@clinicos.in", "ClinicOS");
        Email toEmail = new Email(to);
        Content content = new Content("text/html", htmlBody);
        Mail mail = new Mail(from, subject, toEmail, content);
        sendGrid.api(new Request().method(Method.POST).endpoint("mail/send").body(mail.build()));
    }
}
```

### Email templates (Thymeleaf HTML)

Templates stored in `src/main/resources/templates/email/`:

| Template | Trigger |
|----------|---------|
| `appointment-booked.html` | Patient books appointment |
| `appointment-reminder.html` | 24-hour reminder scheduler |
| `appointment-confirmed.html` | Clinic confirms appointment |
| `appointment-cancelled.html` | Appointment cancelled |
| `invoice-issued.html` | Invoice issued to patient |
| `payment-received.html` | Payment recorded |
| `password-reset.html` | Forgot password flow |
| `email-verify.html` | New account registration |

**Sample template (`appointment-booked.html`):**
```html
<html>
<body>
  <h2>Appointment Confirmed, <span th:text="${patientName}">Patient</span>!</h2>
  <p>Your appointment has been booked at <strong th:text="${clinicName}">Clinic</strong>.</p>
  <table>
    <tr><td>Doctor</td><td th:text="${doctorName}">Dr. Name</td></tr>
    <tr><td>Date</td><td th:text="${appointmentDate}">Date</td></tr>
    <tr><td>Time</td><td th:text="${startTime}">Time</td></tr>
  </table>
  <p>Reply STOP to this SMS to opt out of future notifications.</p>
</body>
</html>
```

### application.properties additions

```properties
twilio.account-sid=${TWILIO_ACCOUNT_SID}
twilio.auth-token=${TWILIO_AUTH_TOKEN}
twilio.from-number=${TWILIO_FROM_NUMBER:+15005550006}

sendgrid.api-key=${SENDGRID_API_KEY}
```

---

## Phase 21 – WebSocket Real-Time Queue Board

**Goal:** The receptionist's queue screen updates live — when a token moves from WAITING to CALLED, every browser on the clinic's queue board sees it instantly without refreshing. Built with STOMP over WebSocket and Spring's native `@MessageMapping`.

**Why this matters:** This requires understanding a fundamentally different communication model (full-duplex, stateful) versus REST (stateless, request-response). It's a senior-level topic that demonstrates protocol breadth.

### Architecture

```
Browser (SockJS client)
    ↕  WebSocket upgrade
Spring WebSocket (STOMP broker)
    ↕  in-memory message broker
QueueService.callToken()
    → messagingTemplate.convertAndSend("/topic/queue/{clinicId}/{doctorId}", event)
```

### Configuration

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");   // subscribe prefix — queue updates
        config.setApplicationDestinationPrefixes("/app");  // send prefix — client → server
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();  // fallback for browsers without native WebSocket
    }
}
```

### Event model

```java
// QueueEvent.java — the payload pushed to subscribers
@Data
@Builder
public class QueueEvent {
    private Long tokenId;
    private Integer tokenNumber;
    private String patientName;
    private QueueStatus oldStatus;
    private QueueStatus newStatus;
    private Long doctorId;
    private String doctorName;
    private LocalDateTime eventTime;
    private String message; // "Token 12 — Rahul Sharma is now being called"
}
```

### QueueService push integration

```java
// QueueService.java — modified callToken()
public QueueTokenResponse callToken(Long clinicId, Long doctorId, UserPrincipal principal) {
    // ... existing business logic ...

    QueueEvent event = QueueEvent.builder()
        .tokenId(token.getId())
        .tokenNumber(token.getTokenNumber())
        .newStatus(QueueStatus.CALLED)
        .message("Token " + token.getTokenNumber() + " — " + patientName + " please proceed")
        .eventTime(LocalDateTime.now())
        .build();

    // Push to all subscribers watching this clinic/doctor queue
    messagingTemplate.convertAndSend(
        "/topic/queue/" + clinicId + "/" + doctorId, event);

    return toResponse(token);
}
```

### Client subscription (JavaScript — for the queue board)

```javascript
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function() {
    // Subscribe to this clinic's queue for doctor 3
    stompClient.subscribe('/topic/queue/5/3', function(frame) {
        const event = JSON.parse(frame.body);
        displayQueueUpdate(event); // Updates the TV screen in the waiting room
    });
});
```

### WebSocket endpoints

| Destination | Direction | Description |
|------------|-----------|-------------|
| `/topic/queue/{clinicId}/{doctorId}` | Server → Client (push) | Queue status changes in real time |
| `/topic/queue/{clinicId}/stats` | Server → Client (push) | Live wait count, avg wait time |
| `/app/queue/{clinicId}/subscribe` | Client → Server | Client registers interest |

---

## Phase 22 – Payment Gateway (Razorpay Subscription Billing)

**Goal:** The subscription plan upgrade flow currently stores plan data but has no payment step. This phase adds real payment collection via Razorpay — the standard Indian payment gateway — so clicking "Upgrade to PRO" charges the clinic owner's card and activates the plan on webhook confirmation.

**Why this matters:** This makes the SaaS business model real. You can describe an end-to-end payment flow in an interview: order creation → payment → webhook verification → plan activation → idempotency handling.

### Payment flow

```
1. POST /api/v1/clinics/{cid}/subscription/checkout
   → SubscriptionController calls RazorpayService.createOrder(planId)
   → Razorpay creates an order (order_id returned to frontend)

2. Frontend opens Razorpay checkout modal with order_id
   → Patient pays via card/UPI/netbanking

3. Razorpay calls POST /api/v1/webhooks/razorpay
   → ClinicOS verifies HMAC signature
   → On payment.captured event → activates subscription
   → Returns 200 to Razorpay (prevents retry)

4. GET /api/v1/clinics/{cid}/subscription
   → Returns ACTIVE status with new plan
```

### Webhook signature verification (security-critical)

```java
// RazorpayWebhookController.java
@PostMapping("/api/v1/webhooks/razorpay")
public ResponseEntity<Void> handleWebhook(
        @RequestBody String payload,
        @RequestHeader("X-Razorpay-Signature") String signature) {

    // HMAC-SHA256 verification — rejects forged webhooks
    String expectedSignature = HmacUtils.hmacSha256Hex(razorpayWebhookSecret, payload);
    if (!MessageDigest.isEqual(
            expectedSignature.getBytes(),
            signature.getBytes())) {
        return ResponseEntity.status(403).build();
    }

    WebhookEvent event = objectMapper.readValue(payload, WebhookEvent.class);

    if ("payment.captured".equals(event.getEvent())) {
        subscriptionService.activateSubscription(
            event.getPayload().getPayment().getOrderId(),
            event.getPayload().getPayment().getId()
        );
    }

    return ResponseEntity.ok().build();
}
```

### New endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/api/v1/clinics/{cid}/subscription/checkout` | `CLINIC_ADMIN` | Create Razorpay order for plan upgrade |
| `GET` | `/api/v1/clinics/{cid}/subscription/payment-history` | `CLINIC_ADMIN` | Subscription payment records |
| `POST` | `/api/v1/webhooks/razorpay` | None (HMAC verified) | Razorpay event receiver |

### New DB migration — V14__subscription_payments.sql

```sql
CREATE TABLE subscription_payments (
    id              BIGSERIAL PRIMARY KEY,
    clinic_id       BIGINT NOT NULL REFERENCES clinics(id),
    plan_id         BIGINT NOT NULL REFERENCES plans(id),
    razorpay_order_id   VARCHAR(100) UNIQUE NOT NULL,
    razorpay_payment_id VARCHAR(100),
    amount_paise    BIGINT NOT NULL,   -- stored in paise (₹2999 = 299900)
    currency        VARCHAR(3) NOT NULL DEFAULT 'INR',
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',  -- PENDING, CAPTURED, FAILED, REFUNDED
    captured_at     TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);
```

### Idempotency guarantee

Webhook events are idempotent: `razorpay_payment_id` is stored with `UNIQUE` constraint. A duplicate webhook for the same payment silently returns 200 without re-activating.

---

## Phase 23 – Agentic AI with OpenRouter

**Goal:** Embed a multi-tool AI agent into ClinicOS that acts as a clinical intelligence layer. The agent can converse in natural language, call internal system tools (query patient history, check availability, summarize records), and produce structured outputs. Built using OpenRouter's API (which provides access to Claude, GPT-4, Gemini, and open-source models behind a single endpoint).

**Why this matters:** This is the highest-signal differentiator in the entire project. Barely any portfolio projects implement a true agentic loop with tool-calling. This demonstrates LLM integration, tool design, async processing, and prompt engineering simultaneously.

### What the agent can do

| Capability | Example prompt | What happens |
|-----------|---------------|-------------|
| Appointment assistant | "Book Rahul for a dental cleaning next Tuesday morning" | Agent checks availability, finds slot, confirms with user, calls book API |
| Patient summary | "Summarize the last 6 months of patient 42's history for Dr. Meera" | Agent fetches prescriptions + vitals + appointments, sends to LLM, returns clinical summary |
| Queue optimizer | "How long will token 15 wait today?" | Agent pulls queue stats, avg consultation time, calls LLM for estimate |
| Symptom triage | "Patient reports chest pain and shortness of breath — which doctor should they see?" | LLM reasons over available doctors and specializations |
| Billing query | "What's the outstanding amount for clinic 5 this month?" | Agent calls reporting API, formats natural-language answer |
| Report insight | "Which doctor has the highest no-show rate?" | Agent fetches doctor performance report, LLM interprets the data |

### Architecture — Agentic loop

```
User message
    ↓
POST /api/v1/ai/chat  (AiController)
    ↓
AiAgentService.chat(sessionId, message)
    ↓
┌─────────────────────────────────────────────┐
│  AGENT LOOP (max 5 iterations)               │
│                                              │
│  1. Send message + tool definitions to LLM   │
│  2. LLM responds with:                        │
│     a. Final answer → return to user          │
│     b. Tool call → execute → back to step 1  │
└─────────────────────────────────────────────┘
```

### OpenRouter API integration

```java
@Service
@Slf4j
public class OpenRouterClient {

    private final WebClient webClient;

    @Value("${openrouter.api-key}")
    private String apiKey;

    @Value("${openrouter.model:anthropic/claude-3.5-sonnet}")
    private String model;

    public OpenRouterClient() {
        this.webClient = WebClient.builder()
            .baseUrl("https://openrouter.ai/api/v1")
            .defaultHeader("Authorization", "Bearer " + apiKey)
            .defaultHeader("HTTP-Referer", "https://clinicos.in")
            .defaultHeader("X-Title", "ClinicOS")
            .build();
    }

    public ChatResponse chat(ChatRequest request) {
        return webClient.post()
            .uri("/chat/completions")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(ChatResponse.class)
            .block();
    }
}
```

### Tool definitions (what the agent can call)

```java
// Tools are defined as JSON Schema — the LLM decides which to call
List<Tool> CLINICOS_TOOLS = List.of(

    Tool.function("get_patient_history",
        "Get a patient's appointments, prescriptions, and vitals",
        schema(Map.of(
            "clinicId", longParam("Clinic ID"),
            "patientId", longParam("Patient ID"),
            "months",    intParam("How many months back to look (default 6)")
        ))),

    Tool.function("get_doctor_availability",
        "Check what appointment slots are available for a doctor on a given date",
        schema(Map.of(
            "clinicId",  longParam("Clinic ID"),
            "doctorId",  longParam("Doctor ID"),
            "date",      stringParam("Date in YYYY-MM-DD format")
        ))),

    Tool.function("get_queue_status",
        "Get the current queue state for a doctor today",
        schema(Map.of(
            "clinicId", longParam("Clinic ID"),
            "doctorId", longParam("Doctor ID")
        ))),

    Tool.function("get_revenue_report",
        "Get the revenue summary for a clinic in a date range",
        schema(Map.of(
            "clinicId",  longParam("Clinic ID"),
            "fromDate",  stringParam("Start date YYYY-MM-DD"),
            "toDate",    stringParam("End date YYYY-MM-DD")
        ))),

    Tool.function("get_doctor_performance",
        "Get appointment completion rate, revenue, and top treatments for each doctor",
        schema(Map.of(
            "clinicId",  longParam("Clinic ID"),
            "fromDate",  stringParam("Start date"),
            "toDate",    stringParam("End date")
        )))
);
```

### Tool execution — ToolDispatcher

```java
@Component
public class ToolDispatcher {

    // Injects existing services — agent reuses all existing business logic
    private final AppointmentService appointmentService;
    private final DoctorAvailabilityService availabilityService;
    private final QueueService queueService;
    private final ReportingService reportingService;
    private final PatientService patientService;

    public Object dispatch(String toolName, Map<String, Object> args) {
        return switch (toolName) {
            case "get_patient_history"    -> patientService.getPatientHistory(args);
            case "get_doctor_availability" -> availabilityService.computeAvailability(
                                                toLong(args, "doctorId"),
                                                toDate(args, "date"));
            case "get_queue_status"       -> queueService.getQueueStatus(
                                                toLong(args, "clinicId"),
                                                toLong(args, "doctorId"));
            case "get_revenue_report"     -> reportingService.revenueReport(args);
            case "get_doctor_performance" -> reportingService.doctorPerformanceReport(args);
            default -> throw new AppException(HttpStatus.BAD_REQUEST,
                                              "Unknown tool: " + toolName);
        };
    }
}
```

### Conversation memory — Redis-backed sessions

```java
// Each chat session maintains message history in Redis (TTL: 30 min)
// Key: "ai:session:{sessionId}"
// Value: List<ChatMessage> serialized as JSON
// Allows multi-turn conversations: "tell me more about that patient"
```

### New endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/api/v1/ai/chat` | `CLINIC_ADMIN` | Send a message to the AI agent |
| `GET` | `/api/v1/ai/chat/{sessionId}/history` | `CLINIC_ADMIN` | Retrieve conversation history |
| `DELETE` | `/api/v1/ai/chat/{sessionId}` | `CLINIC_ADMIN` | Clear session |
| `POST` | `/api/v1/ai/summarize/patient/{patientId}` | `CLINIC_ADMIN` | One-shot patient summary |
| `POST` | `/api/v1/ai/summarize/report` | `CLINIC_ADMIN` | Natural-language report summary |

**Request:**
```json
POST /api/v1/ai/chat
{
  "sessionId": "sess_abc123",
  "clinicId": 5,
  "message": "Which of my doctors had the most no-shows last month?"
}
```

**Response:**
```json
{
  "sessionId": "sess_abc123",
  "reply": "Dr. Arjun Mehta had the highest no-show rate last month at 18.4% (9 out of 49 appointments). The clinic average was 11.2%. Would you like me to look at which day of the week or time slot has the most no-shows for Dr. Mehta?",
  "toolsUsed": ["get_doctor_performance"],
  "model": "anthropic/claude-3.5-sonnet",
  "tokensUsed": 847,
  "responseTimeMs": 1240
}
```

### application.properties additions

```properties
openrouter.api-key=${OPENROUTER_API_KEY}
openrouter.model=anthropic/claude-3.5-sonnet
openrouter.max-iterations=5
openrouter.session-ttl-minutes=30
```

---

## Phase 24 – COBOL Financial Calculation Engine

**Goal:** Integrate a COBOL program as the authoritative calculation engine for invoice financial arithmetic — subtotal, discount, tax, and total — replacing the Java `BigDecimal` logic with a compiled COBOL binary invoked via `ProcessBuilder`. This demonstrates knowledge of legacy system integration, a real-world enterprise pattern used in banking, insurance, and healthcare billing systems.

**Why this matters:** COBOL processes over $3 trillion in daily commerce. Healthcare billing systems in large hospitals (especially in the US and Europe) still run COBOL. Demonstrating that you can bridge a modern Java microservice with a COBOL financial engine shows enterprise-grade thinking that virtually no other portfolio project touches. In an interview, this becomes an instant conversation piece.

### What COBOL handles

All monetary arithmetic in invoice creation is delegated to the COBOL engine:

```
INPUT  → subtotal, discount_percent, discount_amount, tax_percent
OUTPUT → calculated_discount, calculated_tax, total_amount, validation_error
```

COBOL's `COMP-3` (packed decimal) arithmetic eliminates IEEE-754 floating point rounding errors — the same reason financial institutions trust it for 60+ years.

### The COBOL program — `CLINBILL.cbl`

```cobol
       IDENTIFICATION DIVISION.
       PROGRAM-ID. CLINBILL.
       AUTHOR. CLINICOS-SYSTEM.

      *---------------------------------------------------------------
      * ClinicOS Billing Engine
      * Purpose: Authoritative financial calculation for invoices
      * Uses COMP-3 (packed decimal) for exact monetary arithmetic
      *---------------------------------------------------------------

       ENVIRONMENT DIVISION.

       DATA DIVISION.
       WORKING-STORAGE SECTION.

       01 WS-INPUT.
          05 WS-SUBTOTAL         PIC 9(10)V99 COMP-3.
          05 WS-DISCOUNT-PCT     PIC 9(3)V99  COMP-3.
          05 WS-DISCOUNT-AMT     PIC 9(10)V99 COMP-3.
          05 WS-TAX-PCT          PIC 9(3)V99  COMP-3.
          05 WS-DISCOUNT-MODE    PIC X(1).
             88 USE-PERCENT      VALUE 'P'.
             88 USE-AMOUNT       VALUE 'A'.

       01 WS-OUTPUT.
          05 WS-CALC-DISCOUNT    PIC 9(10)V99 COMP-3.
          05 WS-TAXABLE-AMOUNT   PIC 9(10)V99 COMP-3.
          05 WS-CALC-TAX         PIC 9(10)V99 COMP-3.
          05 WS-TOTAL            PIC 9(10)V99 COMP-3.
          05 WS-RETURN-CODE      PIC 9(2) COMP-3.
             88 CALC-SUCCESS     VALUE 0.
             88 DISCOUNT-EXCEEDS VALUE 1.
             88 INVALID-INPUT    VALUE 2.

       PROCEDURE DIVISION.
       MAIN-PARA.
           MOVE 0 TO WS-RETURN-CODE

           IF WS-SUBTOTAL <= 0
               MOVE 2 TO WS-RETURN-CODE
               STOP RUN
           END-IF

           IF USE-PERCENT
               COMPUTE WS-CALC-DISCOUNT ROUNDED =
                   WS-SUBTOTAL * WS-DISCOUNT-PCT / 100
           ELSE
               MOVE WS-DISCOUNT-AMT TO WS-CALC-DISCOUNT
           END-IF

           IF WS-CALC-DISCOUNT > WS-SUBTOTAL
               MOVE 1 TO WS-RETURN-CODE
               STOP RUN
           END-IF

           COMPUTE WS-TAXABLE-AMOUNT =
               WS-SUBTOTAL - WS-CALC-DISCOUNT

           COMPUTE WS-CALC-TAX ROUNDED =
               WS-TAXABLE-AMOUNT * WS-TAX-PCT / 100

           COMPUTE WS-TOTAL =
               WS-TAXABLE-AMOUNT + WS-CALC-TAX

           MOVE 0 TO WS-RETURN-CODE
           STOP RUN.
```

### Java bridge — `CobolBillingEngine.java`

```java
@Service
@Slf4j
public class CobolBillingEngine {

    @Value("${cobol.binary-path:./cobol/CLINBILL}")
    private String cobolBinaryPath;

    @Value("${cobol.enabled:true}")
    private boolean enabled;

    /**
     * Delegates invoice financial calculation to the COBOL binary.
     * Falls back to Java BigDecimal calculation if COBOL binary unavailable.
     */
    public BillingCalculation calculate(BigDecimal subtotal,
                                         BigDecimal discountPercent,
                                         BigDecimal discountAmount,
                                         BigDecimal taxPercent) {
        if (!enabled || !Files.exists(Path.of(cobolBinaryPath))) {
            log.warn("COBOL engine unavailable — using Java fallback");
            return calculateJavaFallback(subtotal, discountPercent, discountAmount, taxPercent);
        }

        try {
            // Pass parameters as space-delimited string to COBOL via stdin
            // Format: SUBTOTAL DISCOUNT_PCT DISCOUNT_AMT TAX_PCT MODE(P|A)
            String input = String.format("%s %s %s %s %s",
                subtotal.toPlainString(),
                discountPercent.toPlainString(),
                discountAmount.toPlainString(),
                taxPercent.toPlainString(),
                discountPercent.compareTo(BigDecimal.ZERO) > 0 ? "P" : "A");

            ProcessBuilder pb = new ProcessBuilder(cobolBinaryPath)
                .redirectErrorStream(true);

            Process process = pb.start();

            try (var writer = new BufferedWriter(
                    new OutputStreamWriter(process.getOutputStream()))) {
                writer.write(input);
            }

            String output = new String(process.getInputStream().readAllBytes());
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                log.error("COBOL engine returned exit code {}: {}", exitCode, output);
                return calculateJavaFallback(subtotal, discountPercent, discountAmount, taxPercent);
            }

            return parseCobilOutput(output);

        } catch (Exception ex) {
            log.error("COBOL engine invocation failed: {}", ex.getMessage());
            return calculateJavaFallback(subtotal, discountPercent, discountAmount, taxPercent);
        }
    }
}
```

### Docker build — COBOL compilation step

```dockerfile
# COBOL compilation stage — added to Dockerfile
FROM gnucobol:3.2 AS cobol-builder
WORKDIR /cobol
COPY src/main/cobol/CLINBILL.cbl .
RUN cobc -x -o CLINBILL CLINBILL.cbl   # Compile to native executable

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=cobol-builder /cobol/CLINBILL ./cobol/CLINBILL
COPY --from=builder /app/target/clinicos-*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Fallback design

The COBOL engine has a **graceful Java fallback**: if the binary is missing, fails to start, or returns a non-zero exit code, `BillingService` silently falls back to `BigDecimal` arithmetic and logs a warning. The invoice still gets created — COBOL failure is never user-visible.

### New files
- `src/main/cobol/CLINBILL.cbl` — The COBOL program
- `src/main/java/com/prakash/clinicos/billing/cobol/CobolBillingEngine.java`
- `src/main/java/com/prakash/clinicos/billing/cobol/BillingCalculation.java` (record)

### application.properties additions

```properties
cobol.enabled=true
cobol.binary-path=./cobol/CLINBILL
```

---

## Phase 25 – Observability & Monitoring (Actuator + Prometheus + Grafana)

**Goal:** Make the system's internal health, performance, and business metrics visible at runtime. This phase adds Spring Boot Actuator health endpoints, Micrometer metrics export to Prometheus, and a pre-built Grafana dashboard showing API latency, error rates, JVM health, cache hit rates, and business KPIs like appointments booked per minute.

**Why this matters:** An application nobody can observe is an application nobody trusts. In a production system (and in interviews), observability is non-negotiable. Being able to describe your Grafana dashboard — p99 latency, cache hit ratio, JVM heap — is a senior-level signal.

### Spring Boot Actuator endpoints

| Endpoint | Description |
|----------|-------------|
| `GET /actuator/health` | Liveness + readiness (used by Docker, Kubernetes, Railway) |
| `GET /actuator/health/db` | PostgreSQL connectivity check |
| `GET /actuator/health/redis` | Redis connectivity check |
| `GET /actuator/metrics` | All registered metrics |
| `GET /actuator/prometheus` | Prometheus-format scrape endpoint |
| `GET /actuator/info` | App version, git commit SHA |

### Custom business metrics (Micrometer)

```java
// MetricsService.java — records business events as Prometheus metrics
@Service
public class MetricsService {

    private final MeterRegistry registry;

    // Counters — "how many times has X happened?"
    public void appointmentBooked(Long clinicId, String doctorName) {
        registry.counter("clinicos.appointments.booked",
            "clinic_id", clinicId.toString(),
            "doctor", doctorName).increment();
    }

    public void invoiceIssued(Long clinicId, BigDecimal amount) {
        registry.counter("clinicos.invoices.issued",
            "clinic_id", clinicId.toString()).increment();
        registry.gauge("clinicos.revenue.latest",
            List.of(Tag.of("clinic_id", clinicId.toString())),
            amount, BigDecimal::doubleValue);
    }

    // Timers — "how long does X take?"
    public Timer availabilityTimer() {
        return registry.timer("clinicos.availability.compute.duration");
    }

    // Gauge — "what is the current value of X?"
    public void queueDepth(Long clinicId, Long doctorId, int depth) {
        registry.gauge("clinicos.queue.depth",
            List.of(Tag.of("clinic_id", clinicId.toString()),
                    Tag.of("doctor_id", doctorId.toString())),
            depth);
    }
}
```

### DoctorAvailabilityService — timed

```java
@Cacheable(value = "availability", key = "#doctorId + ':' + #date")
public DoctorAvailabilityResponse computeAvailability(Long doctorId, LocalDate date) {
    return metricsService.availabilityTimer().record(() -> {
        // ... existing algorithm ...
    });
}
```

### Grafana dashboard panels

Pre-built dashboard JSON committed at `monitoring/grafana/clinicos-dashboard.json`:

| Panel | Metric |
|-------|--------|
| API Request Rate | `rate(http_server_requests_seconds_count[5m])` |
| API p99 Latency | `histogram_quantile(0.99, http_server_requests_seconds_bucket)` |
| Error Rate | `rate(http_server_requests_seconds_count{status=~"5.."}[5m])` |
| Appointments/min | `rate(clinicos.appointments.booked[1m])` |
| Queue Depth | `clinicos.queue.depth` per doctor |
| Availability Cache Hit Rate | `cache_gets_total{result="hit",name="availability"}` |
| JVM Heap Used | `jvm_memory_used_bytes{area="heap"}` |
| DB Connection Pool | `hikaricp_connections_active` |
| COBOL Engine Invocations | `clinicos.cobol.invocations_total` |

### docker-compose additions (monitoring stack)

```yaml
  prometheus:
    image: prom/prometheus:latest
    volumes:
      - ./monitoring/prometheus.yml:/etc/prometheus/prometheus.yml
    ports:
      - "9090:9090"

  grafana:
    image: grafana/grafana:latest
    ports:
      - "3000:3000"
    environment:
      GF_SECURITY_ADMIN_PASSWORD: admin
    volumes:
      - ./monitoring/grafana:/etc/grafana/provisioning
```

**Run:** `make up` → Grafana at `http://localhost:3000`, Prometheus at `http://localhost:9090`

---

## Phase 26 – Production Cloud Deployment (Railway + AWS Reference)

**Goal:** The project has a live, publicly accessible URL that anyone can hit with Postman. Not "it runs on my laptop" — a real deployment with environment-injected secrets, a managed PostgreSQL instance, Redis, and a CI/CD pipeline that deploys automatically on push to `main`.

**Why this matters:** There is no substitute for a live URL in an interview. "Here is the Swagger UI, hit any endpoint" ends the credibility gap immediately.

### Option A — Railway (fastest, free tier available)

Railway auto-detects the `Dockerfile`, provisions managed Postgres and Redis, and injects environment variables from its dashboard.

**Steps:**
1. Push repo to GitHub
2. Connect GitHub repo to Railway (`railway link`)
3. Add services: PostgreSQL plugin, Redis plugin
4. Set environment variables in Railway dashboard (copy from `.env.example`)
5. Railway sets `DATABASE_URL` and `REDIS_URL` automatically — override in `application.properties`:

```properties
spring.datasource.url=${DATABASE_URL}
spring.data.redis.url=${REDIS_URL}
```

6. Every `git push main` triggers a redeploy (CI runs first via GitHub Actions, deploy webhook fires on success)

**Live URL pattern:** `https://clinicos-production.up.railway.app`
**Swagger UI:** `https://clinicos-production.up.railway.app/swagger-ui/index.html`

### Option B — AWS reference architecture (describe in interviews)

```
Route 53 (DNS)
    ↓
Application Load Balancer (HTTPS, ACM cert)
    ↓
ECS Fargate (clinicos container, 2 tasks min)
    ↓
RDS PostgreSQL (Multi-AZ, t3.micro for dev)
ElastiCache Redis (cache.t3.micro)

Secrets: AWS Secrets Manager → injected as env vars at task startup
Logs: CloudWatch Logs (log group /clinicos/app)
CI: GitHub Actions → ECR push → ECS rolling deploy
```

**IaC:** `infra/terraform/` — Terraform modules for ECS, RDS, ElastiCache, ALB, VPC, Route 53.

### Health check configuration (for load balancer and Railway)

```java
// HealthController.java
@RestController
public class HealthController {
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "version", "1.0.0"));
    }
}
```

```properties
# Actuator readiness for Kubernetes / ECS
management.endpoint.health.probes.enabled=true
management.health.livenessState.enabled=true
management.health.readinessState.enabled=true
```

### Environment variable reference (production checklist)

| Variable | Source | Required |
|----------|--------|----------|
| `SPRING_DATASOURCE_URL` | Railway Postgres / RDS | Yes |
| `SPRING_DATASOURCE_USERNAME` | Secrets Manager | Yes |
| `SPRING_DATASOURCE_PASSWORD` | Secrets Manager | Yes |
| `SPRING_DATA_REDIS_URL` | Railway Redis / ElastiCache | Yes |
| `JWT_SECRET` | Secrets Manager | Yes (64+ chars) |
| `TWILIO_ACCOUNT_SID` | Secrets Manager | Yes (Phase 20) |
| `TWILIO_AUTH_TOKEN` | Secrets Manager | Yes (Phase 20) |
| `TWILIO_FROM_NUMBER` | Secrets Manager | Yes (Phase 20) |
| `SENDGRID_API_KEY` | Secrets Manager | Yes (Phase 20) |
| `OPENROUTER_API_KEY` | Secrets Manager | Yes (Phase 23) |
| `RAZORPAY_KEY_ID` | Secrets Manager | Yes (Phase 22) |
| `RAZORPAY_KEY_SECRET` | Secrets Manager | Yes (Phase 22) |
| `COBOL_ENABLED` | Env dashboard | Optional (default true) |

### SSL / HTTPS

Railway provisions a free TLS certificate automatically. On AWS, ACM (Certificate Manager) issues free certificates attached to the ALB.

**The project is now:**
- Live at a public HTTPS URL
- Auto-deploying on every push to `main` that passes CI
- Swagger UI accessible to anyone without running anything locally
- Monitoring visible on Grafana
- All secrets externalized — no credentials in the repository

---

## Roadmap Summary

| Phase | What | Status |
|-------|------|--------|
| 1–14 | Core platform (Auth, Clinics, Doctors, Patients, Appointments, Queue, Billing, Medical Records, Notifications, Patient Portal, Subscriptions, Audit) | ✅ Complete |
| 15 | OpenAPI 3.0 / Swagger UI | ✅ Complete |         ./mvnw spring-boot:run     http://localhost:8080/swagger-ui/index.html
| 16 | Test suite (JUnit 5 + Testcontainers + JaCoCo) | ⬜ Upcoming |
| 17 | Docker + docker-compose + Makefile | ⬜ Upcoming |
| 18 | CI/CD with GitHub Actions | ⬜ Upcoming |
| 19 | Redis caching (availability + rate limiting) | ⬜ Upcoming |
| 20 | Real notification providers (Twilio SMS + SendGrid Email) | ⬜ Upcoming |
| 21 | WebSocket real-time queue board (STOMP) | ⬜ Upcoming |
| 22 | Razorpay subscription payment gateway | ⬜ Upcoming |
| 23 | Agentic AI with OpenRouter (multi-tool clinical agent) | ⬜ Upcoming |
| 24 | COBOL financial calculation engine | ⬜ Upcoming |
| 25 | Observability (Actuator + Prometheus + Grafana) | ⬜ Upcoming |
| 26 | Production deployment (Railway + AWS reference) | ⬜ Upcoming |
