/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.repository;

import ch.gryphus.chainvault.model.entity.MigrationAudit;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Pageable;
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

    /**
     * Count all by status int.
     *
     * @param status the status
     * @return the int
     */
    int countAllByStatus(MigrationAudit.MigrationStatus status);

    /**
 * Retrieve MigrationAudit records whose `completedAt` timestamp is not null, constrained by the provided limit.
 *
 * @param of the maximum number of results to return
 * @return a list of MigrationAudit entries with a non-null `completedAt`, limited by `of`
 */
    List<MigrationAudit> getAllByCompletedAtIsNotNull(Limit of);

    /**
 * Retrieve MigrationAudit records with a non-null completedAt using the given pagination.
 *
 * @param pageable controls page size, page number, and sorting for the query
 * @return a list of MigrationAudit entities whose completedAt is not null for the requested page
 */
    List<MigrationAudit> getAllByCompletedAtIsNotNull(Pageable pageable);

    /**
 * Count MigrationAudit records whose `completedAt` timestamp is not null.
 *
 * @return the number of MigrationAudit records with a non-null `completedAt`
 */
    long countByCompletedAtIsNotNull();
}