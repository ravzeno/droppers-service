package ravzeno.droppers_service.domain.pairing.model;

import jakarta.persistence.*;
import java.time.Instant;
import ravzeno.droppers_service.common.persistence.InstantStringConverter;

@Entity
@Table(name = "pairing_claims")
public class PairingClaim {
    @Id private String id;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "device_name")
    private String deviceName;

    @Column(name = "pair_token_hash")
    private String pairTokenHash;

    @Enumerated(EnumType.STRING)
    private PairingClaimStatus status;

    @Convert(converter = InstantStringConverter.class)
    @Column(name = "created_at")
    private Instant createdAt;

    @Convert(converter = InstantStringConverter.class)
    @Column(name = "updated_at")
    private Instant updatedAt;

    protected PairingClaim() {}

    public PairingClaim(
            String id,
            String sessionId,
            String deviceId,
            String deviceName,
            String pairTokenHash,
            PairingClaimStatus status,
            Instant createdAt,
            Instant updatedAt) {
        this.id = id;
        this.sessionId = sessionId;
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.pairTokenHash = pairTokenHash;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String id() {
        return id;
    }

    public String sessionId() {
        return sessionId;
    }

    public String deviceId() {
        return deviceId;
    }

    public String deviceName() {
        return deviceName;
    }

    public String pairTokenHash() {
        return pairTokenHash;
    }

    public PairingClaimStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
