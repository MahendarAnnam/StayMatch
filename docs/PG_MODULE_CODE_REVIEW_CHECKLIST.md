# PG Discovery Module — Code Review Checklist

> Reviewed against: SOLID, Clean Architecture, JPA best practices, REST conventions, security, and production-readiness.
> Date: April 14, 2026

---

## Legend

| Symbol | Meaning |
|--------|---------|
| ✅ | Pass — meets standards |
| ⚠️ | Warning — functional but improvable |
| ❌ | Fail — must fix before production |

---

## 1. Architecture & SOLID Principles

| # | Check | Status | File(s) | Notes |
|---|-------|--------|---------|-------|
| 1.1 | Layered architecture (Controller → Service → Repository) | ✅ | All | Controller never touches Repository directly |
| 1.2 | Single Responsibility — `PgService` vs `PgSearchService` split | ✅ | `PgService.java`, `PgSearchService.java` | CRUD and search have separate interfaces and implementations |
| 1.3 | Open/Closed — Specification pattern for filters | ✅ | `PgSpecifications.java` | New filters can be added without modifying `PgSearchServiceImpl` |
| 1.4 | Interface Segregation — service interfaces | ✅ | `PgService.java`, `PgSearchService.java` | Each interface has a focused contract |
| 1.5 | Dependency Inversion — controller depends on interfaces | ✅ | `PgController.java` | Injects `PgService` and `PgSearchService` interfaces, not impls |
| 1.6 | Liskov Substitution — implementations are substitutable | ✅ | `PgServiceImpl.java`, `PgSearchServiceImpl.java` | Can swap impls without breaking callers |
| 1.7 | No business logic in Controller | ✅ | `PgController.java` | Controller only maps params → criteria DTO → delegates to service |
| 1.8 | No business logic in Repository | ✅ | All repositories | Pure data-access interfaces |

---

## 2. Entity Layer

| # | Check | Status | File(s) | Notes |
|---|-------|--------|---------|-------|
| 2.1 | All entities have `@Entity`, `@Table`, `@Id`, `@GeneratedValue` | ✅ | All 6 entities | Consistent IDENTITY strategy |
| 2.2 | Audit fields (`createdAt`, `updatedAt`) with `@PrePersist`/`@PreUpdate` | ✅ | `Pg.java`, `OwnerProfile.java` | ⚠️ Consider extracting to `@MappedSuperclass BaseEntity` to avoid duplication across all entities |
| 2.3 | Bidirectional relationship helpers on owning side | ✅ | `Pg.java` → `addSharingOption()`, `addPgFacility()`, `addImage()` | Properly syncs both sides |
| 2.4 | `FetchType.LAZY` on all `@ManyToOne` | ✅ | `SharingOption`, `PgFacility`, `Image` | Avoids N+1 at entity declaration level |
| 2.5 | Cascade and orphanRemoval on `@OneToMany` collections | ✅ | `Pg.java` | `CascadeType.ALL` + `orphanRemoval = true` for sharing options, facilities, images |
| 2.6 | Unique constraints match DB schema | ✅ | `SharingOption` (pg_id + sharing_type), `PgFacility` (pg_id + facility_id) | |
| 2.7 | `PgFacility` uses surrogate `@Id` instead of composite PK | ⚠️ | `PgFacility.java` | **Schema uses composite PK** `PRIMARY KEY (pg_id, facility_id)` but entity has `@Id Long id` with `@GeneratedValue`. JPA entity and SQL schema are **inconsistent**. Either add `id BIGSERIAL` to schema or switch entity to `@IdClass`/`@EmbeddedId`. |
| 2.8 | Column lengths match DTO `@Size` annotations and SQL `VARCHAR(n)` | ✅ | All | `name(150)`, `description(600)`, `email(120)`, etc. all align |
| 2.9 | `@Column(precision, scale)` for monetary fields | ✅ | `SharingOption.rent` | `NUMERIC(10,2)` aligns with schema |
| 2.10 | No `toString()` printing lazy collections (avoids `LazyInitializationException`) | ✅ | All entities | No `toString` overrides on entities |

---

## 3. DTO & Validation Layer

| # | Check | Status | File(s) | Notes |
|---|-------|--------|---------|-------|
| 3.1 | Request DTOs have Jakarta Bean Validation annotations | ✅ | `CreatePgRequest`, `OwnerProfileRequest`, `SharingOptionRequest`, `ImageRequest` | `@NotBlank`, `@NotNull`, `@Size`, `@DecimalMin`, `@DecimalMax`, `@Valid` (cascading) |
| 3.2 | `@Valid` cascading on nested request objects | ✅ | `CreatePgRequest.java` line 15, 40, 45 | Owner, sharingOptions, images all cascade validation |
| 3.3 | `@NotEmpty` enforces at-least-one sharing option | ✅ | `CreatePgRequest.java` line 39 | |
| 3.4 | Lat/Lng range validation (`-90..90`, `-180..180`) | ✅ | `CreatePgRequest.java` lines 26-32 | |
| 3.5 | Response DTOs have no validation annotations (correct separation) | ✅ | `PgResponse`, `OwnerProfileResponse`, `SharingOptionResponse`, `ImageResponse` | |
| 3.6 | `PgSearchCriteria` is a plain POJO (not validated — correct for query params) | ✅ | `PgSearchCriteria.java` | Filtering is optional |
| 3.7 | No entity leakage in responses | ✅ | All | DTOs never expose JPA entities |
| 3.8 | `distanceKm` only on response, not on entity | ✅ | `PgResponse.java` | Computed field, not persisted |
| 3.9 | Missing: `minPrice > maxPrice` cross-field validation | ⚠️ | `PgSearchCriteria.java` | No validation prevents `minPrice=10000 & maxPrice=5000`. Consider adding a custom `@ValidPriceRange` constraint or service-level check. |

---

## 4. Mapper Layer

| # | Check | Status | File(s) | Notes |
|---|-------|--------|---------|-------|
| 4.1 | Mappers are `@Component` (Spring-managed) | ✅ | All 4 mappers | Injected via constructor DI |
| 4.2 | `PgMapper` delegates to sub-mappers (SRP) | ✅ | `PgMapper.java` | Composes `OwnerProfileMapper`, `SharingOptionMapper`, `ImageMapper` |
| 4.3 | Null-safe collection handling | ✅ | `PgMapper.toResponse()` lines 66-91 | Null-checks before streaming |
| 4.4 | Email normalized to lowercase on creation | ✅ | `OwnerProfileMapper.toEntity()` | `request.getEmail().toLowerCase()` |
| 4.5 | Facility names mapped from join entity → flat string list | ✅ | `PgMapper.toResponse()` line 75-79 | Extracts `facilityName` from `PgFacility.facility` |
| 4.6 | `toEntity` does not set `id` (correct — let DB generate) | ✅ | All mappers | |

---

## 5. Repository Layer

| # | Check | Status | File(s) | Notes |
|---|-------|--------|---------|-------|
| 5.1 | `PgRepository` extends `JpaSpecificationExecutor` | ✅ | `PgRepository.java` | Enables dynamic predicate-based queries |
| 5.2 | `OwnerProfileRepository.findByEmailIgnoreCase` for dedup | ✅ | `OwnerProfileRepository.java` | |
| 5.3 | `FacilityRepository.findByFacilityNameIgnoreCase` for get-or-create | ✅ | `FacilityRepository.java` | |
| 5.4 | Missing: `@EntityGraph` on `PgRepository.findById` | ⚠️ | `PgRepository.java` | `getPgById()` will trigger **N+1 queries** for sharingOptions, pgFacilities, images, and facility. Add a custom `@EntityGraph(attributePaths = {"sharingOptions", "pgFacilities.facility", "images", "owner"})` query. |
| 5.5 | Missing: `@EntityGraph` or fetch join on search results | ⚠️ | `PgSearchServiceImpl.java` | `pgRepository.findAll(spec, pageable)` will also N+1 for each PG's children. Consider `@EntityGraph` or `JOIN FETCH` in a custom query. |

---

## 6. Service Layer

| # | Check | Status | File(s) | Notes |
|---|-------|--------|---------|-------|
| 6.1 | `@Transactional` on write operations | ✅ | `PgServiceImpl.createPg()` | |
| 6.2 | `@Transactional(readOnly = true)` on read operations | ✅ | `PgServiceImpl.getPgById()`, `PgSearchServiceImpl.searchPgs()` | Correct Hibernate optimisation |
| 6.3 | `ResourceNotFoundException` for missing PG | ✅ | `PgServiceImpl.getPgById()` line 51 | |
| 6.4 | Owner resolution — get-or-create by email | ✅ | `PgServiceImpl.resolveOwner()` | |
| 6.5 | Facility resolution — get-or-create by name | ✅ | `PgServiceImpl.attachFacilities()` | |
| 6.6 | Geo-radius filtering is **post-query**, not DB-level | ⚠️ | `PgSearchServiceImpl.computeDistance()` line 77 | PGs beyond radius get `distanceKm = null` but **are still included in the `Page`**. The returned page count will be wrong. Fix: filter the page content and adjust totals, or add a bounding-box pre-filter in Specifications. |
| 6.7 | `computeDistance` returns `null` for out-of-radius PGs (leaks to response) | ❌ | `PgSearchServiceImpl.java` line 78 | PGs with `distanceKm = null` appear in results but have no distance — confusing for API consumers. They should be **excluded** from results entirely. |
| 6.8 | Specification join duplication risk | ⚠️ | `PgSpecifications.java` | If both `hasSharingType` AND `rentBetween` are combined, two separate `JOIN` on `sharingOptions` are created. This can cause duplicate results or wrong predicates. Use `query.distinct(true)` or share a single join. |
| 6.9 | Missing `query.distinct(true)` on Specification queries | ❌ | `PgSpecifications.java` | Join-based Specifications (sharing + facilities) will produce **duplicate PG rows** in results. Must add `query.distinct(true)` in each Specification or in `buildSpecification()`. |
| 6.10 | Facility filter returns PGs with **any** of the listed facilities | ✅ | `PgSpecifications.hasFacilities()` | Uses `IN(...)` — correct for "any match". If "all match" is needed later, change to a `HAVING COUNT(DISTINCT ...)` approach. |

---

## 7. Controller Layer

| # | Check | Status | File(s) | Notes |
|---|-------|--------|---------|-------|
| 7.1 | `@RestController` + `@RequestMapping("/api/v1/pgs")` versioned path | ✅ | `PgController.java` | |
| 7.2 | `@Valid @RequestBody` on POST | ✅ | `PgController.java` line 40 | Triggers Bean Validation |
| 7.3 | `@Validated` at class level for param constraint support | ✅ | `PgController.java` line 26 | Enables `@Min(1)` on path variable |
| 7.4 | `HttpStatus.CREATED` (201) for POST | ✅ | `PgController.java` line 42 | |
| 7.5 | Pagination via `@PageableDefault(size = 20)` | ✅ | `PgController.java` line 59 | |
| 7.6 | No business logic in controller | ✅ | `PgController.java` | Only DTO assembly + delegation |
| 7.7 | Missing: `@CrossOrigin` or global CORS config | ⚠️ | `PgController.java` | Angular frontend will need CORS. Add a `WebMvcConfigurer` CORS mapping or `@CrossOrigin`. |
| 7.8 | Missing: API response wrapper / HATEOAS links | ⚠️ | `PgController.java` | Not blocking, but a standardised `ApiResponse<T>` wrapper improves client consistency. |

---

## 8. Exception Handling

| # | Check | Status | File(s) | Notes |
|---|-------|--------|---------|-------|
| 8.1 | `@RestControllerAdvice` global handler | ✅ | `GlobalExceptionHandler.java` | |
| 8.2 | Handles `ResourceNotFoundException` → 404 | ✅ | | |
| 8.3 | Handles `MethodArgumentNotValidException` → 400 with field errors | ✅ | | |
| 8.4 | Handles `IllegalArgumentException` → 400 | ✅ | | |
| 8.5 | Generic `Exception` → 500 (does not leak stack trace) | ✅ | | |
| 8.6 | Missing: `ConstraintViolationException` handler | ⚠️ | `GlobalExceptionHandler.java` | `@Min(1)` on path variable in controller will throw `ConstraintViolationException`, not `MethodArgumentNotValidException`. This results in an unformatted 500 error. Add an explicit handler returning 400. |
| 8.7 | Missing: `DataIntegrityViolationException` handler | ⚠️ | `GlobalExceptionHandler.java` | Duplicate owner email or duplicate sharing type per PG will throw an unformatted 500. Add a handler returning 409 Conflict. |

---

## 9. Database Schema Alignment

| # | Check | Status | File(s) | Notes |
|---|-------|--------|---------|-------|
| 9.1 | `owner_profiles` table present in schema | ✅ | `schema.sql` line 12 | |
| 9.2 | `pg.owner_id` FK → `owner_profiles(id)` | ✅ | `schema.sql` line 23 | |
| 9.3 | Indexes on search columns | ✅ | `schema.sql` lines 86-92 | `idx_pg_geo`, `idx_pg_active`, `idx_sharing_option_filter`, `idx_pg_facilities_facility` |
| 9.4 | `pg_facilities` schema uses composite PK, JPA uses surrogate `id` | ❌ | `PgFacility.java` vs `schema.sql` line 48-52 | **Mismatch.** Schema: `PRIMARY KEY (pg_id, facility_id)` with no `id` column. Entity: `@Id Long id` with `@GeneratedValue`. Fix one to match the other. |
| 9.5 | Missing `owner_profiles` index on `email` | ⚠️ | `schema.sql` | `UNIQUE` constraint on email creates an implicit index, so this is OK — but explicit index documentation would help. |
| 9.6 | Missing `ON DELETE CASCADE` on `pg.owner_id` FK | ⚠️ | `schema.sql` line 23 | Currently no cascade. Deleting an owner with active PGs will throw FK violation. Intentional (safe) but should be documented. |

---

## 10. Testing

| # | Check | Status | File(s) | Notes |
|---|-------|--------|---------|-------|
| 10.1 | Integration test covers create → getById → search flows | ✅ | `PgDiscoveryIntegrationTest.java` | 6 test scenarios in one test method |
| 10.2 | Tests all search dimensions (sharingType, priceRange, geo, facilities) | ✅ | | |
| 10.3 | Missing: unit tests for `PgServiceImpl` with mocked repos | ⚠️ | — | Service logic (owner resolution, facility attach) should have isolated unit tests |
| 10.4 | Missing: unit tests for `PgSpecifications` | ⚠️ | — | Each Specification should be tested independently |
| 10.5 | Missing: unit tests for `GeoUtils.distanceKm()` | ⚠️ | — | Known-distance test pairs (e.g., Koramangala ↔ Whitefield ≈ 18km) |
| 10.6 | Missing: negative test cases (invalid input, not-found) | ⚠️ | — | No tests for 404 on bad id, 400 on validation failure |
| 10.7 | Test method is monolithic | ⚠️ | `PgDiscoveryIntegrationTest.java` | Single `@Test` method tests 6 scenarios. Break into `@Nested` or separate `@Test` methods with `@Order` for clarity. |
| 10.8 | Missing: `@DirtiesContext` or `@Transactional` on test | ⚠️ | `PgDiscoveryIntegrationTest.java` | Test data bleeds into other tests in the suite (e.g., `TenantPostControllerIntegrationTest`). Add `@Transactional` (auto-rollback) on test class. |

---

## 11. Security & Production Readiness

| # | Check | Status | Notes |
|---|-------|--------|-------|
| 11.1 | No authentication/authorization on APIs | ⚠️ | Expected for MVP. Before production: add Spring Security with role-based access on POST endpoints. |
| 11.2 | No rate limiting | ⚠️ | Search endpoint is open to abuse. Consider API gateway or `@RateLimiter`. |
| 11.3 | No input sanitization beyond validation | ⚠️ | `imageUrl` accepts arbitrary strings. Validate URL format or whitelist domains. |
| 11.4 | `application.yaml` uses H2 in-memory — OK for dev | ✅ | PostgreSQL profile should be added for prod. |
| 11.5 | `ddl-auto: update` — dangerous for production | ❌ | Should be `validate` in production. Use Flyway or Liquibase for migrations. |
| 11.6 | No logging in service methods | ⚠️ | Add SLF4J logging for create/search operations for observability. |

---

## 12. Summary — Action Items

### 🔴 Must Fix (3 items) — ✅ ALL FIXED (April 14 2026)

| # | Issue | Fix | Status |
|---|-------|-----|--------|
| 6.7 | Out-of-radius PGs leak into search results with `distanceKm = null` | `PgSearchServiceImpl` now filters out beyond-radius PGs and builds a clean `PageImpl` | ✅ Fixed |
| 6.9 | Duplicate PG rows from join-based Specifications | Added `query.distinct(true)` in `hasSharingType`, `rentBetween`, `hasFacilities` | ✅ Fixed |
| 9.4 | `PgFacility` entity has surrogate `@Id` but schema uses composite PK | Updated `schema.sql` to use `id BIGSERIAL PRIMARY KEY` + `UNIQUE(pg_id, facility_id)` | ✅ Fixed |

### 🟡 Should Fix (12 items)

| # | Issue | Priority |
|---|-------|----------|
| 2.2 | Extract `BaseEntity` for audit fields | Medium |
| 3.9 | Add `minPrice ≤ maxPrice` cross-field validation | Medium |
| 5.4 | Add `@EntityGraph` on `findById` to prevent N+1 | High |
| 5.5 | Add `@EntityGraph` on search queries | High |
| 6.6 | Move geo-radius to bounding-box pre-filter in Specification | High |
| 6.8 | Avoid duplicate joins when combining sharing Specifications | Medium |
| 7.7 | Add CORS configuration for Angular frontend | High |
| 8.6 | Add `ConstraintViolationException` handler | High |
| 8.7 | Add `DataIntegrityViolationException` → 409 handler | Medium |
| 10.3-10.6 | Add unit tests + negative test cases | Medium |
| 10.8 | Add `@Transactional` on integration test class | Medium |
| 11.5 | Switch `ddl-auto` to `validate` in prod profile | High |

### 🟢 Nice to Have (5 items)

| # | Issue |
|---|-------|
| 7.8 | Standardised `ApiResponse<T>` wrapper |
| 11.1 | Spring Security integration |
| 11.2 | Rate limiting on search endpoint |
| 11.3 | URL validation on `imageUrl` |
| 11.6 | SLF4J logging in service methods |

---

## File Inventory (26 source files reviewed)

```
pgdiscovery/
├── controller/PgController.java                    ✅
├── dto/
│   ├── CreatePgRequest.java                        ✅
│   ├── ImageRequest.java                           ✅
│   ├── ImageResponse.java                          ✅
│   ├── OwnerProfileRequest.java                    ✅
│   ├── OwnerProfileResponse.java                   ✅
│   ├── PgResponse.java                             ✅
│   ├── PgSearchCriteria.java                       ⚠️ (3.9)
│   └── SharingOptionRequest/Response.java          ✅
├── mapper/
│   ├── ImageMapper.java                            ✅
│   ├── OwnerProfileMapper.java                     ✅
│   ├── PgMapper.java                               ✅
│   └── SharingOptionMapper.java                    ✅
├── model/
│   ├── Facility.java                               ✅
│   ├── Image.java                                  ✅
│   ├── OwnerProfile.java                           ⚠️ (2.2)
│   ├── Pg.java                                     ✅
│   ├── PgFacility.java                             ✅ (9.4 fixed)
│   └── SharingOption.java                          ✅
├── repository/
│   ├── FacilityRepository.java                     ✅
│   ├── OwnerProfileRepository.java                 ✅
│   └── PgRepository.java                           ⚠️ (5.4, 5.5)
├── service/
│   ├── PgSearchService.java                        ✅
│   ├── PgService.java                              ✅
│   └── impl/
│       ├── PgSearchServiceImpl.java                ✅ (6.7, 6.9 fixed)
│       └── PgServiceImpl.java                      ✅
├── specification/
│   └── PgSpecifications.java                       ✅ (6.9 fixed) ⚠️ (6.8)
common/
├── exception/GlobalExceptionHandler.java           ⚠️ (8.6, 8.7)
├── util/GeoUtils.java                              ✅
database/schema.sql                                 ✅ (9.4 fixed)
test/PgDiscoveryIntegrationTest.java                ⚠️ (10.3-10.8)
```

