package com.ravzeno.droppers_service.domains.device;

import com.ravzeno.droppers_service.domains.device.model.Device;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeviceRepository extends JpaRepository<Device, UUID> {
    Optional<Device> findByClientId(String clientId);

    @Query(
            """
            select count(d) > 0 from Device d
            where d.tokenHash = :tokenHash
            and not exists (select 1 from DeviceSession s where s.deviceId = d.clientId)
            """)
    boolean existsUnSessionTokenHash(@Param("tokenHash") String tokenHash);

    @Modifying
    @Query("update Device d set d.lastSeenAt = :seenAt where d.tokenHash = :tokenHash")
    void updateLastSeenAt(@Param("tokenHash") String tokenHash, @Param("seenAt") Instant seenAt);
}
