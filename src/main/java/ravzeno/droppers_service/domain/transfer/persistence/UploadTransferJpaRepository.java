package ravzeno.droppers_service.domain.transfer.persistence;

import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ravzeno.droppers_service.domain.transfer.model.TransferStatus;

public interface UploadTransferJpaRepository extends JpaRepository<UploadTransferEntity, String> {
    List<UploadTransferEntity> findAllByOrderByUpdatedAtDesc(Limit limit);

    @Modifying
    @Query(
            """
            update UploadTransferEntity t set t.transferredBytes = :bytes, t.updatedAt = :updatedAt
            where t.id = :id and t.status = :staging
            """)
    int updateProgress(
            @Param("id") String id,
            @Param("bytes") long bytes,
            @Param("updatedAt") Instant updatedAt,
            @Param("staging") TransferStatus staging);

    @Modifying
    @Query(
            """
            update UploadTransferEntity t set t.transferredBytes = :bytes, t.status = :completed,
            t.failureReason = null, t.updatedAt = :updatedAt
            where t.id = :id and t.status = :staging
            """)
    int complete(
            @Param("id") String id,
            @Param("bytes") long bytes,
            @Param("completed") TransferStatus completed,
            @Param("updatedAt") Instant updatedAt,
            @Param("staging") TransferStatus staging);

    @Modifying
    @Query(
            """
            update UploadTransferEntity t set t.status = :status, t.failureReason = :reason,
            t.updatedAt = :updatedAt where t.id = :id and t.status = :staging
            """)
    int updateStatus(
            @Param("id") String id,
            @Param("status") TransferStatus status,
            @Param("reason") String reason,
            @Param("updatedAt") Instant updatedAt,
            @Param("staging") TransferStatus staging);
}
