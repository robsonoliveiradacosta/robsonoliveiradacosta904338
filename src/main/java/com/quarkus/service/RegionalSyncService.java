package com.quarkus.service;

import com.quarkus.dto.RegionalDto;
import com.quarkus.dto.response.SyncResult;
import com.quarkus.entity.Regional;
import com.quarkus.integration.RegionalApiClient;
import com.quarkus.repository.RegionalRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class RegionalSyncService {

    private static final Logger LOG = Logger.getLogger(RegionalSyncService.class);

    @Inject
    @RestClient
    RegionalApiClient apiClient;

    @Inject
    RegionalRepository repository;

    @Transactional
    public SyncResult sync() {
        LOG.info("Starting regional synchronization");

        List<RegionalDto> externos = apiClient.getRegionais();
        LOG.infof("Fetched %d regionals from external API", externos.size());

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
                LOG.debugf("Inserted new regional: id=%d, name=%s", externo.id(), externo.nome());
            } else if (!local.getName().equals(externo.nome())) {
                // Nome alterado: inativar antigo e criar novo
                local.setActive(false);
                Regional novo = new Regional();
                novo.setId(externo.id());
                novo.setName(externo.nome());
                novo.setActive(true);
                repository.persist(novo);
                updated++;
                LOG.debugf("Updated regional: id=%d, old_name=%s, new_name=%s", externo.id(), local.getName(), externo.nome());
            }
        }

        // Inativar ausentes
        for (Regional local : locais) {
            if (local.getActive() && !idsExternos.contains(local.getId())) {
                local.setActive(false);
                deactivated++;
                LOG.debugf("Deactivated regional: id=%d, name=%s", local.getId(), local.getName());
            }
        }

        SyncResult result = new SyncResult(inserted, updated, deactivated);
        LOG.infof("Regional synchronization completed: inserted=%d, updated=%d, deactivated=%d",
                  inserted, updated, deactivated);

        return result;
    }
}
