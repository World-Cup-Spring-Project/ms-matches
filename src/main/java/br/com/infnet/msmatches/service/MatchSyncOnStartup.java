package br.com.infnet.msmatches.service;

import br.com.infnet.msmatches.config.CoreDataProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatchSyncOnStartup {

    private final CoreDataProperties coreDataProperties;
    private final MatchSyncService matchSyncService;

    @EventListener(ApplicationReadyEvent.class)
    public void syncGamesOnStartup() {
        if (!coreDataProperties.syncOnStartup()) {
            return;
        }

        try {
            var result = matchSyncService.syncAll();
            log.info(
                    "Startup sync from ms-core-data: created={}, updated={}, total={}",
                    result.created(),
                    result.updated(),
                    result.total()
            );
        } catch (RuntimeException exception) {
            log.warn("Startup sync from ms-core-data failed: {}", exception.getMessage());
        }
    }
}
