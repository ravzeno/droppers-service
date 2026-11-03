package com.ravzeno.droppers_service.domains.transfer;

import com.ravzeno.droppers_service.domains.transfer.model.UploadTransfer;
import com.ravzeno.droppers_service.domains.transfer.model.enum_type.TransferStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface UploadTransferRepository extends JpaRepository<UploadTransfer, UUID> {
    List<UploadTransfer> findAllByOrderByUpdatedAtDesc(Limit limit);

    List<UploadTransfer> findAllByStatusAndUpdatedAtBefore(TransferStatus status, Instant cutoff);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(
            """
            update UploadTransfer t set t.transferredBytes = :bytes, t.updatedAt = :updatedAt
            where t.id = :id and t.status = :staging
            """)
    int updateProgress(
            @Param("id") UUID id,
            @Param("bytes") long bytes,
            @Param("updatedAt") Instant updatedAt,
            @Param("staging") TransferStatus staging);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(
            """
            update UploadTransfer t set t.transferredBytes = :bytes, t.status = :completed,
            t.failureReason = null, t.updatedAt = :updatedAt
            where t.id = :id and t.status = :staging
            """)
    int complete(
            @Param("id") UUID id,
            @Param("bytes") long bytes,
            @Param("completed") TransferStatus completed,
            @Param("updatedAt") Instant updatedAt,
            @Param("staging") TransferStatus staging);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(
            """
            update UploadTransfer t set t.status = :status, t.failureReason = :reason,
            t.updatedAt = :updatedAt where t.id = :id and t.status = :staging
            """)
    int updateStatus(
            @Param("id") UUID id,
            @Param("status") TransferStatus status,
            @Param("reason") String reason,
            @Param("updatedAt") Instant updatedAt,
            @Param("staging") TransferStatus staging);
}
