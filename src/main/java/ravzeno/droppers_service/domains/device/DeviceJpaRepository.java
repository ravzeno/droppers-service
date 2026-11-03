package ravzeno.droppers_service.domains.device;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ravzeno.droppers_service.domains.device.model.Device;

public interface DeviceJpaRepository extends JpaRepository<Device, String> {
    List<Device> findAllByOrderByPairedAtDesc();

    @Query(
            """
            select count(d) > 0 from Device d
            where d.tokenHash = :tokenHash
            and not exists (select 1 from DeviceSession s where s.deviceId = d.id)
            """)
    boolean existsUnsessionedTokenHash(@Param("tokenHash") String tokenHash);

    @Modifying
    @Query("update Device d set d.lastSeenAt = :seenAt where d.tokenHash = :tokenHash")
    void updateLastSeenAt(@Param("tokenHash") String tokenHash, @Param("seenAt") Instant seenAt);
}
