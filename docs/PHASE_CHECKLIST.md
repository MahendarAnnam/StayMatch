# StayMatch — Phase-by-Phase Implementation Checklist

> Total: **73 source files** | **4 test classes** | **BUILD SUCCESSFUL** (all tests pass)
> Last verified: April 14, 2026

---

## Phase 1: PG Discovery Backend Module

> *Spring Boot backend for PG listing with layered architecture*

### 1.1 Entities

| # | Item | File | Status |
|---|------|------|--------|
| 1 | `OwnerProfile` entity (extends `BaseEntity`) | `pgdiscovery/model/OwnerProfile.java` | ✅ |
| 2 | `Pg` entity with bidirectional helpers | `pgdiscovery/model/Pg.java` | ✅ |
| 3 | `SharingOption` entity (unique per pg+type) | `pgdiscovery/model/SharingOption.java` | ✅ |
| 4 | `Facility` entity (master table) | `pgdiscovery/model/Facility.java` | ✅ |
| 5 | `PgFacility` join entity (surrogate PK) | `pgdiscovery/model/PgFacility.java` | ✅ |
| 6 | `Image` entity with display order | `pgdiscovery/model/Image.java` | ✅ |

### 1.2 DTOs

| # | Item | File | Status |
|---|------|------|--------|
| 7 | `CreatePgRequest` (validated, cascading `@Valid`) | `pgdiscovery/dto/CreatePgRequest.java` | ✅ |
| 8 | `OwnerProfileRequest` / `OwnerProfileResponse` | `pgdiscovery/dto/OwnerProfile*.java` | ✅ |
| 9 | `SharingOptionRequest` / `SharingOptionResponse` | `pgdiscovery/dto/SharingOption*.java` | ✅ |
| 10 | `ImageRequest` / `ImageResponse` | `pgdiscovery/dto/Image*.java` | ✅ |
| 11 | `PgResponse` (includes `distanceKm`) | `pgdiscovery/dto/PgResponse.java` | ✅ |
| 12 | `PgSearchCriteria` (lat/lng/radius/price/sharing/facilities) | `pgdiscovery/dto/PgSearchCriteria.java` | ✅ |

### 1.3 Mappers

| # | Item | File | Status |
|---|------|------|--------|
| 13 | `PgMapper` (delegates to sub-mappers) | `pgdiscovery/mapper/PgMapper.java` | ✅ |
| 14 | `OwnerProfileMapper` | `pgdiscovery/mapper/OwnerProfileMapper.java` | ✅ |
| 15 | `SharingOptionMapper` | `pgdiscovery/mapper/SharingOptionMapper.java` | ✅ |
| 16 | `ImageMapper` | `pgdiscovery/mapper/ImageMapper.java` | ✅ |

### 1.4 Repositories

| # | Item | File | Status |
|---|------|------|--------|
| 17 | `PgRepository` (JpaSpecificationExecutor + @EntityGraph) | `pgdiscovery/repository/PgRepository.java` | ✅ |
| 18 | `OwnerProfileRepository` (findByEmailIgnoreCase) | `pgdiscovery/repository/OwnerProfileRepository.java` | ✅ |
| 19 | `FacilityRepository` (findByFacilityNameIgnoreCase) | `pgdiscovery/repository/FacilityRepository.java` | ✅ |

### 1.5 Services

| # | Item | File | Status |
|---|------|------|--------|
| 20 | `PgService` interface | `pgdiscovery/service/PgService.java` | ✅ |
| 21 | `PgServiceImpl` (create PG + get by ID) | `pgdiscovery/service/impl/PgServiceImpl.java` | ✅ |
| 22 | `PgSearchService` interface (SRP split) | `pgdiscovery/service/PgSearchService.java` | ✅ |
| 23 | `PgSearchServiceImpl` (specification + geo) | `pgdiscovery/service/impl/PgSearchServiceImpl.java` | ✅ |

### 1.6 Controller & Specification

| # | Item | File | Status |
|---|------|------|--------|
| 24 | `PgController` (REST API: POST, GET, GET/search) | `pgdiscovery/controller/PgController.java` | ✅ |
| 25 | `PgSpecifications` (composable JPA predicates) | `pgdiscovery/specification/PgSpecifications.java` | ✅ |

### 1.7 Test

| # | Item | File | Status |
|---|------|------|--------|
| 26 | `PgDiscoveryIntegrationTest` (create + 4 search scenarios) | `test/.../PgDiscoveryIntegrationTest.java` | ✅ |

---

## Phase 2: Tenant Matching Backend Module

> *Spring Boot backend for tenant posts, search, and matching*

### 2.1 Entities

| # | Item | File | Status |
|---|------|------|--------|
| 27 | `User` entity | `tenantmatching/model/User.java` | ✅ |
| 28 | `TenantPost` entity (extends `BaseEntity`) | `tenantmatching/model/TenantPost.java` | ✅ |
| 29 | `Preference` entity | `tenantmatching/model/Preference.java` | ✅ |

### 2.2 DTOs

| # | Item | File | Status |
|---|------|------|--------|
| 30 | `CreateTenantPostRequest` (validated) | `tenantmatching/dto/CreateTenantPostRequest.java` | ✅ |
| 31 | `CreateUserRequest` | `tenantmatching/dto/CreateUserRequest.java` | ✅ |
| 32 | `PreferenceRequest` / `PreferenceResponse` | `tenantmatching/dto/Preference*.java` | ✅ |
| 33 | `TenantPostResponse` | `tenantmatching/dto/TenantPostResponse.java` | ✅ |
| 34 | `UserResponse` (tenant matching) | `tenantmatching/dto/UserResponse.java` | ✅ |
| 35 | `TenantPostSearchCriteria` | `tenantmatching/dto/TenantPostSearchCriteria.java` | ✅ |

### 2.3 Mappers & Repositories

| # | Item | File | Status |
|---|------|------|--------|
| 36 | `TenantPostMapper` | `tenantmatching/mapper/TenantPostMapper.java` | ✅ |
| 37 | `UserMapper` (tenant matching module) | `tenantmatching/mapper/UserMapper.java` | ✅ |
| 38 | `PreferenceMapper` | `tenantmatching/mapper/PreferenceMapper.java` | ✅ |
| 39 | `TenantPostRepository` | `tenantmatching/repository/TenantPostRepository.java` | ✅ |
| 40 | `UserRepository` | `tenantmatching/repository/UserRepository.java` | ✅ |
| 41 | `PreferenceRepository` | `tenantmatching/repository/PreferenceRepository.java` | ✅ |

### 2.4 Service & Controller

| # | Item | File | Status |
|---|------|------|--------|
| 42 | `TenantPostService` interface | `tenantmatching/service/TenantPostService.java` | ✅ |
| 43 | `TenantPostServiceImpl` (create, search, match) | `tenantmatching/service/impl/TenantPostServiceImpl.java` | ✅ |
| 44 | `TenantPostController` (4 endpoints) | `tenantmatching/controller/TenantPostController.java` | ✅ |

### 2.5 Test

| # | Item | File | Status |
|---|------|------|--------|
| 45 | `TenantPostControllerIntegrationTest` | `test/.../TenantPostControllerIntegrationTest.java` | ✅ |

---

## Phase 3: Critical Bug Fixes

> *Fixed 3 critical issues from code review*

| # | Fix | Files Modified | Status |
|---|-----|----------------|--------|
| 46 | `query.distinct(true)` in all join-based Specifications | `PgSpecifications.java` | ✅ |
| 47 | Out-of-radius PGs excluded (not leaked with null distance) | `PgSearchServiceImpl.java` | ✅ |
| 48 | `pg_facilities` schema aligned (surrogate PK + unique constraint) | `schema.sql`, `PgFacility.java` | ✅ |

---

## Phase 4: Performance & N+1 Fixes

> *Eliminated N+1 queries and added geo bounding-box optimization*

| # | Fix | Files Modified | Status |
|---|-----|----------------|--------|
| 49 | `@EntityGraph` on `PgRepository.findById` | `PgRepository.java` | ✅ |
| 50 | `@EntityGraph` on `PgRepository.findAll(Spec, Pageable)` | `PgRepository.java` | ✅ |
| 51 | `List` → `Set` on `Pg` collections (fix MultipleBagFetchException) | `Pg.java` | ✅ |
| 52 | `GeoUtils.boundingBox()` helper added | `common/util/GeoUtils.java` | ✅ |
| 53 | `PgSpecifications.withinBoundingBox()` (DB-level lat/lng filter) | `PgSpecifications.java` | ✅ |
| 54 | Bounding-box pre-filter wired into search pipeline | `PgSearchServiceImpl.java` | ✅ |

---

## Phase 5: Exception Handler Enhancements

> *Added missing exception handlers for clean error responses*

| # | Handler | HTTP Status | File | Status |
|---|---------|-------------|------|--------|
| 55 | `ConstraintViolationException` | 400 Bad Request | `GlobalExceptionHandler.java` | ✅ |
| 56 | `DataIntegrityViolationException` | 409 Conflict | `GlobalExceptionHandler.java` | ✅ |
| 57 | `AccessDeniedException` | 403 Forbidden | `GlobalExceptionHandler.java` | ✅ |

---

## Phase 6: Code Quality Refactoring

> *BaseEntity extraction, logging, validation improvements*

| # | Improvement | Files Modified | Status |
|---|-------------|----------------|--------|
| 58 | `BaseEntity` `@MappedSuperclass` (id, createdAt, updatedAt) | `common/model/BaseEntity.java` | ✅ |
| 59 | `OwnerProfile` extends `BaseEntity` | `pgdiscovery/model/OwnerProfile.java` | ✅ |
| 60 | `Pg` extends `BaseEntity` | `pgdiscovery/model/Pg.java` | ✅ |
| 61 | `TenantPost` extends `BaseEntity` | `tenantmatching/model/TenantPost.java` | ✅ |
| 62 | SLF4J logging in `PgServiceImpl` | `pgdiscovery/service/impl/PgServiceImpl.java` | ✅ |
| 63 | SLF4J logging in `PgSearchServiceImpl` | `pgdiscovery/service/impl/PgSearchServiceImpl.java` | ✅ |
| 64 | `minPrice ≤ maxPrice` validation | `PgSearchServiceImpl.java` | ✅ |

---

## Phase 7: JWT Security & Authentication

> *Complete JWT-based auth with role-based authorization*

### 7.1 Security Infrastructure

| # | Item | File | Status |
|---|------|------|--------|
| 65 | `Role` enum (PG_OWNER, TENANT, ADMIN) | `auth/model/Role.java` | ✅ |
| 66 | `AppUser` entity (extends BaseEntity, BCrypt password) | `auth/model/AppUser.java` | ✅ |
| 67 | `AppUserRepository` (findByEmail, existsByEmail) | `auth/repository/AppUserRepository.java` | ✅ |
| 68 | `JwtUtil` (generate/parse/validate using JJWT) | `auth/security/JwtUtil.java` | ✅ |
| 69 | `AppUserDetails` (UserDetails adapter) | `auth/security/AppUserDetails.java` | ✅ |
| 70 | `AppUserDetailsService` (loads user by email) | `auth/security/AppUserDetailsService.java` | ✅ |
| 71 | `JwtAuthenticationFilter` (OncePerRequestFilter) | `auth/security/JwtAuthenticationFilter.java` | ✅ |
| 72 | `SecurityConfig` (filter chain, BCrypt, method security) | `auth/security/SecurityConfig.java` | ✅ |
| 73 | `OwnershipGuard` (SpEL helper for resource ownership) | `auth/security/OwnershipGuard.java` | ✅ |

### 7.2 Auth DTOs

| # | Item | File | Status |
|---|------|------|--------|
| 74 | `RegisterRequest` (validated: @NotBlank, @Email, @Size) | `auth/dto/RegisterRequest.java` | ✅ |
| 75 | `LoginRequest` (validated) | `auth/dto/LoginRequest.java` | ✅ |
| 76 | `AuthResponse` (token + userId + email + role) | `auth/dto/AuthResponse.java` | ✅ |

### 7.3 Authorization Rules on Controllers

| # | Endpoint | Access Rule | Status |
|---|----------|-------------|--------|
| 77 | `POST /api/v1/pgs` | `@PreAuthorize("hasAnyRole('PG_OWNER', 'ADMIN')")` | ✅ |
| 78 | `POST /api/v1/tenant-posts` | `@PreAuthorize("hasAnyRole('TENANT', 'ADMIN')")` | ✅ |
| 79 | `GET /api/v1/pgs/**` | Public (permitAll) | ✅ |
| 80 | `GET /api/v1/tenant-posts/**` | Public (permitAll) | ✅ |
| 81 | `POST /api/v1/auth/**` | Public (permitAll) | ✅ |

### 7.4 Dependencies & Config

| # | Item | File | Status |
|---|------|------|--------|
| 82 | `spring-boot-starter-security` dependency | `build.gradle` | ✅ |
| 83 | `jjwt-api/impl/jackson 0.12.6` dependency | `build.gradle` | ✅ |
| 84 | `app.jwt.secret` + `app.jwt.expiration-ms` config | `application.yaml` | ✅ |
| 85 | `app_users` table in schema | `database/schema.sql` | ✅ |

---

## Phase 8: User Service Module (SRP Refactor)

> *Separated user lifecycle from authentication concerns*

| # | Item | File | Status |
|---|------|------|--------|
| 86 | `UserResponse` DTO (no token — clean profile DTO) | `auth/dto/UserResponse.java` | ✅ |
| 87 | `UserMapper` (`@Component("appUserMapper")` — avoids bean conflict) | `auth/mapper/UserMapper.java` | ✅ |
| 88 | `UserService` interface (registerUser, getUserById, getUserByEmail) | `auth/service/UserService.java` | ✅ |
| 89 | `UserServiceImpl` (BCrypt, role validation, SLF4J logging) | `auth/service/impl/UserServiceImpl.java` | ✅ |
| 90 | `AuthServiceImpl` refactored (delegates to UserService — SRP) | `auth/service/impl/AuthServiceImpl.java` | ✅ |
| 91 | `AuthController` updated (injects both services, GET /users/{id}) | `auth/controller/AuthController.java` | ✅ |
| 92 | `UserServiceIntegrationTest` (3 tests: register, duplicate, bad role) | `test/.../UserServiceIntegrationTest.java` | ✅ |

---

## Cross-Cutting Concerns

| # | Item | File | Status |
|---|------|------|--------|
| 93 | `GeoUtils` — Haversine + bounding-box | `common/util/GeoUtils.java` | ✅ |
| 94 | `BaseEntity` — id, createdAt, updatedAt, @PrePersist/@PreUpdate | `common/model/BaseEntity.java` | ✅ |
| 95 | `GlobalExceptionHandler` — 6 handlers (400/403/404/409/500) | `common/exception/GlobalExceptionHandler.java` | ✅ |
| 96 | `ErrorResponse` record | `common/exception/ErrorResponse.java` | ✅ |
| 97 | `ResourceNotFoundException` | `common/exception/ResourceNotFoundException.java` | ✅ |
| 98 | PostgreSQL schema (10 tables, 7 indexes) | `database/schema.sql` | ✅ |

---

## Test Suite Summary

| Test Class | Tests | Status |
|------------|-------|--------|
| `StayMatchApplicationTests` | 1 (context loads) | ✅ PASS |
| `PgDiscoveryIntegrationTest` | 1 (6 scenarios) | ✅ PASS |
| `TenantPostControllerIntegrationTest` | 1 (create + search + match) | ✅ PASS |
| `UserServiceIntegrationTest` | 3 (register, dup email, bad role) | ✅ PASS |
| **Total** | **6 tests** | **✅ ALL PASS** |

---

## API Endpoint Summary

| Method | Endpoint | Auth | Role |
|--------|----------|------|------|
| `POST` | `/api/v1/auth/register` | Public | — |
| `POST` | `/api/v1/auth/login` | Public | — |
| `GET` | `/api/v1/auth/users/{id}` | JWT | ADMIN or self |
| `POST` | `/api/v1/pgs` | JWT | PG_OWNER, ADMIN |
| `GET` | `/api/v1/pgs/{id}` | Public | — |
| `GET` | `/api/v1/pgs/search` | Public | — |
| `POST` | `/api/v1/tenant-posts` | JWT | TENANT, ADMIN |
| `GET` | `/api/v1/tenant-posts/{id}` | Public | — |
| `GET` | `/api/v1/tenant-posts/search` | Public | — |
| `GET` | `/api/v1/tenant-posts/{id}/matches` | Public | — |

---

## Final Build Verification

```
.\gradlew.bat clean test → BUILD SUCCESSFUL
6 tests, 0 failures
73 source files, 4 test classes
```

