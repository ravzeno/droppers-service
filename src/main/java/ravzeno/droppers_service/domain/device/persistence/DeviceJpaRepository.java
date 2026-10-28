package ravzeno.droppers_service.domain.device.persistence;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeviceJpaRepository extends JpaRepository<DeviceEntity, String> {
    List<DeviceEntity> findAllByOrderByPairedAtDesc();

    @Query(
            """
            select count(d) > 0 from DeviceEntity d
            where d.tokenHash = :tokenHash
            and not exists (select 1 from DeviceSessionEntity s where s.deviceId = d.id)
            """)
    boolean existsUnsessionedTokenHash(@Param("tokenHash") String tokenHash);

    @Modifying
    @Query("update DeviceEntity d set d.lastSeenAt = :seenAt where d.tokenHash = :tokenHash")
    void updateLastSeenAt(@Param("tokenHash") String tokenHash, @Param("seenAt") Instant seenAt);
}
