package ravzeno.droppers_service.domain.device.persistence;

import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceSessionJpaRepository extends JpaRepository<DeviceSessionEntity, String> {
    boolean existsByTokenHashAndRevokedAtIsNullAndExpiresAtAfter(
            String tokenHash, Instant expiresAt);

    void deleteByDeviceId(String deviceId);

    boolean existsByDeviceId(String deviceId);
}
