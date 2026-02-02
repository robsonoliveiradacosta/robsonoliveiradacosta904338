# Tarefa 10.0: Implementar sincronização de Regionais

<critical>Ler os arquivos de prd.md e techspec.md desta pasta, se você não ler esses arquivos sua tarefa será invalidada</critical>

## Visão Geral

Implementar sincronização de regionais com API externa usando REST Client e Scheduler. A sincronização deve ocorrer automaticamente às 04:00 diariamente e também via endpoint manual. Aplicar regras de inserção, inativação e atualização conforme PRD.

<requirements>
- Usar `quarkus-rest-client-jackson` para cliente REST
- Usar `quarkus-scheduler` para job agendado
- Sincronização diária às 04:00 (cron: 0 0 4 * * ?)
- Endpoint manual para disparo: POST /api/v1/regionals/sync (ADMIN)
- Endpoint de listagem: GET /api/v1/regionals (USER, ADMIN)
- Regras de sincronização:
  - Novo no externo → INSERT active=true
  - Ausente no externo → UPDATE active=false
  - Nome alterado → UPDATE antigo active=false + INSERT novo
</requirements>

## Subtarefas

- [x] 10.1 Criar `RegionalApiClient` interface com @RegisterRestClient
- [x] 10.2 Configurar REST Client no application.properties
- [x] 10.3 Criar `RegionalDto` para deserialização da resposta externa
- [x] 10.4 Criar `RegionalRepository` extends PanacheRepository
- [x] 10.5 Criar `RegionalSyncService` com lógica de sincronização
- [x] 10.6 Implementar regras: inserir novos, inativar ausentes, tratar alterações
- [x] 10.7 Criar `RegionalSyncScheduler` com @Scheduled(cron)
- [x] 10.8 Criar `RegionalResource` com endpoints /sync e listagem
- [x] 10.9 Adicionar tratamento de erros (timeout, API indisponível)
- [x] 10.10 Criar testes unitários para lógica de sincronização
- [x] 10.11 Criar testes de integração com WireMock para API externa
- [x] 10.12 Testar sincronização manual e verificar logs

## Detalhes de Implementação

Consultar a seção **"Pontos de Integração > API Externa de Regionais"** na techspec.md para configuração do cliente.

**Interface do REST Client:**

```java
@RegisterRestClient(configKey = "regional-api")
@Path("/v1")
public interface RegionalApiClient {

    @GET
    @Path("/regionais")
    List<RegionalDto> getRegionais();
}
```

**DTO para API externa:**

```java
public record RegionalDto(
    Integer id,
    String nome
) {}
```

**Configuração application.properties:**

```properties
quarkus.rest-client.regional-api.url=https://integrador-argus-api.geia.vip
quarkus.rest-client.regional-api.connect-timeout=5000
quarkus.rest-client.regional-api.read-timeout=30000
```

**Scheduler:**

```java
@ApplicationScoped
public class RegionalSyncScheduler {

    private static final Logger LOG = Logger.getLogger(RegionalSyncScheduler.class);

    @Inject
    RegionalSyncService syncService;

    @Scheduled(cron = "0 0 4 * * ?") // Diariamente às 04:00
    void scheduledSync() {
        LOG.info("Starting scheduled regional sync...");
        try {
            syncService.sync();
            LOG.info("Scheduled regional sync completed successfully");
        } catch (Exception e) {
            LOG.error("Scheduled regional sync failed", e);
        }
    }
}
```

**Lógica de sincronização:**

```java
@ApplicationScoped
public class RegionalSyncService {

    @Inject
    @RestClient
    RegionalApiClient apiClient;

    @Inject
    RegionalRepository repository;

    @Transactional
    public SyncResult sync() {
        List<RegionalDto> externos = apiClient.getRegionais();
        List<Regional> locais = repository.listAll();

        Map<Integer, Regional> locaisMap = locais.stream()
            .collect(Collectors.toMap(Regional::getId, r -> r));

        Set<Integer> idsExternos = new HashSet<>();
        int inserted = 0, updated = 0, deactivated = 0;

        for (RegionalDto externo : externos) {
            idsExternos.add(externo.id());
            Regional local = locaisMap.get(externo.id());

            if (local == null) {
                // Novo: inserir
                Regional novo = new Regional();
                novo.setId(externo.id());
                novo.setName(externo.nome());
                novo.setActive(true);
                repository.persist(novo);
                inserted++;
            } else if (!local.getName().equals(externo.nome())) {
                // Nome alterado: inativar antigo e criar novo
                local.setActive(false);
                Regional novo = new Regional();
                novo.setId(externo.id());
                novo.setName(externo.nome());
                novo.setActive(true);
                repository.persist(novo);
                updated++;
            }
        }

        // Inativar ausentes
        for (Regional local : locais) {
            if (local.getActive() && !idsExternos.contains(local.getId())) {
                local.setActive(false);
                deactivated++;
            }
        }

        return new SyncResult(inserted, updated, deactivated);
    }
}
```

**Endpoints:**

| Método | Path | Descrição | Acesso |
|--------|------|-----------|--------|
| GET | `/api/v1/regionals` | Lista regionais ativos | USER, ADMIN |
| POST | `/api/v1/regionals/sync` | Dispara sincronização | ADMIN |

## Critérios de Sucesso

- [x] REST Client conecta na API externa corretamente
- [x] Scheduler executa às 04:00 (verificar logs)
- [x] Endpoint manual dispara sincronização
- [x] Novos regionais são inseridos com active=true
- [x] Regionais ausentes são marcados active=false
- [x] Alteração de nome: antigo inativado, novo inserido
- [x] Timeout tratado graciosamente (não quebra a aplicação)
- [x] Listagem retorna apenas regionais ativos
- [x] Testes com WireMock validam cenários

## Arquivos relevantes

- `src/main/java/com/quarkus/integration/RegionalApiClient.java`
- `src/main/java/com/quarkus/dto/RegionalDto.java`
- `src/main/java/com/quarkus/repository/RegionalRepository.java`
- `src/main/java/com/quarkus/service/RegionalSyncService.java`
- `src/main/java/com/quarkus/scheduler/RegionalSyncScheduler.java`
- `src/main/java/com/quarkus/resource/RegionalResource.java`
- `src/test/java/com/quarkus/service/RegionalSyncServiceTest.java`
- `src/test/java/com/quarkus/resource/RegionalResourceTest.java`
