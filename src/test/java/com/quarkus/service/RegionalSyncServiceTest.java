package com.quarkus.service;

import com.quarkus.dto.RegionalDto;
import com.quarkus.dto.response.SyncResult;
import com.quarkus.entity.Regional;
import com.quarkus.integration.RegionalApiClient;
import com.quarkus.repository.RegionalRepository;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@QuarkusTest
class RegionalSyncServiceTest {

    @Inject
    RegionalSyncService syncService;

    @InjectMock
    @RestClient
    RegionalApiClient apiClient;

    @InjectMock
    RegionalRepository repository;

    @BeforeEach
    void setUp() {
        Mockito.reset(apiClient, repository);
    }

    @Test
    void testSync_InsertNewRegional() {
        // Given: API externa retorna um novo regional
        RegionalDto newRegional = new RegionalDto(1, "Regional Norte");
        when(apiClient.getRegionais()).thenReturn(List.of(newRegional));
        when(repository.listAll()).thenReturn(Collections.emptyList());

        // When: executar sync
        SyncResult result = syncService.sync();

        // Then: deve inserir 1 regional
        assertEquals(1, result.inserted());
        assertEquals(0, result.updated());
        assertEquals(0, result.deactivated());
        verify(repository, times(1)).persist(any(Regional.class));
    }

    @Test
    void testSync_DeactivateAbsentRegional() {
        // Given: Regional local não está na API externa
        Regional existingRegional = new Regional(1, "Regional Sul", true);
        when(apiClient.getRegionais()).thenReturn(Collections.emptyList());
        when(repository.listAll()).thenReturn(List.of(existingRegional));

        // When: executar sync
        SyncResult result = syncService.sync();

        // Then: deve inativar 1 regional
        assertEquals(0, result.inserted());
        assertEquals(0, result.updated());
        assertEquals(1, result.deactivated());
        assertFalse(existingRegional.getActive());
    }

    @Test
    void testSync_UpdateChangedRegional() {
        // Given: Regional com nome alterado na API externa
        Regional existingRegional = new Regional(1, "Regional Nordeste", true);
        RegionalDto updatedRegional = new RegionalDto(1, "Regional Nordeste Atualizado");

        when(apiClient.getRegionais()).thenReturn(List.of(updatedRegional));
        when(repository.listAll()).thenReturn(List.of(existingRegional));

        // When: executar sync
        SyncResult result = syncService.sync();

        // Then: deve atualizar (inativar antigo e criar novo)
        assertEquals(0, result.inserted());
        assertEquals(1, result.updated());
        assertEquals(0, result.deactivated());
        assertFalse(existingRegional.getActive());
        verify(repository, times(1)).persist(any(Regional.class));
    }

    @Test
    void testSync_NoChanges() {
        // Given: Regional não sofreu alterações
        Regional existingRegional = new Regional(1, "Regional Centro-Oeste", true);
        RegionalDto sameRegional = new RegionalDto(1, "Regional Centro-Oeste");

        when(apiClient.getRegionais()).thenReturn(List.of(sameRegional));
        when(repository.listAll()).thenReturn(List.of(existingRegional));

        // When: executar sync
        SyncResult result = syncService.sync();

        // Then: não deve fazer alterações
        assertEquals(0, result.inserted());
        assertEquals(0, result.updated());
        assertEquals(0, result.deactivated());
        verify(repository, never()).persist(any(Regional.class));
    }

    @Test
    void testSync_MultipleOperations() {
        // Given: cenário complexo com múltiplas operações
        Regional existingRegional1 = new Regional(1, "Regional A", true);
        Regional existingRegional2 = new Regional(2, "Regional B Antigo", true);
        Regional existingRegional3 = new Regional(3, "Regional C", true);

        RegionalDto apiRegional1 = new RegionalDto(1, "Regional A"); // sem mudanças
        RegionalDto apiRegional2 = new RegionalDto(2, "Regional B Novo"); // nome mudou
        RegionalDto apiRegional4 = new RegionalDto(4, "Regional D"); // novo

        when(apiClient.getRegionais()).thenReturn(Arrays.asList(apiRegional1, apiRegional2, apiRegional4));
        when(repository.listAll()).thenReturn(Arrays.asList(existingRegional1, existingRegional2, existingRegional3));

        // When: executar sync
        SyncResult result = syncService.sync();

        // Then: deve inserir 1, atualizar 1 e inativar 1
        assertEquals(1, result.inserted()); // Regional D
        assertEquals(1, result.updated()); // Regional B
        assertEquals(1, result.deactivated()); // Regional C
    }
}
