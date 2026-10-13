# StayMatch

Production-oriented starter implementation for:

- PG Discovery (frontend and SQL schema)
- Tenant Matching (Spring Boot backend, frontend, and SQL schema)

## Backend (Spring Boot)

### PG Discovery Module

Layered architecture for PG lifecycle and search:

- Controller: `src/main/java/org/staymatch/pgdiscovery/controller`
- Service (separated): `PgService` (CRUD) + `PgSearchService` (filtering/geo/pagination)
- Repository: `src/main/java/org/staymatch/pgdiscovery/repository` — uses `JpaSpecificationExecutor`
- Specification: `src/main/java/org/staymatch/pgdiscovery/specification`
- DTO + Mapper: `src/main/java/org/staymatch/pgdiscovery/dto`, `src/main/java/org/staymatch/pgdiscovery/mapper`
- Entities: `OwnerProfile`, `Pg`, `SharingOption`, `Facility`, `PgFacility`, `Image`

#### APIs

- `POST /api/v1/pgs` — Create PG with sharing options, facilities, images
- `GET /api/v1/pgs/{id}` — Get PG details by ID
- `GET /api/v1/pgs/search` — Paginated search with filters:
  - `latitude`, `longitude`, `radiusKm` (Haversine geo filtering)
  - `minPrice`, `maxPrice` (rent range)
  - `sharingType` (e.g., Single, Double, Triple)
  - `facilityNames` (multi-value, e.g., WiFi, AC)
  - Standard pagination: `page`, `size`, `sort`

### Tenant Matching Module

Tenant Matching module follows layered architecture:

- Controller: `src/main/java/org/staymatch/tenantmatching/controller`
- Service interface + implementation: `src/main/java/org/staymatch/tenantmatching/service`
- Repository: `src/main/java/org/staymatch/tenantmatching/repository`
- DTO + Mapper: `src/main/java/org/staymatch/tenantmatching/dto`, `src/main/java/org/staymatch/tenantmatching/mapper`
- Exception handling: `src/main/java/org/staymatch/common/exception`

### APIs

- `POST /api/v1/tenant-posts` - Create tenant requirement
- `GET /api/v1/tenant-posts/{id}` - Get tenant post by id
- `GET /api/v1/tenant-posts/search` - Search tenant posts (budget/location/preferences)
- `GET /api/v1/tenant-posts/{id}/matches` - Match tenants by budget/location/preferences

## Frontend (Angular)

Path: `frontend/`

Feature pages:

- PG Search Page
- PG Detail Page
- Tenant Matching Page

Reusable structure:

- `src/app/modules`
- `src/app/components`
- `src/app/services`

All components use separate `.ts`, `.html`, `.css` files.

## Database (PostgreSQL)

Schema: `database/schema.sql`

Includes 3NF-oriented tables, PK/FK constraints, and query-focused indexes:

- `users`
- `pg`
- `sharing_options`
- `facilities`
- `pg_facilities`
- `images`
- `tenant_posts`
- `preferences`

## Quick Run

```powershell
.\gradlew clean test
.\gradlew bootRun
```

Frontend commands:

```powershell
Set-Location .\frontend
npm install
npm run build
npm start
```

