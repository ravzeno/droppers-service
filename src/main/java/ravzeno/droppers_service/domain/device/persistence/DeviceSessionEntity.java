package ravzeno.droppers_service.domain.device.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import ravzeno.droppers_service.common.persistence.InstantStringConverter;

@Entity
@Table(name = "device_sessions")
public class DeviceSessionEntity {
    @Id private String id;

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "token_hash")
    private String tokenHash;

    @Convert(converter = InstantStringConverter.class)
    @Column(name = "expires_at")
    private Instant expiresAt;

    @Convert(converter = InstantStringConverter.class)
    @Column(name = "revoked_at")
    private Instant revokedAt;

    protected DeviceSessionEntity() {}

    public DeviceSessionEntity(String id, String deviceId, String tokenHash, Instant expiresAt) {
        this.id = id;
        this.deviceId = deviceId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }
}
