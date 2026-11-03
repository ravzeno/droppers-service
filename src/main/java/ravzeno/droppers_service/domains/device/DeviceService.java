package ravzeno.droppers_service.domains.device;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ravzeno.droppers_service.commons.exception.core.SecurityViolationException;
import ravzeno.droppers_service.commons.security.RequestSecurityProperties;
import ravzeno.droppers_service.commons.security.TokenHasher;
import ravzeno.droppers_service.domains.device.dto.DeviceDto;
import ravzeno.droppers_service.domains.device.model.Device;
import ravzeno.droppers_service.domains.device.model.DeviceSession;

@Service
public class DeviceService {
    private final DeviceJpaRepository devices;
    private final DeviceSessionJpaRepository sessions;
    private final RequestSecurityProperties security;

    public DeviceService(
            DeviceJpaRepository devices,
            DeviceSessionJpaRepository sessions,
            RequestSecurityProperties security) {
        this.devices = devices;
        this.sessions = sessions;
        this.security = security;
    }

    public List<DeviceDto> list() {
        return devices.findAllByOrderByPairedAtDesc().stream().map(Device::toDto).toList();
    }

    @Transactional
    public void register(String id, String name, String tokenHash) {
        devices.save(
                Device.builder()
                        .id(id)
                        .name(name)
                        .tokenHash(tokenHash)
                        .pairedAt(Instant.now())
                        .build());
        sessions.save(
                DeviceSession.builder()
                        .id("session_" + java.util.UUID.randomUUID())
                        .deviceId(id)
                        .tokenHash(tokenHash)
                        .expiresAt(
                                Instant.now()
                                        .plus(Duration.ofDays(security.deviceSessionTtlDays())))
                        .build());
    }

    @Transactional
    public void revoke(String id) {
        sessions.deleteByDeviceId(id);
        devices.deleteById(id);
    }

    @Transactional
    public void authenticate(String token) {
        if (token == null || token.isBlank()) {
            throw new SecurityViolationException("Valid device token is required");
        }

        String tokenHash = TokenHasher.sha256(token);
        if (!sessions.existsByTokenHashAndRevokedAtIsNullAndExpiresAtAfter(tokenHash, Instant.now())
                && !devices.existsUnsessionedTokenHash(tokenHash)) {
            throw new SecurityViolationException("Valid device token is required");
        }
        devices.updateLastSeenAt(tokenHash, Instant.now());
    }
}
