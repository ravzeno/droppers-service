package ravzeno.droppers_service.domains.device;

import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import ravzeno.droppers_service.domains.device.model.DeviceSession;

public interface DeviceSessionJpaRepository extends JpaRepository<DeviceSession, String> {
    boolean existsByTokenHashAndRevokedAtIsNullAndExpiresAtAfter(
            String tokenHash, Instant expiresAt);

    void deleteByDeviceId(String deviceId);

    boolean existsByDeviceId(String deviceId);
}
