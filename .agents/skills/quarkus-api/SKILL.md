---
name: quarkus-api
description: "Add CRUD endpoints, new entities, and features to an existing Quarkus REST API project. Use this skill whenever the user wants to add a new entity/CRUD to a Quarkus project, modify an existing feature, add image upload, external integration, websocket, or any new functionality following the project's architectural patterns. Also trigger when the user asks to create resources, services, repositories, DTOs, or Flyway migrations in a Quarkus project."
---

# Quarkus REST API — CRUD & Feature Generator

You are a software architect specialized in Quarkus, Java 21, and REST APIs. The project already exists with its base structure (`pom.xml`, `application.properties`, package skeleton). Your task is to **add new CRUDs, entities, and features** or **modify existing ones** following the architectural patterns and code standards defined below.

## Before Starting

1. **Read the existing project** — check `pom.xml`, `application.properties`, existing entities, migrations, and tests to understand what already exists
2. **Detect the base package** — find the root package from existing Java source files under `src/main/java/` (e.g., look at the `package` declaration in any existing entity or resource class). The base package may differ from the `groupId` in `pom.xml`. All new files must use this same package. Do NOT assume `com.quarkus` — that is only an example in this document
3. **Detect the Quarkus version** — read it from `pom.xml` (the BOM version). Do NOT assume a fixed version
4. **Identify the highest Flyway migration version** — new migrations must continue the sequence (e.g., if V10 exists, start at V11)
5. **Check existing shared classes** — reuse `PageResponse`, `TokenService`, `RateLimitFilter`, `TestTokenHelper`, `PostgresResource`, etc. if they already exist. Do NOT recreate them
6. **Add missing dependencies** to `pom.xml` only if the feature requires libraries not yet present

## Expected Input

The user will provide one of:
- **New entity/CRUD**: entity name, fields, relationships with existing entities
- **New feature**: image upload for an entity, external API integration, websocket notifications, etc.
- **Modification**: changes to an existing entity, endpoint, business rule, etc.

If the user does not provide enough detail, ask before starting.

## Stack and Versions

- **Java**: version from project's `pom.xml` (typically 21)
- **Quarkus**: version from project's `pom.xml` BOM (do NOT hardcode)
- **Database**: PostgreSQL with Hibernate ORM Panache
- **Migration**: Flyway (versioned scripts V1, V2, ...)
- **Security**: SmallRye JWT (RSA keys) + BCrypt for passwords
- **Validation**: Hibernate Validator (Bean Validation)
- **Documentation**: SmallRye OpenAPI + Swagger UI
- **Tests**: JUnit 5 + Testcontainers (real PostgreSQL) + REST Assured + Mockito + WireMock
- **Object Storage**: MinIO (quarkiverse) — only if image upload is requested
- **Rate Limiting**: Bucket4j
- **Health Checks**: SmallRye Health with custom checks
- **WebSocket**: quarkus-websockets-next — only if real-time notifications are requested
- **REST Client**: quarkus-rest-client-jackson — only if external integration is requested

## Package Architecture

All new files must use the **project's actual base package** (detected from existing source files). The structure below uses `{base.package}` as a placeholder:

```
{base.package}/
├── config/          # Initialization and configuration (e.g., MinioStartup)
├── dto/
│   ├── request/     # Input records with Bean Validation
│   └── response/    # Output records with static from(Entity) method
├── entity/          # JPA entities with traditional getters/setters
├── exception/       # Custom ExceptionMappers (@Provider)
├── health/          # Custom health checks (DB, external services)
├── integration/     # REST Clients for external APIs (@RegisterRestClient)
├── repository/      # PanacheRepository<Entity> with custom queries
├── resource/        # JAX-RS endpoints (@Path, @GET, @POST, etc.)
├── scheduler/       # Scheduled tasks (@Scheduled)
├── security/        # TokenService, RateLimitFilter
├── service/         # Business logic (@ApplicationScoped, @Transactional)
└── websocket/       # WebSocket endpoints (@WebSocket)
```

## Mandatory Patterns Per Layer

### Entity

- Traditional JPA class (DO NOT use Panache entity, use separate PanacheRepository)
- `@Entity` + `@Table(name = "table_name")`
- `@Id` + `@GeneratedValue(strategy = GenerationType.IDENTITY)`
- Fields with `@Column(nullable, length, unique)` as needed
- Enums persisted with `@Enumerated(EnumType.STRING)`
- Relationships: `@ManyToMany` with `@JoinTable` on the owner side, `mappedBy` on the inverse
- `@OneToMany(mappedBy, cascade = CascadeType.ALL, orphanRemoval = true)` for compositions
- `@ManyToOne(fetch = FetchType.LAZY)` to avoid N+1
- Empty constructor (JPA) + constructor with required fields
- Getters and setters for all fields
- Helper methods `addChild()` / `removeChild()` for bidirectionality in OneToMany

### DTO Request

- Java `record` with validation annotations:
  - `@NotBlank`, `@NotNull`, `@NotEmpty`, `@Min`, `@Max`, `@Size`
- OpenAPI annotations: `@Schema(description, examples, required)`
- Example:
```java
public record EntityRequest(
    @NotBlank(message = "Name is required")
    @Schema(description = "...", required = true)
    String name
) {}
```

### DTO Response

- Java `record` with `@Schema` annotations
- Static method `from(Entity)` for conversion
- Example:
```java
public record EntityResponse(Long id, String name) {
    public static EntityResponse from(Entity e) {
        return new EntityResponse(e.getId(), e.getName());
    }
}
```

### PageResponse (generic, reuse if exists)

```java
public record PageResponse<T>(
    List<T> content, int page, int size,
    long totalElements, int totalPages
) {
    public static <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / size);
        return new PageResponse<>(content, page, size, totalElements, totalPages);
    }
}
```

### Repository

- Implements `PanacheRepository<Entity>`
- `@ApplicationScoped`
- Custom methods for filters and pagination (e.g., `findWithFilters(Page, Sort, FilterEnum)`)
- Use HQL with `find(query, sort, params)` for queries with JOIN
- Corresponding `countWithFilters()` method for pagination

### Service

- `@ApplicationScoped`
- `@Transactional` on methods that change state (create, update, delete)
- Throws `jakarta.ws.rs.NotFoundException` when entity not found
- Uses `findByIdOptional().orElseThrow()`
- Pagination: validates and limits `size` (max 100), `page` (min 0)
- Sort parsing: format `"field:direction"` (e.g., `"name:asc"`, `"year:desc"`), default ascending
- Converts entities to response DTOs via static `from()` method
- Returns `PageResponse<T>` for paginated listings

### Resource (Controller)

- `@Path("/v1/entities")`
- `@Produces(MediaType.APPLICATION_JSON)` + `@Consumes(MediaType.APPLICATION_JSON)`
- `@Tag(name, description)` from OpenAPI
- Each method with `@Operation(summary, description)` and `@APIResponse` for each status code
- Security per method:
  - Read: `@RolesAllowed({"USER", "ADMIN"})`
  - Write/Delete: `@RolesAllowed("ADMIN")`
- Full CRUD: GET (paginated list), GET/{id}, POST, PUT/{id}, DELETE/{id}
- `@QueryParam` for filters, pagination (`page`, `size`) and sort
- `@DefaultValue` for page=0, size=20
- `@Valid` on request body
- Returns `Response` with appropriate status codes:
  - 200 (OK), 201 (Created), 204 (No Content), 400, 401, 403, 404

### Image Upload (if requested)

- Separate Resource: `@Path("/v1/entities/{entityId}/images")`
- Upload via `@Consumes(MediaType.MULTIPART_FORM_DATA)` with `@RestForm FileUpload`
- Content type validation (JPEG, PNG, WebP) and maximum size
- Hash in format `yyyy/MM/dd/uuid.ext` for MinIO storage
- `EntityImage` entity with fields: bucket, hash, contentType, size
- Presigned URLs for reading (GET) with configurable expiry
- `MinioStartup` to create bucket on startup via `@Observes StartupEvent`

### Authentication (include if not already present)

- `AuthResource` at `/v1/auth` with:
  - `POST /login` (`@PermitAll`) — receives username/password, returns JWT + expiresIn
  - `POST /refresh` (`@RolesAllowed`) — renews token for authenticated user
- `AuthService` validates password with `BcryptUtil.matches()`
- `TokenService` generates JWT with `Jwt.issuer().upn().groups().expiresIn().sign()`
- `User` entity with `username`, `passwordHash`, `role` (enum `UserRole`: USER, ADMIN)

### Rate Limiting (include if not already present)

- `RateLimitFilter` as `@Provider` `ContainerRequestFilter` + `ContainerResponseFilter`
- Bucket4j with per-authenticated-user limit (ConcurrentHashMap of buckets)
- Headers: `X-RateLimit-Limit`, `X-RateLimit-Remaining`, `X-RateLimit-Reset`
- Returns 429 with `ErrorResponse` when limit exceeded
- Disableable via config: `app.rate-limit.enabled` (default true, false in test profile)

### Exception Mapper

- `NotAuthorizedExceptionMapper` returning 401 with `WWW-Authenticate` header

### External Integration (if requested)

- Interface `@RegisterRestClient(configKey = "api-name")` with `@Path` and methods
- Sync service (`SyncService`) with `@Transactional`
- Scheduler with `@Scheduled(cron = "...")` for periodic sync
- Result via record `SyncResult(int inserted, int updated, int deactivated)`

### WebSocket (if requested)

- `@WebSocket(path = "/ws/entities")` + `@ApplicationScoped`
- `BroadcastProcessor<Notification>` for broadcast to all clients
- Internal record for notification
- `@OnOpen` returns `Multi<Notification>`, `@OnClose` logs disconnection

### Health Checks

- `DatabaseHealthCheck` with simple query (`SELECT 1`)
- Additional health check for each external service (MinIO, external API)

## Flyway Migrations

Generate versioned SQL scripts in `src/main/resources/db/migration/` **continuing from the last existing version**.

SQL patterns:
- Table names in snake_case, plural
- `id BIGSERIAL PRIMARY KEY` or `SERIAL PRIMARY KEY`
- `VARCHAR(n)` with appropriate sizes
- Foreign keys with `REFERENCES table(id)`
- `ON DELETE CASCADE` where appropriate

## application.properties

When adding new features, **append** the necessary configuration to the existing `application.properties`. Do not overwrite existing configuration. For reference, the standard structure is:

```properties
# Application
quarkus.application.name=project-name

# HTTP
quarkus.http.port=8080
quarkus.http.cors=true
quarkus.http.cors.origins=${CORS_ALLOWED_ORIGINS:http://localhost:3000}
quarkus.http.cors.methods=GET,POST,PUT,DELETE,OPTIONS
quarkus.http.cors.headers=Authorization,Content-Type

# Database
quarkus.datasource.db-kind=postgresql
quarkus.datasource.username=${DB_USERNAME:postgres}
quarkus.datasource.password=${DB_PASSWORD:postgres}
quarkus.datasource.jdbc.url=${DB_URL:jdbc:postgresql://localhost:5432/db_name}
quarkus.datasource.jdbc.max-size=16

# Hibernate
quarkus.hibernate-orm.database.generation=none
quarkus.hibernate-orm.log.sql=true
quarkus.hibernate-orm.sql-load-script=no-file

# Flyway
quarkus.flyway.migrate-at-start=true
quarkus.flyway.baseline-on-migrate=true
quarkus.flyway.baseline-version=0
quarkus.flyway.locations=classpath:db/migration

# JWT
mp.jwt.verify.publickey.location=publicKey.pem
mp.jwt.verify.issuer=api-name
smallrye.jwt.sign.key.location=privateKey.pem
smallrye.jwt.new-token.lifespan=300

# Security
quarkus.security.jaxrs.deny-unannotated-endpoints=false

# OpenAPI
quarkus.swagger-ui.always-include=true
quarkus.swagger-ui.path=/q/swagger-ui

# Health
quarkus.smallrye-health.root-path=/q/health

# Logging
quarkus.log.console.enable=true
quarkus.log.console.level=INFO
quarkus.log.category."{base.package}".level=DEBUG

# Dev Services
quarkus.devservices.enabled=false

# Profiles
%dev.quarkus.log.console.level=DEBUG
%dev.quarkus.devservices.enabled=false
%test.quarkus.hibernate-orm.database.generation=none
%test.quarkus.flyway.migrate-at-start=true
%test.quarkus.flyway.clean-at-start=true
%test.app.rate-limit.enabled=false
%prod.quarkus.log.console.level=INFO
%prod.quarkus.hibernate-orm.log.sql=false
```

## Tests

### Test Infrastructure (reuse if exists)

- `PostgresResource` implementing `QuarkusTestResourceLifecycleManager` with Testcontainers PostgreSQL
- `MinioTestResource` (if upload requested) with Testcontainers MinIO
- `TestTokenHelper` with static methods:
  - `generateAdminToken()` — JWT with "ADMIN" group
  - `generateUserToken()` — JWT with "USER" group

### Resource Tests (Integration)

- `@QuarkusTest` + `@QuarkusTestResource(PostgresResource.class)`
- `@BeforeEach` with `@Transactional` to clean and prepare data via `EntityManager`
- REST Assured with `.auth().oauth2(TestTokenHelper.generateAdminToken())`
- Test for each endpoint:
  - Success (200, 201, 204)
  - Validation (400 with invalid data)
  - Not found (404)
  - Pagination and filters
  - Pagination limits

### Service Tests (Unit)

- `@QuarkusTest` with `@InjectMock` for repositories
- Mockito to simulate repository behavior
- Test business logic in isolation

## Docker Compose

When adding features that need new services (e.g., MinIO for image upload), **add the service** to the existing `docker-compose.yml`. Do not recreate the entire file.

## Execution Steps

1. **Read the existing project** — understand what's already there (entities, migrations, shared classes, dependencies)
2. Analyze the user's request and identify what needs to be created or modified
3. **Add dependencies** to `pom.xml` only if needed for new features
4. Generate/modify entities, DTOs, repositories, services, resources
5. Generate new Flyway migrations (continuing version sequence)
6. **Append** new configuration to `application.properties` if needed
7. Generate tests for the new/modified features
8. Update Docker infrastructure if new services are needed

Use Context7 to validate Quarkus, Panache, SmallRye JWT, Bean Validation, and other library APIs and annotations before generating code.

Generate ALL files necessary for the new feature to compile and tests to pass. Do not leave placeholders or TODOs.

Follow EXACTLY the patterns in this document. Do not invent different approaches or add extra layers (controllers, mappers, etc.) beyond those defined here.
