# Tarefa 5.0: Implementar CRUD de Álbuns

<critical>Ler os arquivos de prd.md e techspec.md desta pasta, se você não ler esses arquivos sua tarefa será invalidada</critical>

## Visão Geral

Implementar o CRUD completo de Álbuns seguindo a arquitetura em camadas. Inclui paginação, filtro por tipo de artista, ordenação, gerenciamento do relacionamento N:N com artistas e testes.

<requirements>
- Seguir estrutura de pacotes definida na techspec
- Usar PanacheRepository para acesso a dados
- Implementar paginação (page, size) com limite máximo de 100
- Implementar filtro por tipo de artista (SINGER/BAND)
- Implementar ordenação por título e ano
- Gerenciar relacionamento N:N com artistas (vincular/desvincular)
- Hard delete (exclusão permanente)
- **NOTA:** Segurança (JWT/@RolesAllowed) será adicionada na Tarefa 6.0
- **NOTA:** Notificação WebSocket será adicionada na Tarefa 9.0
</requirements>

## Subtarefas

- [ ] 5.1 Criar `AlbumRepository` extends `PanacheRepository<Album>`
- [ ] 5.2 Implementar queries customizadas para paginação e filtros
- [ ] 5.3 Criar DTOs: `AlbumRequest`, `AlbumResponse`, `PageResponse<T>`
- [ ] 5.4 Criar `AlbumService` com lógica de negócio
- [ ] 5.5 Implementar vinculação de artistas ao criar/atualizar álbum
- [ ] 5.6 Criar `AlbumResource` com endpoints REST
- [ ] 5.7 Adicionar validação Bean Validation nos DTOs
- [ ] 5.8 Adicionar anotações OpenAPI nos endpoints
- [ ] 5.9 Criar testes unitários para `AlbumService`
- [ ] 5.10 Criar testes de integração para `AlbumResource`
- [ ] 5.11 Testar paginação e filtros manualmente

## Detalhes de Implementação

Consultar a seção **"Endpoints de API > Álbuns"** na techspec.md para a especificação dos endpoints.

**Endpoints a implementar:**

| Método | Path | Descrição |
|--------|------|-----------|
| GET | `/api/v1/albums` | Lista paginada com filtros |
| GET | `/api/v1/albums/{id}` | Busca por ID |
| POST | `/api/v1/albums` | Cria novo álbum |
| PUT | `/api/v1/albums/{id}` | Atualiza álbum |
| DELETE | `/api/v1/albums/{id}` | Remove álbum |

**Query params para listagem:**
- `page` (int, default 0)
- `size` (int, default 20, max 100)
- `sort` (string): `title:asc`, `title:desc`, `year:asc`, `year:desc`
- `artistType` (string): `SINGER` ou `BAND`

**Exemplo de AlbumRequest:**

```java
public record AlbumRequest(
    @NotBlank String title,
    @NotNull @Min(1900) @Max(2100) Integer year,
    @NotEmpty List<Long> artistIds
) {}
```

**Exemplo de PageResponse:**

```java
public record PageResponse<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages
) {}
```

**Filtro por tipo de artista:**
Requer JOIN com tabela de artistas para filtrar álbuns que tenham pelo menos um artista do tipo especificado.

## Critérios de Sucesso

- [ ] 5 endpoints funcionando corretamente
- [ ] Paginação funciona com page, size e totalElements corretos
- [ ] Filtro por artistType retorna apenas álbuns com artistas do tipo
- [ ] Ordenação por title e year funciona (asc/desc)
- [ ] Vinculação de artistas funciona no create e update
- [ ] DELETE remove álbum e limpa relacionamentos
- [ ] Testes cobrem paginação, filtros e casos de erro
- [ ] Endpoints documentados no Swagger UI

## Arquivos relevantes

- `src/main/java/com/quarkus/repository/AlbumRepository.java`
- `src/main/java/com/quarkus/service/AlbumService.java`
- `src/main/java/com/quarkus/resource/AlbumResource.java`
- `src/main/java/com/quarkus/dto/request/AlbumRequest.java`
- `src/main/java/com/quarkus/dto/response/AlbumResponse.java`
- `src/main/java/com/quarkus/dto/response/PageResponse.java`
- `src/test/java/com/quarkus/service/AlbumServiceTest.java`
- `src/test/java/com/quarkus/resource/AlbumResourceTest.java`
