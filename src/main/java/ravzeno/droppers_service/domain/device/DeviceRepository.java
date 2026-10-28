package ravzeno.droppers_service.domain.device;

import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ravzeno.droppers_service.domain.device.model.Device;
import ravzeno.droppers_service.domain.device.persistence.DeviceEntity;
import ravzeno.droppers_service.domain.device.persistence.DeviceJpaRepository;
import ravzeno.droppers_service.domain.device.persistence.DeviceSessionEntity;
import ravzeno.droppers_service.domain.device.persistence.DeviceSessionJpaRepository;

@Repository
public class DeviceRepository {
    private final DeviceJpaRepository devices;
    private final DeviceSessionJpaRepository sessions;

    public DeviceRepository(DeviceJpaRepository devices, DeviceSessionJpaRepository sessions) {
        this.devices = devices;
        this.sessions = sessions;
    }

    @Transactional
    public void save(String id, String name, String tokenHash, Instant expiresAt) {
        devices.save(new DeviceEntity(id, name, tokenHash, Instant.now()));
        sessions.save(
                new DeviceSessionEntity(
                        "session_" + java.util.UUID.randomUUID(), id, tokenHash, expiresAt));
    }

    public List<Device> list() {
        return devices.findAllByOrderByPairedAtDesc().stream().map(DeviceEntity::toModel).toList();
    }

    @Transactional
    public void delete(String id) {
        sessions.deleteByDeviceId(id);
        devices.deleteById(id);
    }

    public boolean tokenExists(String tokenHash) {
        return sessions.existsByTokenHashAndRevokedAtIsNullAndExpiresAtAfter(
                        tokenHash, Instant.now())
                || devices.existsUnsessionedTokenHash(tokenHash);
    }

    @Transactional
    public void markSeen(String tokenHash) {
        devices.updateLastSeenAt(tokenHash, Instant.now());
    }
}
