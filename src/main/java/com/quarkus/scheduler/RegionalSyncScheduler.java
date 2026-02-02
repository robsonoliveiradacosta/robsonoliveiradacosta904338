package com.quarkus.scheduler;

import com.quarkus.service.RegionalSyncService;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

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
