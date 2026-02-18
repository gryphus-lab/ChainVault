package ch.gryphus.chainvault.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class MigrationOrchestrator {

    private final JobScheduler jobScheduler;
    private final ArchiveMigrationService migrationService;
    private final RestClient sourceRestClient; // configured with base-url + auth

    public void startFullMigration() {
        // Example – real version should paginate
        List<String> docIds = sourceRestClient.get()
                .uri("/documents?status=ARCHIVED&size=10000")
                .retrieve()
                .body(new ParameterizedTypeReference<List<String>>() {
                });

        log.info("Enqueuing {} documents", Objects.requireNonNull(docIds).size());

        docIds.forEach(docId ->
                jobScheduler.enqueue(() -> migrationService.migrateDocument(docId))
        );
    }
}