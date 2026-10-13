# StayMatch — API Contract Documentation

> **Base URL:** `http://localhost:8080`
> **Content-Type:** `application/json`
> **Date:** April 14, 2026

---

## Table of Contents

1. [Common Conventions](#1-common-conventions)
2. [Auth APIs](#2-auth-apis)
   - [POST /api/v1/auth/register](#21-register)
   - [POST /api/v1/auth/login](#22-login)
   - [POST /api/v1/auth/refresh](#23-refresh-token)
3. [PG APIs](#3-pg-apis)
   - [GET /api/v1/pgs/search](#31-search-pgs)
   - [GET /api/v1/pgs/{id}](#32-get-pg-by-id)
   - [POST /api/v1/pgs](#33-create-pg)
   - [PUT /api/v1/pgs/{id}](#34-update-pg)
   - [DELETE /api/v1/pgs/{id}](#35-delete-pg)
4. [Tenant APIs](#4-tenant-apis)
   - [GET /api/v1/tenant-posts/search](#41-search-tenant-posts)
   - [POST /api/v1/tenant-posts](#42-create-tenant-post)
5. [Error Response Reference](#5-error-response-reference)

---

## 1. Common Conventions

### Authentication Header

All protected endpoints require a JWT Bearer token:

```
Authorization: Bearer <access_token>
```

### Roles

| Role        | Prefix (internal) | Description                    |
|-------------|-------------------|--------------------------------|
| `PG_OWNER`  | `ROLE_PG_OWNER`   | Can create/update/delete PGs   |
| `TENANT`    | `ROLE_TENANT`     | Can create tenant posts        |
| `ADMIN`     | `ROLE_ADMIN`      | Full access to all resources   |

### Standard Error Response Structure

Every error returns the same JSON envelope:

```json
{
  "timestamp": "2026-04-14T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Descriptive error message",
  "validationErrors": null
}
```

When validation fails, `validationErrors` is a map of `field → message`:

```json
{
  "timestamp": "2026-04-14T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "validationErrors": {
    "email": "Email must be valid",
    "password": "Password must be 8-100 characters"
  }
}
```

### Pagination (PG Search)

PG search results are paginated. Pagination params are query parameters:

| Parameter | Type    | Default | Description        |
|-----------|---------|---------|--------------------|
| `page`    | Integer | `0`     | Zero-based page    |
| `size`    | Integer | `20`    | Items per page     |
| `sort`    | String  | —       | e.g. `name,asc`    |

---

## 2. Auth APIs

### 2.1 Register

Creates a new user account and returns access + refresh tokens.

```
POST /api/v1/auth/register
```

**Auth Required:** No

#### Request Headers

| Header         | Value              |
|----------------|--------------------|
| `Content-Type` | `application/json` |

#### Request Body

| Field         | Type   | Required | Validation                         |
|---------------|--------|----------|------------------------------------|
| `fullName`    | String | ✅       | Max 100 chars                      |
| `email`       | String | ✅       | Valid email, max 120 chars, unique  |
| `password`    | String | ✅       | 8–100 characters                   |
| `phoneNumber` | String | ❌       | Max 20 chars                       |
| `role`        | String | ✅       | `PG_OWNER`, `TENANT`, or `ADMIN`   |

```json
{
  "fullName": "Ravi Kumar",
  "email": "ravi@example.com",
  "password": "Secure@123",
  "phoneNumber": "9876543210",
  "role": "PG_OWNER"
}
```

#### Response — `201 Created`

```json
{
  "token": "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJyYXZpQGV4YW...",
  "refreshToken": "a3f7c2d1-9e84-4b6a-bf12-1a2b3c4d5e6f",
  "userId": 1,
  "email": "ravi@example.com",
  "role": "PG_OWNER"
}
```

#### Error Responses

| Status | Condition                    | Example Message                              |
|--------|------------------------------|----------------------------------------------|
| `400`  | Validation failed            | `"Validation failed"` + `validationErrors`   |
| `400`  | Duplicate email              | `"Email already registered: ravi@example.com"`|
| `400`  | Invalid role                 | `"Invalid role: XYZ. Must be one of: PG_OWNER, TENANT, ADMIN"` |
| `409`  | DB unique constraint         | `"A data integrity constraint was violated..."` |

---

### 2.2 Login

Authenticates a user and returns access + refresh tokens.

```
POST /api/v1/auth/login
```

**Auth Required:** No

#### Request Headers

| Header         | Value              |
|----------------|--------------------|
| `Content-Type` | `application/json` |

#### Request Body

| Field      | Type   | Required | Validation  |
|------------|--------|----------|-------------|
| `email`    | String | ✅       | Valid email  |
| `password` | String | ✅       | Not blank    |

```json
{
  "email": "ravi@example.com",
  "password": "Secure@123"
}
```

#### Response — `200 OK`

```json
{
  "token": "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJyYXZpQGV4YW...",
  "refreshToken": "b4e8d3f2-7a91-4c5b-ae23-2b3c4d5e6f7a",
  "userId": 1,
  "email": "ravi@example.com",
  "role": "PG_OWNER"
}
```

#### Error Responses

| Status | Condition              | Example Message              |
|--------|------------------------|------------------------------|
| `400`  | Validation failed      | `"Validation failed"`        |
| `400`  | Wrong credentials      | `"Invalid email or password"`|

---

### 2.3 Refresh Token

Exchanges a valid refresh token for a new short-lived access token.

```
POST /api/v1/auth/refresh
```

**Auth Required:** No

#### Request Headers

| Header         | Value              |
|----------------|--------------------|
| `Content-Type` | `application/json` |

#### Request Body

| Field          | Type   | Required | Validation |
|----------------|--------|----------|------------|
| `refreshToken` | String | ✅       | Not blank  |

```json
{
  "refreshToken": "b4e8d3f2-7a91-4c5b-ae23-2b3c4d5e6f7a"
}
```

#### Response — `200 OK`

```json
{
  "accessToken": "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJyYXZpQGV4YW..."
}
```

#### Error Responses

| Status | Condition               | Example Message                                     |
|--------|-------------------------|-----------------------------------------------------|
| `400`  | Validation failed       | `"Validation failed"`                               |
| `400`  | Token not found in DB   | `"Refresh token not found"`                         |
| `401`  | Token expired           | `"Refresh token has expired. Please login again."`  |

---

## 3. PG APIs

### 3.1 Search PGs

Search and filter PG listings with geo-location, price, sharing type, and facility filters. Results are paginated.

```
GET /api/v1/pgs/search
```

**Auth Required:** No (public)

#### Query Parameters

| Parameter       | Type       | Required | Description                                    |
|-----------------|------------|----------|------------------------------------------------|
| `latitude`      | Double     | ❌       | Center latitude for geo search (use with `longitude` & `radiusKm`) |
| `longitude`     | Double     | ❌       | Center longitude for geo search                |
| `radiusKm`      | Double     | ❌       | Search radius in km                            |
| `minPrice`      | BigDecimal | ❌       | Minimum rent filter                            |
| `maxPrice`      | BigDecimal | ❌       | Maximum rent filter                            |
| `sharingType`   | String     | ❌       | e.g. `SINGLE`, `DOUBLE`, `TRIPLE`              |
| `facilityNames` | String[]   | ❌       | Repeat param: `facilityNames=WiFi&facilityNames=AC` |
| `page`          | Integer    | ❌       | Page number (default `0`)                      |
| `size`          | Integer    | ❌       | Page size (default `20`)                       |
| `sort`          | String     | ❌       | Sort field + direction                         |

#### Example Request

```
GET /api/v1/pgs/search?latitude=12.9716&longitude=77.5946&radiusKm=5&minPrice=5000&maxPrice=15000&sharingType=DOUBLE&facilityNames=WiFi&facilityNames=AC&page=0&size=10
```

#### Response — `200 OK`

```json
{
  "content": [
    {
      "id": 1,
      "owner": {
        "id": 10,
        "fullName": "Ravi Kumar",
        "email": "ravi@example.com",
        "phoneNumber": "9876543210"
      },
      "name": "Sunshine PG for Men",
      "description": "Well-furnished PG near MG Road metro",
      "latitude": 12.9750,
      "longitude": 77.5990,
      "locationLabel": "MG Road, Bangalore",
      "isActive": true,
      "sharingOptions": [
        {
          "id": 1,
          "sharingType": "DOUBLE",
          "rent": 8500.00,
          "availabilityCount": 3
        },
        {
          "id": 2,
          "sharingType": "TRIPLE",
          "rent": 6000.00,
          "availabilityCount": 5
        }
      ],
      "facilities": ["WiFi", "AC", "Laundry", "Parking"],
      "images": [
        {
          "id": 1,
          "imageUrl": "https://cdn.staymatch.com/pg/1/room1.jpg",
          "displayOrder": 1
        },
        {
          "id": 2,
          "imageUrl": "https://cdn.staymatch.com/pg/1/hall.jpg",
          "displayOrder": 2
        }
      ],
      "distanceKm": 1.34,
      "createdAt": "2026-03-01T08:30:00Z",
      "updatedAt": "2026-04-10T12:00:00Z"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": { "sorted": false, "empty": true, "unsorted": true },
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalElements": 1,
  "totalPages": 1,
  "last": true,
  "size": 10,
  "number": 0,
  "sort": { "sorted": false, "empty": true, "unsorted": true },
  "numberOfElements": 1,
  "first": true,
  "empty": false
}
```

> **Note:** `distanceKm` is only populated when `latitude` + `longitude` are provided.

---

### 3.2 Get PG by ID

Fetches full PG details including sharing options, facilities, and images.

```
GET /api/v1/pgs/{id}
```

**Auth Required:** No (public)

#### Path Parameters

| Parameter | Type | Required | Validation  |
|-----------|------|----------|-------------|
| `id`      | Long | ✅       | Min value 1 |

#### Example Request

```
GET /api/v1/pgs/1
```

#### Response — `200 OK`

```json
{
  "id": 1,
  "owner": {
    "id": 10,
    "fullName": "Ravi Kumar",
    "email": "ravi@example.com",
    "phoneNumber": "9876543210"
  },
  "name": "Sunshine PG for Men",
  "description": "Well-furnished PG near MG Road metro",
  "latitude": 12.9750,
  "longitude": 77.5990,
  "locationLabel": "MG Road, Bangalore",
  "isActive": true,
  "sharingOptions": [
    {
      "id": 1,
      "sharingType": "DOUBLE",
      "rent": 8500.00,
      "availabilityCount": 3
    }
  ],
  "facilities": ["WiFi", "AC", "Laundry"],
  "images": [
    {
      "id": 1,
      "imageUrl": "https://cdn.staymatch.com/pg/1/room1.jpg",
      "displayOrder": 1
    }
  ],
  "distanceKm": null,
  "createdAt": "2026-03-01T08:30:00Z",
  "updatedAt": "2026-04-10T12:00:00Z"
}
```

#### Error Responses

| Status | Condition      | Example Message              |
|--------|----------------|------------------------------|
| `400`  | `id` < 1       | Constraint violation error   |
| `404`  | PG not found   | `"PG not found with id: 99"` |

---

### 3.3 Create PG

Creates a new PG listing with owner profile, sharing options, facilities, and images.

```
POST /api/v1/pgs
```

**Auth Required:** Yes
**Roles Allowed:** `PG_OWNER`, `ADMIN`

#### Request Headers

| Header          | Value                      |
|-----------------|----------------------------|
| `Content-Type`  | `application/json`         |
| `Authorization` | `Bearer <access_token>`    |

#### Request Body

| Field                              | Type            | Required | Validation                          |
|------------------------------------|-----------------|----------|-------------------------------------|
| `owner`                            | Object          | ✅       | Owner profile details               |
| `owner.fullName`                   | String          | ✅       | Max 100 chars                       |
| `owner.email`                      | String          | ✅       | Valid email, max 120 chars           |
| `owner.phoneNumber`               | String          | ❌       | Max 20 chars                        |
| `name`                             | String          | ✅       | Max 150 chars                       |
| `description`                      | String          | ❌       | Max 600 chars                       |
| `latitude`                         | Double          | ✅       | -90.0 to 90.0                       |
| `longitude`                        | Double          | ✅       | -180.0 to 180.0                     |
| `locationLabel`                    | String          | ✅       | Max 150 chars                       |
| `sharingOptions`                   | Array\<Object\> | ✅       | At least 1 item                     |
| `sharingOptions[].sharingType`     | String          | ✅       | Max 40 chars (e.g. `SINGLE`)        |
| `sharingOptions[].rent`            | BigDecimal      | ✅       | > 0                                 |
| `sharingOptions[].availabilityCount` | Integer       | ✅       | >= 0                                |
| `facilityNames`                    | Array\<String\> | ❌       | Each max 80 chars                   |
| `images`                           | Array\<Object\> | ❌       | Image details                       |
| `images[].imageUrl`               | String          | ✅       | Not blank                           |
| `images[].displayOrder`           | Integer         | ❌       | Display priority                    |

```json
{
  "owner": {
    "fullName": "Ravi Kumar",
    "email": "ravi@example.com",
    "phoneNumber": "9876543210"
  },
  "name": "Sunshine PG for Men",
  "description": "Well-furnished PG near MG Road metro station",
  "latitude": 12.9750,
  "longitude": 77.5990,
  "locationLabel": "MG Road, Bangalore",
  "sharingOptions": [
    {
      "sharingType": "SINGLE",
      "rent": 12000.00,
      "availabilityCount": 2
    },
    {
      "sharingType": "DOUBLE",
      "rent": 8500.00,
      "availabilityCount": 5
    }
  ],
  "facilityNames": ["WiFi", "AC", "Laundry", "Parking"],
  "images": [
    {
      "imageUrl": "https://cdn.staymatch.com/pg/1/room1.jpg",
      "displayOrder": 1
    },
    {
      "imageUrl": "https://cdn.staymatch.com/pg/1/hall.jpg",
      "displayOrder": 2
    }
  ]
}
```

#### Response — `201 Created`

Returns full `PgResponse` (same structure as [Get PG by ID](#32-get-pg-by-id)).

#### Error Responses

| Status | Condition                        | Example Message                                      |
|--------|----------------------------------|------------------------------------------------------|
| `400`  | Validation failed                | `"Validation failed"` + `validationErrors` map       |
| `401`  | Missing / invalid token          | —                                                    |
| `403`  | Wrong role (e.g. `TENANT`)       | `"You do not have permission to perform this action"` |
| `409`  | Duplicate constraint (e.g. owner)| `"A data integrity constraint was violated..."`       |

---

### 3.4 Update PG

Partially updates an existing PG. Only the owner of the PG or an ADMIN can update.

```
PUT /api/v1/pgs/{id}
```

**Auth Required:** Yes
**Roles Allowed:** `PG_OWNER` (own PG only), `ADMIN`

#### Request Headers

| Header          | Value                      |
|-----------------|----------------------------|
| `Content-Type`  | `application/json`         |
| `Authorization` | `Bearer <access_token>`    |

#### Path Parameters

| Parameter | Type | Required | Validation  |
|-----------|------|----------|-------------|
| `id`      | Long | ✅       | Min value 1 |

#### Request Body

All fields are **optional** — only send what you want to update.

| Field                              | Type            | Validation                    |
|------------------------------------|-----------------|-------------------------------|
| `name`                             | String          | Max 150 chars                 |
| `description`                      | String          | Max 600 chars                 |
| `latitude`                         | Double          | -90.0 to 90.0                 |
| `longitude`                        | Double          | -180.0 to 180.0               |
| `locationLabel`                    | String          | Max 150 chars                 |
| `isActive`                         | Boolean         | —                             |
| `sharingOptions`                   | Array\<Object\> | Replaces all options          |
| `sharingOptions[].sharingType`     | String          | Max 40 chars                  |
| `sharingOptions[].rent`            | BigDecimal      | > 0                           |
| `sharingOptions[].availabilityCount` | Integer       | >= 0                          |
| `facilityNames`                    | Array\<String\> | Replaces all facilities       |
| `images`                           | Array\<Object\> | Replaces all images           |
| `images[].imageUrl`               | String          | Not blank                     |
| `images[].displayOrder`           | Integer         | —                             |

```json
{
  "name": "Sunshine PG for Men — Updated",
  "isActive": false,
  "sharingOptions": [
    {
      "sharingType": "DOUBLE",
      "rent": 9000.00,
      "availabilityCount": 4
    }
  ]
}
```

#### Response — `200 OK`

Returns full `PgResponse` with updated data.

#### Error Responses

| Status | Condition                   | Example Message                                      |
|--------|-----------------------------|------------------------------------------------------|
| `400`  | Validation failed / `id` < 1 | `"Validation failed"` or constraint violation        |
| `401`  | Missing / invalid token     | —                                                    |
| `403`  | Not owner & not ADMIN       | `"You do not have permission to perform this action"` |
| `404`  | PG not found                | `"PG not found with id: 99"`                         |

---

### 3.5 Delete PG

Deletes a PG listing. Only the owner of the PG or an ADMIN can delete.

```
DELETE /api/v1/pgs/{id}
```

**Auth Required:** Yes
**Roles Allowed:** `PG_OWNER` (own PG only), `ADMIN`

#### Request Headers

| Header          | Value                   |
|-----------------|-------------------------|
| `Authorization` | `Bearer <access_token>` |

#### Path Parameters

| Parameter | Type | Required | Validation  |
|-----------|------|----------|-------------|
| `id`      | Long | ✅       | Min value 1 |

#### Example Request

```
DELETE /api/v1/pgs/1
Authorization: Bearer eyJhbGciOiJIUzM4NCJ9...
```

#### Response — `204 No Content`

No response body.

#### Error Responses

| Status | Condition                   | Example Message                                      |
|--------|-----------------------------|------------------------------------------------------|
| `400`  | `id` < 1                   | Constraint violation error                           |
| `401`  | Missing / invalid token     | —                                                    |
| `403`  | Not owner & not ADMIN       | `"You do not have permission to perform this action"` |
| `404`  | PG not found                | `"PG not found with id: 99"`                         |

---

## 4. Tenant APIs

### 4.1 Search Tenant Posts

Search tenant posts by budget, location, and lifestyle preferences.

```
GET /api/v1/tenant-posts/search
```

**Auth Required:** No (public)

#### Query Parameters

| Parameter           | Type       | Required | Validation   | Description                              |
|---------------------|------------|----------|--------------|------------------------------------------|
| `budget`            | BigDecimal | ❌       | > 0          | Filter posts where budget range covers this amount |
| `latitude`          | Double     | ❌       | —            | Center latitude for geo search           |
| `longitude`         | Double     | ❌       | —            | Center longitude for geo search          |
| `radiusKm`          | Double     | ❌       | >= 0.1       | Radius in km                             |
| `foodPreference`    | String     | ❌       | —            | e.g. `VEG`, `NON_VEG`, `BOTH`           |
| `smokingPreference` | String     | ❌       | —            | e.g. `NO_SMOKING`, `SMOKING_ALLOWED`     |
| `preferredGender`   | String     | ❌       | —            | e.g. `MALE`, `FEMALE`, `ANY`             |

#### Example Request

```
GET /api/v1/tenant-posts/search?budget=8000&latitude=12.9716&longitude=77.5946&radiusKm=5&foodPreference=VEG
```

#### Response — `200 OK`

Returns a JSON array (not paginated):

```json
[
  {
    "id": 1,
    "user": {
      "id": 5,
      "fullName": "Amit Sharma",
      "email": "amit@example.com",
      "phoneNumber": "9123456789"
    },
    "title": "Looking for PG near Koramangala",
    "description": "Need a clean PG with WiFi and vegetarian food",
    "minBudget": 6000.00,
    "maxBudget": 10000.00,
    "preferredLatitude": 12.9352,
    "preferredLongitude": 77.6245,
    "locationLabel": "Koramangala, Bangalore",
    "moveInDate": "2026-05-01",
    "isActive": true,
    "preference": {
      "id": 1,
      "foodPreference": "VEG",
      "smokingPreference": "NO_SMOKING",
      "preferredGender": "MALE",
      "additionalNotes": "Early riser, no loud music"
    },
    "distanceKm": 3.21,
    "matchScore": null,
    "createdAt": "2026-04-10T09:00:00Z",
    "updatedAt": "2026-04-10T09:00:00Z"
  }
]
```

> **Note:** `distanceKm` is only populated when `latitude` + `longitude` are provided. `matchScore` is populated only when using the match endpoint.

---

### 4.2 Create Tenant Post

Creates a new tenant requirement post with user details and preferences.

```
POST /api/v1/tenant-posts
```

**Auth Required:** Yes
**Roles Allowed:** `TENANT`, `ADMIN`

#### Request Headers

| Header          | Value                      |
|-----------------|----------------------------|
| `Content-Type`  | `application/json`         |
| `Authorization` | `Bearer <access_token>`    |

#### Request Body

| Field                              | Type       | Required | Validation                               |
|------------------------------------|------------|----------|------------------------------------------|
| `user`                             | Object     | ✅       | User details                             |
| `user.fullName`                    | String     | ✅       | Max 100 chars                            |
| `user.email`                       | String     | ✅       | Valid email, max 120 chars               |
| `user.phoneNumber`                | String     | ❌       | Max 20 chars                             |
| `title`                            | String     | ✅       | Max 120 chars                            |
| `description`                      | String     | ❌       | Max 500 chars                            |
| `minBudget`                        | BigDecimal | ✅       | > 0                                      |
| `maxBudget`                        | BigDecimal | ✅       | > 0 and >= `minBudget` (DB constraint)   |
| `preferredLatitude`                | Double     | ���       | -90.0 to 90.0                            |
| `preferredLongitude`               | Double     | ✅       | -180.0 to 180.0                          |
| `locationLabel`                    | String     | ✅       | Max 120 chars                            |
| `moveInDate`                       | String     | ✅       | `YYYY-MM-DD`, today or future            |
| `preference`                       | Object     | ✅       | Lifestyle preferences                    |
| `preference.foodPreference`        | String     | ❌       | Max 50 chars                             |
| `preference.smokingPreference`     | String     | ❌       | Max 50 chars                             |
| `preference.preferredGender`       | String     | ❌       | Max 50 chars                             |
| `preference.additionalNotes`       | String     | ❌       | Max 300 chars                            |

```json
{
  "user": {
    "fullName": "Amit Sharma",
    "email": "amit@example.com",
    "phoneNumber": "9123456789"
  },
  "title": "Looking for PG near Koramangala",
  "description": "Need a clean PG with WiFi and vegetarian food",
  "minBudget": 6000.00,
  "maxBudget": 10000.00,
  "preferredLatitude": 12.9352,
  "preferredLongitude": 77.6245,
  "locationLabel": "Koramangala, Bangalore",
  "moveInDate": "2026-05-01",
  "preference": {
    "foodPreference": "VEG",
    "smokingPreference": "NO_SMOKING",
    "preferredGender": "MALE",
    "additionalNotes": "Early riser, no loud music"
  }
}
```

#### Response — `201 Created`

```json
{
  "id": 1,
  "user": {
    "id": 5,
    "fullName": "Amit Sharma",
    "email": "amit@example.com",
    "phoneNumber": "9123456789"
  },
  "title": "Looking for PG near Koramangala",
  "description": "Need a clean PG with WiFi and vegetarian food",
  "minBudget": 6000.00,
  "maxBudget": 10000.00,
  "preferredLatitude": 12.9352,
  "preferredLongitude": 77.6245,
  "locationLabel": "Koramangala, Bangalore",
  "moveInDate": "2026-05-01",
  "isActive": true,
  "preference": {
    "id": 1,
    "foodPreference": "VEG",
    "smokingPreference": "NO_SMOKING",
    "preferredGender": "MALE",
    "additionalNotes": "Early riser, no loud music"
  },
  "distanceKm": null,
  "matchScore": null,
  "createdAt": "2026-04-14T10:00:00Z",
  "updatedAt": "2026-04-14T10:00:00Z"
}
```

#### Error Responses

| Status | Condition                     | Example Message                                      |
|--------|-------------------------------|------------------------------------------------------|
| `400`  | Validation failed             | `"Validation failed"` + `validationErrors` map       |
| `400`  | `maxBudget` < `minBudget`     | DB check constraint violation                        |
| `401`  | Missing / invalid token       | —                                                    |
| `403`  | Wrong role (e.g. `PG_OWNER`)  | `"You do not have permission to perform this action"` |
| `409`  | Duplicate user email           | `"A data integrity constraint was violated..."`       |

---

## 5. Error Response Reference

### Error Envelope

All errors follow this structure:

```json
{
  "timestamp": "2026-04-14T10:30:00Z",
  "status": <http_status_code>,
  "error": "<HTTP Status Phrase>",
  "message": "<description>",
  "validationErrors": { "<field>": "<message>" } | null
}
```

### HTTP Status Code Summary

| Code  | Meaning                  | When                                                   |
|-------|--------------------------|--------------------------------------------------------|
| `400` | Bad Request              | Validation failure, illegal argument, constraint violation |
| `401` | Unauthorized             | Expired refresh token, missing/invalid JWT             |
| `403` | Forbidden                | Authenticated but wrong role or not resource owner     |
| `404` | Not Found                | Resource does not exist                                |
| `409` | Conflict                 | Unique constraint violation (duplicate data)           |
| `500` | Internal Server Error    | Unexpected server error (no stack trace exposed)       |

### Validation Error Example (400)

```json
{
  "timestamp": "2026-04-14T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "validationErrors": {
    "fullName": "Full name is required",
    "email": "Email must be valid",
    "password": "Password must be 8-100 characters",
    "role": "Role is required"
  }
}
```

### Access Denied Example (403)

```json
{
  "timestamp": "2026-04-14T10:30:00Z",
  "status": 403,
  "error": "Forbidden",
  "message": "You do not have permission to perform this action",
  "validationErrors": null
}
```

### Not Found Example (404)

```json
{
  "timestamp": "2026-04-14T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "PG not found with id: 99",
  "validationErrors": null
}
```

### Refresh Token Expired Example (401)

```json
{
  "timestamp": "2026-04-14T10:30:00Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Refresh token has expired. Please login again.",
  "validationErrors": null
}
```

### Data Integrity Conflict Example (409)

```json
{
  "timestamp": "2026-04-14T10:30:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "A data integrity constraint was violated. This is typically caused by a duplicate entry or an invalid reference.",
  "validationErrors": null
}
```

---

## Token Lifecycle Quick Reference

| Token         | Lifespan  | Storage           | Refresh Strategy                       |
|---------------|-----------|-------------------|----------------------------------------|
| Access Token  | 15 min    | Client (memory)   | Call `POST /api/v1/auth/refresh`       |
| Refresh Token | 7 days    | Server (DB) + Client | Re-login after expiry               |

### Recommended Frontend Flow

```
1. Login  →  store accessToken + refreshToken
2. API call  →  attach accessToken in Authorization header
3. If 401  →  call /api/v1/auth/refresh with refreshToken
4. If refresh succeeds  →  retry original request with new accessToken
5. If refresh fails (401)  →  redirect to login
```

