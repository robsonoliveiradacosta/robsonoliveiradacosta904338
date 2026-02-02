# Tarefa 4.0: Implementar CRUD de Artistas

<critical>Ler os arquivos de prd.md e techspec.md desta pasta, se você não ler esses arquivos sua tarefa será invalidada</critical>

## Visão Geral

Implementar o CRUD completo de Artistas seguindo a arquitetura em camadas (Repository → Service → Resource). Inclui DTOs de request/response, validação, filtros de busca, ordenação e testes.

<requirements>
- Seguir estrutura de pacotes definida na techspec
- Usar PanacheRepository para acesso a dados
- Implementar filtro por nome e ordenação alfabética (asc/desc)
- Validar entrada com Bean Validation (@NotBlank, @NotNull, etc.)
- Hard delete (exclusão permanente)
- Documentar endpoints com OpenAPI annotations
- **NOTA:** Segurança (JWT/@RolesAllowed) será adicionada na Tarefa 6.0
</requirements>

## Subtarefas

- [ ] 4.1 Criar `ArtistRepository` extends `PanacheRepository<Artist>`
- [ ] 4.2 Implementar query customizada para busca por nome com ordenação
- [ ] 4.3 Criar DTOs: `ArtistRequest` e `ArtistResponse`
- [ ] 4.4 Criar `ArtistService` com lógica de negócio
- [ ] 4.5 Criar `ArtistResource` com endpoints REST
- [ ] 4.6 Adicionar validação Bean Validation nos DTOs
- [ ] 4.7 Adicionar anotações OpenAPI nos endpoints
- [ ] 4.8 Criar testes unitários para `ArtistService`
- [ ] 4.9 Criar testes de integração para `ArtistResource`
- [ ] 4.10 Testar manualmente via Swagger UI ou curl

## Detalhes de Implementação

Consultar a seção **"Endpoints de API > Artistas"** na techspec.md para a especificação dos endpoints.

**Endpoints a implementar:**

| Método | Path | Descrição |
|--------|------|-----------|
| GET | `/api/v1/artists` | Lista com filtro e ordenação |
| GET | `/api/v1/artists/{id}` | Busca por ID |
| POST | `/api/v1/artists` | Cria novo artista |
| PUT | `/api/v1/artists/{id}` | Atualiza artista |
| DELETE | `/api/v1/artists/{id}` | Remove artista |

**Query params para listagem:**
- `name` (string): filtro por nome (LIKE case-insensitive)
- `sort` (string): `name:asc` ou `name:desc`

**Exemplo de Repository com query customizada:**

```java
@ApplicationScoped
public class ArtistRepository implements PanacheRepository<Artist> {

    public List<Artist> findByNameContaining(String name, Sort sort) {
        if (name == null || name.isBlank()) {
            return listAll(sort);
        }
        return list("LOWER(name) LIKE LOWER(?1)", sort, "%" + name + "%");
    }
}
```

## Critérios de Sucesso

- [ ] 5 endpoints funcionando corretamente
- [ ] Filtro por nome funciona com busca parcial case-insensitive
- [ ] Ordenação por nome (asc/desc) funciona
- [ ] Validação retorna 400 Bad Request para dados inválidos
- [ ] DELETE retorna 204 No Content
- [ ] Testes unitários cobrem casos de sucesso e erro
- [ ] Testes de integração validam fluxo completo
- [ ] Endpoints documentados no Swagger UI

## Arquivos relevantes

- `src/main/java/com/quarkus/repository/ArtistRepository.java`
- `src/main/java/com/quarkus/service/ArtistService.java`
- `src/main/java/com/quarkus/resource/ArtistResource.java`
- `src/main/java/com/quarkus/dto/request/ArtistRequest.java`
- `src/main/java/com/quarkus/dto/response/ArtistResponse.java`
- `src/test/java/com/quarkus/service/ArtistServiceTest.java`
- `src/test/java/com/quarkus/resource/ArtistResourceTest.java`
