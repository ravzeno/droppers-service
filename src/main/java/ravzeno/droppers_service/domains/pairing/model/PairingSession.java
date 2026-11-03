package ravzeno.droppers_service.domains.pairing.model;

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
@Table(name = "pairing_sessions")
@Getter
@Setter
@Builder
@NoArgsConstructor
public class PairingSession {
    @Id private String id;

    @Column(nullable = false)
    private String code;

    @Convert(converter = InstantStringConverter.class)
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Convert(converter = InstantStringConverter.class)
    @Column(name = "consumed_at")
    private Instant consumedAt;

    public PairingSession(String id, String code, Instant expiresAt, Instant consumedAt) {
        this.id = id;
        this.code = code;
        this.expiresAt = expiresAt;
        this.consumedAt = consumedAt;
    }
}
