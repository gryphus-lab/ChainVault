/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.repository;

import ch.gryphus.chainvault.entity.MigrationAudit;
import ch.gryphus.chainvault.entity.MigrationStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MigrationAuditRepository extends JpaRepository<MigrationAudit, Long> {

    Optional<MigrationAudit> findByProcessInstanceKey(Long processInstanceKey);

    List<MigrationAudit> findByDocumentId(String documentId);

    List<MigrationAudit> findByStatus(MigrationStatus status);
}
