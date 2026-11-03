package ravzeno.droppers_service.domains.pairing.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ravzeno.droppers_service.commons.model.converter.InstantStringConverter;
import ravzeno.droppers_service.domains.pairing.model.enum_type.PairingClaimStatus;

@Entity
@Table(name = "pairing_claims")
@Getter
@Setter
@Builder
@NoArgsConstructor
public class PairingClaim {
    @Id private String id;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Column(name = "device_id", nullable = false)
    private String deviceId;

    @Column(name = "device_name", nullable = false)
    private String deviceName;

    @Column(name = "pair_token_hash", nullable = false)
    private String pairTokenHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PairingClaimStatus status;

    @Convert(converter = InstantStringConverter.class)
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Convert(converter = InstantStringConverter.class)
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

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
}
