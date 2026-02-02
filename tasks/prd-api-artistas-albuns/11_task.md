# Tarefa 11.0: Configurar Health Checks e documentação OpenAPI

<critical>Ler os arquivos de prd.md e techspec.md desta pasta, se você não ler esses arquivos sua tarefa será invalidada</critical>

## Visão Geral

Configurar observabilidade da aplicação com health checks (liveness e readiness) e documentação completa da API via OpenAPI/Swagger. Incluir verificações de saúde para banco de dados e MinIO.

<requirements>
- Usar `quarkus-smallrye-health` para health checks
- Implementar liveness probe (aplicação está viva)
- Implementar readiness probe (banco e MinIO disponíveis)
- Usar `quarkus-smallrye-openapi` para documentação
- Documentar todos os endpoints com anotações OpenAPI
- Configurar informações da API (título, versão, descrição)
- Swagger UI disponível em /q/swagger-ui
</requirements>

## Subtarefas

- [ ] 11.1 Verificar dependências smallrye-health e smallrye-openapi no pom.xml
- [ ] 11.2 Criar `DatabaseHealthCheck` implements HealthCheck
- [ ] 11.3 Criar `MinioHealthCheck` implements HealthCheck
- [ ] 11.4 Configurar informações OpenAPI no application.properties
- [ ] 11.5 Adicionar anotações @Operation em todos os endpoints
- [ ] 11.6 Adicionar anotações @APIResponse para códigos de retorno
- [ ] 11.7 Adicionar @Schema nos DTOs para documentar modelos
- [ ] 11.8 Adicionar @Tag para agrupar endpoints por recurso
- [ ] 11.9 Testar endpoints de health: /q/health, /q/health/live, /q/health/ready
- [ ] 11.10 Testar Swagger UI: /q/swagger-ui
- [ ] 11.11 Verificar que OpenAPI spec está completa: /q/openapi

## Detalhes de Implementação

**Health Check para Banco de Dados:**

```java
@Readiness
@ApplicationScoped
public class DatabaseHealthCheck implements HealthCheck {

    @Inject
    EntityManager entityManager;

    @Override
    public HealthCheckResponse call() {
        try {
            entityManager.createNativeQuery("SELECT 1").getSingleResult();
            return HealthCheckResponse.up("Database connection");
        } catch (Exception e) {
            return HealthCheckResponse.down("Database connection");
        }
    }
}
```

**Health Check para MinIO:**

```java
@Readiness
@ApplicationScoped
public class MinioHealthCheck implements HealthCheck {

    @Inject
    MinioClient minioClient;

    @ConfigProperty(name = "app.minio.bucket")
    String bucket;

    @Override
    public HealthCheckResponse call() {
        try {
            minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            return HealthCheckResponse.up("MinIO connection");
        } catch (Exception e) {
            return HealthCheckResponse.down("MinIO connection");
        }
    }
}
```

**Configuração OpenAPI no application.properties:**

```properties
quarkus.smallrye-openapi.info-title=API de Artistas e Álbuns
quarkus.smallrye-openapi.info-version=1.0.0
quarkus.smallrye-openapi.info-description=API REST para gerenciamento de catálogo musical
quarkus.smallrye-openapi.info-contact-name=Suporte
quarkus.smallrye-openapi.info-contact-email=suporte@example.com
quarkus.swagger-ui.always-include=true
```

**Exemplo de anotações OpenAPI em Resource:**

```java
@Path("/api/v1/artists")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Artists", description = "Operações de gerenciamento de artistas")
public class ArtistResource {

    @GET
    @Operation(summary = "Listar artistas", description = "Retorna lista de artistas com filtro e ordenação")
    @APIResponse(responseCode = "200", description = "Lista de artistas")
    @APIResponse(responseCode = "401", description = "Não autenticado")
    public List<ArtistResponse> list(
        @QueryParam("name") @Parameter(description = "Filtro por nome") String name,
        @QueryParam("sort") @Parameter(description = "Ordenação: name:asc ou name:desc") String sort
    ) { ... }

    @POST
    @Operation(summary = "Criar artista", description = "Cria um novo artista no catálogo")
    @APIResponse(responseCode = "201", description = "Artista criado")
    @APIResponse(responseCode = "400", description = "Dados inválidos")
    @APIResponse(responseCode = "401", description = "Não autenticado")
    @APIResponse(responseCode = "403", description = "Sem permissão")
    public Response create(
        @RequestBody(description = "Dados do artista") ArtistRequest request
    ) { ... }
}
```

**Exemplo de anotações @Schema em DTO:**

```java
@Schema(description = "Dados para criação de artista")
public record ArtistRequest(
    @Schema(description = "Nome do artista", example = "Queen", required = true)
    @NotBlank String name,

    @Schema(description = "Tipo do artista", example = "BAND", required = true)
    @NotNull ArtistType type
) {}
```

**Endpoints de Health:**

| Endpoint | Descrição |
|----------|-----------|
| `/q/health` | Status geral (liveness + readiness) |
| `/q/health/live` | Liveness probe |
| `/q/health/ready` | Readiness probe |

## Critérios de Sucesso

- [ ] /q/health retorna status UP quando tudo ok
- [ ] /q/health/ready retorna DOWN se banco indisponível
- [ ] /q/health/ready retorna DOWN se MinIO indisponível
- [ ] Swagger UI acessível em /q/swagger-ui
- [ ] Todos os endpoints documentados com @Operation
- [ ] Modelos (DTOs) documentados com @Schema
- [ ] OpenAPI spec em /q/openapi contém todos os endpoints
- [ ] Informações da API (título, versão) corretas

## Arquivos relevantes

- `src/main/java/com/quarkus/health/DatabaseHealthCheck.java`
- `src/main/java/com/quarkus/health/MinioHealthCheck.java`
- `src/main/java/com/quarkus/resource/ArtistResource.java` (adicionar anotações)
- `src/main/java/com/quarkus/resource/AlbumResource.java` (adicionar anotações)
- `src/main/java/com/quarkus/resource/AuthResource.java` (adicionar anotações)
- `src/main/java/com/quarkus/dto/request/*.java` (adicionar @Schema)
- `src/main/java/com/quarkus/dto/response/*.java` (adicionar @Schema)
- `src/main/resources/application.properties` (configurações OpenAPI)
