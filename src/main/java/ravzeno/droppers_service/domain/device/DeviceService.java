package ravzeno.droppers_service.domain.device;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import ravzeno.droppers_service.common.exception.core.SecurityViolationException;
import ravzeno.droppers_service.common.security.RequestSecurityProperties;
import ravzeno.droppers_service.common.security.TokenHasher;
import ravzeno.droppers_service.domain.device.model.Device;

@Service
public class DeviceService {
    private final DeviceRepository devices;
    private final RequestSecurityProperties security;

    public DeviceService(DeviceRepository devices, RequestSecurityProperties security) {
        this.devices = devices;
        this.security = security;
    }

    public List<Device> list() {
        return devices.list();
    }

    public void register(String id, String name, String tokenHash) {
        devices.save(
                id,
                name,
                tokenHash,
                Instant.now().plus(Duration.ofDays(security.deviceSessionTtlDays())));
    }

    public void revoke(String id) {
        devices.delete(id);
    }

    public void authenticate(String token) {
        if (token == null || token.isBlank()) {
            throw new SecurityViolationException("Valid device token is required");
        }

        String tokenHash = TokenHasher.sha256(token);
        if (!devices.tokenExists(tokenHash)) {
            throw new SecurityViolationException("Valid device token is required");
        }
        devices.markSeen(tokenHash);
    }
}
