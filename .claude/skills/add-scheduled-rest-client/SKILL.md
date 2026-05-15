---
name: add-scheduled-rest-client
description: Add a periodic synchronization job that consumes an external REST API in a Quarkus project — generates a MicroProfile REST Client interface, a sync service (@Transactional), a @Scheduled cron job, an admin-only manual-trigger endpoint, and a WireMock-backed integration test stub. Use whenever the user wants to poll an external API on a schedule, sync data with a third party, or "fetch X every N hours".
---

# add-scheduled-rest-client

Generate a complete external-API-sync slice: REST Client interface, sync service with idempotent upsert, scheduled trigger, manual-trigger endpoint, and a WireMock-stubbed test, matching this repo's `RegionalApiClient` + `RegionalSyncService` + `RegionalSyncScheduler` pattern.

## When to invoke

- "Sync regional data daily"
- "Poll API X every hour and update our database"
- "Add a scheduled job to fetch and upsert Y"

## Inputs to collect

| Input | Default / notes |
|---|---|
| External API base URL property | `quarkus.rest-client.<key>.url` — pick a stable config key |
| Endpoint path on remote API | e.g. `/v1/regionais` |
| Response DTO shape | record matching the remote JSON |
| Local entity name | what we persist locally |
| Cron expression | confirm with user; example `0 0 4 * * ?` (4 AM daily) |
| Admin manual-trigger endpoint? | yes — generate `POST /api/v1/<resource>/sync` for `ADMIN` |

## Dependencies

```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-rest-client-jackson</artifactId>
</dependency>
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-scheduler</artifactId>
</dependency>

<!-- Test stub -->
<dependency>
    <groupId>io.quarkiverse.wiremock</groupId>
    <artifactId>quarkus-wiremock-test</artifactId>
    <version>1.5.3</version>
    <scope>test</scope>
</dependency>
```

## `application.properties` additions

```properties
quarkus.rest-client.{{key}}.url=${EXTERNAL_API_URL:https://example.com}
quarkus.rest-client.{{key}}.scope=jakarta.enterprise.context.ApplicationScoped
quarkus.rest-client.{{key}}.connect-timeout=5000
quarkus.rest-client.{{key}}.read-timeout=30000

quarkus.scheduler.enabled=true

# Tests: redirect to WireMock dev service
%test.quarkus.rest-client.{{key}}.url=${quarkus.wiremock.devservices.url}
```

## Files to generate

### `dto/<Remote>Dto.java`

```java
package {{packageRoot}}.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RegionalDto(Long id, String name, String code) {}
```

> `@JsonIgnoreProperties(ignoreUnknown = true)` is non-negotiable for external APIs — they will add fields you didn't anticipate.

### `integration/<Remote>ApiClient.java`

```java
package {{packageRoot}}.integration;

import {{packageRoot}}.dto.RegionalDto;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

@RegisterRestClient(configKey = "{{key}}")
@Path("/v1")
public interface RegionalApiClient {

    @GET @Path("/regionais")
    @Produces(MediaType.APPLICATION_JSON)
    List<RegionalDto> getRegionais();
}
```

### `service/<Remote>SyncService.java`

```java
package {{packageRoot}}.service;

import {{packageRoot}}.dto.RegionalDto;
import {{packageRoot}}.entity.Regional;
import {{packageRoot}}.integration.RegionalApiClient;
import {{packageRoot}}.repository.RegionalRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.util.List;

@ApplicationScoped
public class RegionalSyncService {

    private static final Logger LOG = Logger.getLogger(RegionalSyncService.class);

    @Inject @RestClient RegionalApiClient client;
    @Inject RegionalRepository repository;

    /** Idempotent: insert new, update existing by external id. */
    @Transactional
    public SyncResult sync() {
        List<RegionalDto> remote = client.getRegionais();
        int created = 0, updated = 0;

        for (RegionalDto dto : remote) {
            var existing = repository.findByExternalId(dto.id());
            if (existing.isPresent()) {
                existing.get().setName(dto.name());
                existing.get().setCode(dto.code());
                updated++;
            } else {
                repository.persist(new Regional(dto.id(), dto.name(), dto.code()));
                created++;
            }
        }
        LOG.infof("Sync complete: %d created, %d updated", created, updated);
        return new SyncResult(created, updated, remote.size());
    }

    public record SyncResult(int created, int updated, int total) {}
}
```

### `scheduler/<Remote>SyncScheduler.java`

```java
package {{packageRoot}}.scheduler;

import {{packageRoot}}.service.RegionalSyncService;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class RegionalSyncScheduler {

    private static final Logger LOG = Logger.getLogger(RegionalSyncScheduler.class);

    @Inject RegionalSyncService syncService;

    @Scheduled(cron = "{{cron}}")
    void scheduledSync() {
        LOG.info("Starting scheduled regional sync...");
        try {
            syncService.sync();
        } catch (Exception e) {
            LOG.error("Scheduled sync failed", e);
        }
    }
}
```

> **Always swallow the exception inside the scheduled method**. An unhandled throw schedules silently but leaves no actionable signal. Log loudly instead.

### `resource/<Remote>Resource.java` (manual trigger)

```java
package {{packageRoot}}.resource;

import {{packageRoot}}.service.RegionalSyncService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/api/v1/regionals")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Regionals")
public class RegionalResource {

    @Inject RegionalSyncService syncService;

    @POST @Path("/sync") @RolesAllowed("ADMIN")
    public RegionalSyncService.SyncResult sync() {
        return syncService.sync();
    }
}
```

### Test — `test/.../service/<Remote>SyncServiceTest.java`

```java
package {{packageRoot}}.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkiverse.wiremock.devservice.ConnectWireMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@ConnectWireMock
class RegionalSyncServiceTest {

    WireMockServer wiremock;

    @Inject RegionalSyncService service;

    @Test
    void syncCreatesAndUpdates() {
        wiremock.stubFor(get("/v1/regionais").willReturn(okJson("""
            [
              {"id": 1, "name": "South", "code": "S"},
              {"id": 2, "name": "North", "code": "N"}
            ]
        """)));

        var result = service.sync();
        assertEquals(2, result.total());
    }
}
```

## Conventions to enforce

- **REST Client interface lives in `integration/`**, not `service/`. The package separation makes the external boundary obvious.
- **Sync service is `@Transactional` at the method level**. The whole sync is one transaction — partial failures roll back. If the user wants per-record transactions, mention the trade-off (one bad record fails everything) and offer batching.
- **Idempotent by external id**. Always look up by the remote's identifier, not by a local-only sequence. If the remote has no stable id, push back.
- **WireMock in tests**. Never let tests reach the real URL. If `${quarkus.wiremock.devservices.url}` isn't set, the test config wiring is wrong.

## Anti-patterns to refuse

- Polling more than once per minute via `@Scheduled` for an external API without explicit reason — push back and ask the user to consider webhooks or smaller payloads.
- Logging the full DTO list at INFO level — log counts only. PII often hides in upstream payloads.
- Skipping a manual-trigger endpoint — operators need a way to force-sync without waiting for cron.

## Post-generation

- Confirm the cron expression in the user's timezone. Quarkus `@Scheduled` defaults to the JVM's timezone — call this out if the user said "midnight UTC" but the container is set otherwise.
- Tell the user: `./mvnw test -Dtest=<Remote>SyncServiceTest` to verify.
