/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.repository;

import ch.gryphus.chainvault.entity.MigrationEvent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MigrationEventRepository extends JpaRepository<MigrationEvent, Long> {
    List<MigrationEvent> findByMigrationAuditIdOrderByCreatedAtAsc(Long auditId);
}
