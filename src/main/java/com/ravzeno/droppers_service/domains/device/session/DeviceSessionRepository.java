package com.ravzeno.droppers_service.domains.device.session;

import com.ravzeno.droppers_service.domains.device.model.DeviceSession;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceSessionRepository extends JpaRepository<DeviceSession, UUID> {
    boolean existsByTokenHashAndRevokedAtIsNullAndExpiresAtAfter(
            String tokenHash, Instant expiresAt);

    void deleteByDeviceId(String deviceId);
}
