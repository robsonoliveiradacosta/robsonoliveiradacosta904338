# Tech Spec - API REST de Artistas e Álbuns

## Resumo Executivo

Esta especificação técnica detalha a implementação de uma API REST para gerenciamento de catálogo musical usando Quarkus 3.31.1 com Java 21. A arquitetura segue o padrão de camadas (Resource → Service → Repository) com Hibernate Panache para persistência, SmallRye JWT para autenticação, WebSockets Next para notificações em tempo real, e integração com MinIO para armazenamento de imagens.

A estratégia de implementação prioriza a construção incremental: primeiro a camada de dados e migrations, seguida pelos endpoints CRUD básicos, depois segurança JWT, e por fim as integrações (MinIO, WebSocket, API externa de regionais).

## Arquitetura do Sistema

### Visão Geral dos Componentes

```
┌─────────────────────────────────────────────────────────────────┐
│                        API Gateway (REST)                        │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐              │
│  │ ArtistResource│ │AlbumResource│ │AuthResource │ ...          │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘              │
├─────────┼────────────────┼────────────────┼─────────────────────┤
│         │    Service Layer (Business Logic)                      │
│  ┌──────▼──────┐  ┌──────▼──────┐  ┌──────▼──────┐              │
│  │ArtistService│  │AlbumService │  │ AuthService │ ...          │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘              │
├─────────┼────────────────┼────────────────┼─────────────────────┤
│         │    Repository Layer (Data Access)                      │
│  ┌──────▼──────┐  ┌──────▼──────┐  ┌──────▼──────┐              │
│  │ArtistRepository│AlbumRepository│ │UserRepository│ ...        │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘              │
├─────────┼────────────────┼────────────────┼─────────────────────┤
│         └────────────────┴────────────────┘                      │
│                      PostgreSQL Database                         │
└─────────────────────────────────────────────────────────────────┘

┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│   MinIO S3   │  │  WebSocket   │  │ External API │
│ (Imagens)    │  │  (Notif.)    │  │ (Regionais)  │
└──────────────┘  └──────────────┘  └──────────────┘
```

**Componentes principais:**

| Componente | Responsabilidade |
|------------|------------------|
| Resource | Endpoints REST, validação de entrada, documentação OpenAPI |
| Service | Lógica de negócio, orquestração, transações |
| Repository | Acesso a dados via Panache, queries customizadas |
| Entity | Mapeamento ORM, relacionamentos JPA |
| Security | Filtros JWT, geração de tokens, RBAC |
| Integration | Clientes para MinIO e API externa |
| WebSocket | Broadcast de notificações para clientes conectados |
| Scheduler | Jobs agendados (sincronização de regionais) |

## Design de Implementação

### Estrutura de Pacotes

```
com.quarkus
├── entity/
│   ├── Artist.java
│   ├── Album.java
│   ├── User.java
│   └── Regional.java
├── repository/
│   ├── ArtistRepository.java
│   ├── AlbumRepository.java
│   ├── UserRepository.java
│   └── RegionalRepository.java
├── service/
│   ├── ArtistService.java
│   ├── AlbumService.java
│   ├── AuthService.java
│   ├── ImageService.java
│   ├── RegionalSyncService.java
│   └── NotificationService.java
├── resource/
│   ├── ArtistResource.java
│   ├── AlbumResource.java
│   ├── AuthResource.java
│   ├── RegionalResource.java
│   └── ImageResource.java
├── dto/
│   ├── request/
│   │   ├── ArtistRequest.java
│   │   ├── AlbumRequest.java
│   │   └── LoginRequest.java
│   └── response/
│       ├── ArtistResponse.java
│       ├── AlbumResponse.java
│       ├── TokenResponse.java
│       └── PageResponse.java
├── security/
│   ├── TokenService.java
│   └── RateLimitFilter.java
├── websocket/
│   └── AlbumNotificationSocket.java
├── integration/
│   ├── MinioClient.java
│   └── RegionalApiClient.java
├── scheduler/
│   └── RegionalSyncScheduler.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   └── BusinessException.java
└── config/
    ├── MinioConfig.java
    └── CorsConfig.java
```

### Modelos de Dados

#### Entidades JPA

```java
// Artist.java
@Entity
@Table(name = "artists")
public class Artist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ArtistType type; // SINGER, BAND

    @ManyToMany(mappedBy = "artists")
    private Set<Album> albums = new HashSet<>();
}

// Album.java
@Entity
@Table(name = "albums")
public class Album {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false)
    private Integer year;

    @ManyToMany
    @JoinTable(
        name = "album_artist",
        joinColumns = @JoinColumn(name = "album_id"),
        inverseJoinColumns = @JoinColumn(name = "artist_id")
    )
    private Set<Artist> artists = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "album_images", joinColumns = @JoinColumn(name = "album_id"))
    @Column(name = "image_key")
    private List<String> imageKeys = new ArrayList<>();
}

// User.java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role; // ADMIN, USER
}

// Regional.java
@Entity
@Table(name = "regionals")
public class Regional {
    @Id
    private Integer id; // ID vem da API externa

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false)
    private Boolean active = true;
}
```

#### Diagrama ER

```
┌─────────────┐       ┌──────────────┐       ┌─────────────┐
│   artists   │       │ album_artist │       │   albums    │
├─────────────┤       ├──────────────┤       ├─────────────┤
│ id (PK)     │◄──────│ artist_id(FK)│       │ id (PK)     │
│ name        │       │ album_id(FK) │──────►│ title       │
│ type        │       └──────────────┘       │ year        │
└─────────────┘                              └──────┬──────┘
                                                    │
                                             ┌──────▼──────┐
                                             │album_images │
                                             ├─────────────┤
                                             │ album_id(FK)│
                                             │ image_key   │
                                             └─────────────┘

┌─────────────┐       ┌─────────────┐
│    users    │       │  regionals  │
├─────────────┤       ├─────────────┤
│ id (PK)     │       │ id (PK)     │
│ username    │       │ name        │
│ password    │       │ active      │
│ role        │       └─────────────┘
└─────────────┘
```

### Endpoints de API

#### Autenticação (`/api/v1/auth`)

| Método | Path | Descrição | Acesso |
|--------|------|-----------|--------|
| `POST` | `/login` | Autenticação, retorna JWT | Público |
| `POST` | `/refresh` | Renova token JWT | Autenticado |

#### Artistas (`/api/v1/artists`)

| Método | Path | Descrição | Acesso |
|--------|------|-----------|--------|
| `GET` | `/` | Lista artistas com filtro e ordenação | USER, ADMIN |
| `GET` | `/{id}` | Busca artista por ID | USER, ADMIN |
| `POST` | `/` | Cria novo artista | ADMIN |
| `PUT` | `/{id}` | Atualiza artista | ADMIN |
| `DELETE` | `/{id}` | Remove artista (hard delete) | ADMIN |

**Query params para listagem:**
- `name` (string): filtro por nome (LIKE)
- `sort` (string): `name:asc` ou `name:desc`

#### Álbuns (`/api/v1/albums`)

| Método | Path | Descrição | Acesso |
|--------|------|-----------|--------|
| `GET` | `/` | Lista álbuns paginados | USER, ADMIN |
| `GET` | `/{id}` | Busca álbum por ID | USER, ADMIN |
| `POST` | `/` | Cria novo álbum (dispara WebSocket) | ADMIN |
| `PUT` | `/{id}` | Atualiza álbum | ADMIN |
| `DELETE` | `/{id}` | Remove álbum (hard delete) | ADMIN |

**Query params para listagem:**
- `page` (int, default 0)
- `size` (int, default 20, max 100)
- `sort` (string): `title:asc`, `year:desc`
- `artistType` (string): `SINGER` ou `BAND`

#### Imagens (`/api/v1/albums/{albumId}/images`)

| Método | Path | Descrição | Acesso |
|--------|------|-----------|--------|
| `POST` | `/` | Upload de imagem(ns) | ADMIN |
| `GET` | `/{imageKey}` | Retorna URL pré-assinada (30 min) | USER, ADMIN |
| `DELETE` | `/{imageKey}` | Remove imagem | ADMIN |

#### Regionais (`/api/v1/regionals`)

| Método | Path | Descrição | Acesso |
|--------|------|-----------|--------|
| `GET` | `/` | Lista regionais ativos | USER, ADMIN |
| `POST` | `/sync` | Dispara sincronização manual | ADMIN |

### Formato de Respostas

```java
// Resposta paginada
public record PageResponse<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages
) {}

// Token response
public record TokenResponse(
    String accessToken,
    String tokenType,
    long expiresIn
) {}

// Error response
public record ErrorResponse(
    int status,
    String message,
    String timestamp,
    String path
) {}
```

## Pontos de Integração

### MinIO

**Dependência:** `io.quarkiverse.minio:quarkus-minio`

**Configuração:**
```properties
quarkus.minio.host=http://minio
quarkus.minio.port=9000
quarkus.minio.secure=false
quarkus.minio.access-key=${MINIO_ACCESS_KEY}
quarkus.minio.secret-key=${MINIO_SECRET_KEY}

# Bucket padrão para imagens
app.minio.bucket=album-images
```

**Uso:**
```java
@ApplicationScoped
public class ImageService {

    @Inject
    MinioClient minioClient;

    @ConfigProperty(name = "app.minio.bucket")
    String bucket;

    public void upload(String objectName, InputStream stream, long size, String contentType) {
        minioClient.putObject(PutObjectArgs.builder()
            .bucket(bucket)
            .object(objectName)
            .stream(stream, size, -1)
            .contentType(contentType)
            .build());
    }

    public String getPresignedUrl(String objectName) {
        return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
            .bucket(bucket)
            .object(objectName)
            .method(Method.GET)
            .expiry(30, TimeUnit.MINUTES)
            .build());
    }

    public void delete(String objectName) {
        minioClient.removeObject(RemoveObjectArgs.builder()
            .bucket(bucket)
            .object(objectName)
            .build());
    }
}
```

**Operações:**
- Upload: `MinioClient.putObject()` com stream
- Download: `MinioClient.getPresignedObjectUrl()` (TTL 30 min)
- Delete: `MinioClient.removeObject()`

**Tratamento de erros:**
- Timeout: 30s para upload
- Retry: 3 tentativas com backoff exponencial
- Fallback: Retornar erro 503 se MinIO indisponível

### API Externa de Regionais

**Endpoint:** `https://integrador-argus-api.geia.vip/v1/regionais`

**Cliente REST:**
```java
@RegisterRestClient(configKey = "regional-api")
@Path("/v1")
public interface RegionalApiClient {
    @GET
    @Path("/regionais")
    List<RegionalDto> getRegionals();
}
```

**Configuração:**
```properties
quarkus.rest-client.regional-api.url=https://integrador-argus-api.geia.vip
quarkus.rest-client.regional-api.connect-timeout=5000
quarkus.rest-client.regional-api.read-timeout=30000
```

**Lógica de sincronização:**
1. Buscar todos regionais da API externa
2. Comparar com registros locais:
   - Novo → INSERT com `active=true`
   - Ausente → UPDATE `active=false`
   - Nome alterado → UPDATE antigo `active=false`, INSERT novo

## Segurança

### JWT com SmallRye

**Dependências:**
- `quarkus-smallrye-jwt`
- `quarkus-smallrye-jwt-build`

**Configuração:**
```properties
mp.jwt.verify.publickey.location=publicKey.pem
mp.jwt.verify.issuer=quarkus-api
smallrye.jwt.sign.key.location=privateKey.pem
smallrye.jwt.new-token.lifespan=300  # 5 minutos
```

**Geração de token:**
```java
@ApplicationScoped
public class TokenService {
    public String generateToken(User user) {
        return Jwt.issuer("quarkus-api")
            .upn(user.getUsername())
            .groups(Set.of(user.getRole().name()))
            .expiresIn(Duration.ofMinutes(5))
            .sign();
    }
}
```

### Rate Limiting com Bucket4j

**Dependência:** `bucket4j-core`

**Implementação via filtro JAX-RS:**
```java
@Provider
@Priority(Priorities.AUTHENTICATION + 1)
public class RateLimitFilter implements ContainerRequestFilter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket createBucket() {
        return Bucket.builder()
            .addLimit(Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1))))
            .build();
    }

    @Override
    public void filter(ContainerRequestContext ctx) {
        String userId = ctx.getSecurityContext().getUserPrincipal().getName();
        Bucket bucket = buckets.computeIfAbsent(userId, k -> createBucket());

        if (!bucket.tryConsume(1)) {
            ctx.abortWith(Response.status(429)
                .entity(new ErrorResponse(429, "Too Many Requests", ...))
                .build());
        }
    }
}
```

### CORS

```properties
quarkus.http.cors=true
quarkus.http.cors.origins=${CORS_ALLOWED_ORIGINS:http://localhost:3000}
quarkus.http.cors.methods=GET,POST,PUT,DELETE,OPTIONS
quarkus.http.cors.headers=Authorization,Content-Type
```

## WebSocket

**Dependência:** `quarkus-websockets-next`

```java
@WebSocket(path = "/ws/albums")
@ApplicationScoped
public class AlbumNotificationSocket {

    @Inject
    WebSocketConnection connection;

    public record AlbumNotification(Long id, String title, Integer year) {}

    public void broadcast(AlbumNotification notification) {
        connection.broadcast().sendTextAndAwait(notification);
    }
}
```

**Integração com AlbumService:**
```java
@Transactional
public Album create(AlbumRequest request) {
    Album album = // ... save album
    notificationSocket.broadcast(new AlbumNotification(album.getId(), album.getTitle(), album.getYear()));
    return album;
}
```

## Abordagem de Testes

### Testes Unitários

**Componentes a testar:**
- Services: lógica de negócio isolada
- TokenService: geração e validação de JWT
- Rate limiting: lógica do bucket

**Mocks necessários:**
- Repositories (Mockito)
- MinIO Client
- REST Client para API externa

**Cenários críticos:**
- Sincronização de regionais (inserção, inativação, atualização)
- Validação de regras de negócio (artista sem álbum, álbum sem artista)
- Rate limiting (consumo e reset do bucket)

### Testes de Integração

**Dependências:**
- `quarkus-junit5`
- `rest-assured`
- Testcontainers para PostgreSQL e MinIO

```java
@QuarkusTest
@TestHTTPEndpoint(ArtistResource.class)
class ArtistResourceTest {

    @Test
    void shouldCreateArtist() {
        given()
            .auth().oauth2(adminToken)
            .contentType(ContentType.JSON)
            .body(new ArtistRequest("Queen", ArtistType.BAND))
        .when()
            .post()
        .then()
            .statusCode(201)
            .body("name", equalTo("Queen"));
    }
}
```

**Cenários de integração:**
- Fluxo completo de autenticação
- CRUD de artistas e álbuns
- Upload e recuperação de imagens
- WebSocket broadcast
- Rate limiting end-to-end

## Sequenciamento de Desenvolvimento

### Ordem de Construção

1. **Fase 1 - Fundação (Entities + Migrations)**
   - Configurar dependências no pom.xml
   - Criar entidades JPA (Artist, Album, User, Regional)
   - Migrations Flyway para schema inicial e dados de exemplo
   - Configurar application.properties

2. **Fase 2 - CRUD Básico**
   - Repositories com PanacheRepository
   - Services com lógica de negócio
   - Resources REST com validação
   - DTOs de request/response
   - Testes unitários e de integração

3. **Fase 3 - Segurança**
   - Configurar SmallRye JWT
   - Implementar AuthService e TokenService
   - Endpoint de login e refresh
   - Anotações @RolesAllowed nos endpoints
   - Rate limiting com Bucket4j

4. **Fase 4 - Integrações**
   - MinIO: upload, presigned URLs, delete
   - WebSocket: notificação de novos álbuns
   - REST Client: API de regionais
   - Scheduler: job diário às 04:00

5. **Fase 5 - Observabilidade e Finalização**
   - Health checks (liveness/readiness)
   - Documentação OpenAPI completa
   - Docker Compose com todos os serviços
   - Testes E2E

### Dependências Técnicas

**Infraestrutura requerida:**
- PostgreSQL 15+
- MinIO (ou S3 compatível)
- Rede Docker para comunicação entre containers

**Dependências Maven a adicionar:**
```xml
<!-- Persistência -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-hibernate-orm-panache</artifactId>
</dependency>
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-jdbc-postgresql</artifactId>
</dependency>
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-flyway</artifactId>
</dependency>

<!-- Segurança -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-smallrye-jwt</artifactId>
</dependency>
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-smallrye-jwt-build</artifactId>
</dependency>

<!-- WebSocket -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-websockets-next</artifactId>
</dependency>

<!-- Scheduler -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-scheduler</artifactId>
</dependency>

<!-- MinIO -->
<dependency>
    <groupId>io.quarkiverse.minio</groupId>
    <artifactId>quarkus-minio</artifactId>
</dependency>

<!-- REST Client -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-rest-client-jackson</artifactId>
</dependency>

<!-- Validação -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-hibernate-validator</artifactId>
</dependency>

<!-- OpenAPI -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-smallrye-openapi</artifactId>
</dependency>

<!-- Health -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-smallrye-health</artifactId>
</dependency>

<!-- Rate Limiting -->
<dependency>
    <groupId>com.bucket4j</groupId>
    <artifactId>bucket4j-core</artifactId>
    <version>8.10.1</version>
</dependency>

<!-- Testes -->
<dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>rest-assured</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <scope>test</scope>
</dependency>
```

## Considerações Técnicas

### Decisões Principais

| Decisão | Justificativa | Alternativas Consideradas |
|---------|---------------|---------------------------|
| Repository Pattern (Panache) | Melhor testabilidade, separação de responsabilidades | Active Record (PanacheEntity) - menos flexível para queries complexas |
| Tabela de junção simples | Requisito não menciona atributos na relação N:N | Entidade intermediária - over-engineering |
| Bucket4j para rate limiting | Leve, in-memory, sem dependência externa | Redis-based - complexidade desnecessária para demo |
| JWT local (não Keycloak) | Simplicidade, controle total, demo-friendly | Keycloak - overhead operacional para escopo do projeto |
| WebSockets Next | API moderna do Quarkus, mais idiomática | WebSocket clássico - menos features |
| Quarkiverse MinIO | Cliente nativo MinIO, API mais direta, suporte a presigned URLs | quarkus-amazon-s3 - mais genérico mas overhead para uso exclusivo com MinIO |

### Riscos Conhecidos

| Risco | Probabilidade | Impacto | Mitigação |
|-------|---------------|---------|-----------|
| MinIO indisponível | Média | Alto | Circuit breaker, fallback com erro gracioso |
| API regionais timeout | Média | Médio | Timeout configurável, retry com backoff |
| Rate limit em memória (perda em restart) | Baixa | Baixo | Aceitável para demo; produção usaria Redis |
| Chaves JWT em arquivo | Baixa | Alto | Variáveis de ambiente em produção |

### Conformidade com Padrões

Conforme `.claude/rules/quarkus.md`:

- **Estrutura de pacotes**: Por camada (resources, services, repositories, entities) ✓
- **Naming conventions**: PascalCase para classes, camelCase para métodos ✓
- **CDI**: @ApplicationScoped, @Inject para DI ✓
- **Configuração**: application.properties com @ConfigProperty ✓
- **Testes**: JUnit 5 com @QuarkusTest e rest-assured ✓
- **Segurança**: SmallRye JWT com @RolesAllowed ✓
- **API Docs**: OpenAPI via quarkus-smallrye-openapi ✓
- **Health**: MicroProfile Health para liveness/readiness ✓

### Arquivos Relevantes

**Configuração:**
- `pom.xml` - dependências Maven
- `src/main/resources/application.properties` - configuração da aplicação
- `src/main/resources/db/migration/` - scripts Flyway
- `src/main/resources/publicKey.pem` - chave pública JWT
- `src/main/resources/privateKey.pem` - chave privada JWT

**Docker:**
- `docker-compose.yml` - orquestração (PostgreSQL, MinIO, App)
- `src/main/docker/Dockerfile.jvm` - build JVM

**Documentação:**
- `tasks/prd-api-artistas-albuns/prd.md` - requisitos do produto
