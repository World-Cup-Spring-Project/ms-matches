package br.com.infnet.msmatches.service;

import br.com.infnet.msmatches.config.CoreDataProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatchSyncScheduler {

    private final CoreDataProperties coreDataProperties;
    private final MatchSyncService matchSyncService;

    @Scheduled(fixedDelayString = "${integrations.core-data.sync-interval-ms:300000}")
    public void syncGames() {
        if (!coreDataProperties.syncEnabled()) {
            return;
        }

        try {
            var result = matchSyncService.syncAll();
            log.info(
                    "Synced games from ms-core-data: created={}, updated={}, total={}",
                    result.created(),
                    result.updated(),
                    result.total()
            );
        } catch (RuntimeException exception) {
            log.warn("Failed to sync games from ms-core-data: {}", exception.getMessage());
        }
    }
}
