package ravzeno.droppers_service.domains.device.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ravzeno.droppers_service.commons.model.converter.InstantStringConverter;

@Entity
@Table(name = "device_sessions")
@Getter
@Setter
@Builder
@NoArgsConstructor
public class DeviceSession {
    @Id private String id;

    @Column(name = "device_id", nullable = false)
    private String deviceId;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @Convert(converter = InstantStringConverter.class)
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Convert(converter = InstantStringConverter.class)
    @Column(name = "revoked_at")
    private Instant revokedAt;

    public DeviceSession(
            String id, String deviceId, String tokenHash, Instant expiresAt, Instant revokedAt) {
        this.id = id;
        this.deviceId = deviceId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.revokedAt = revokedAt;
    }
}
