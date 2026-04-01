/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.repository;

import ch.gryphus.chainvault.model.entity.MigrationEvent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * The interface Migration event repository.
 */
@Repository
public interface MigrationEventRepository extends JpaRepository<MigrationEvent, Long> {
    /**
     * Find by migration audit id order by created at asc list.
     *
     * @param auditId the audit id
     * @return the list
     */
    List<MigrationEvent> findByMigrationAuditIdOrderByCreatedAtAsc(Long auditId);

    /**
     * Gets all by migration audit id.
     *
     * @param migrationAuditId the migration audit id
     * @return the all by migration audit id
     */
    List<MigrationEvent> getAllByMigrationAuditId(Long migrationAuditId);
}
