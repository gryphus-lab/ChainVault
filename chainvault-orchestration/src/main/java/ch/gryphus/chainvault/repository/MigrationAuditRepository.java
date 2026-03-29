/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.repository;

import ch.gryphus.chainvault.model.entity.MigrationAudit;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * The interface Migration audit repository.
 */
@Repository
public interface MigrationAuditRepository extends JpaRepository<MigrationAudit, Long> {

    /**
     * Find by process instance key optional.
     *
     * @param processInstanceKey the process instance key
     * @return the optional
     */
    Optional<MigrationAudit> findByProcessInstanceKey(String processInstanceKey);

    /**
     * Find by document id list.
     *
     * @param documentId the document id
     * @return the list
     */
    List<MigrationAudit> findByDocumentId(String documentId);

    /**
     * Find by status list.
     *
     * @param status the status
     * @return the list
     */
    List<MigrationAudit> findByStatus(MigrationAudit.MigrationStatus status);

    int countAllByStatus(MigrationAudit.MigrationStatus status);

    List<MigrationAudit> getAllByCompletedAtIsNotNull(Limit of);
}
