package ravzeno.droppers_service.domain.pairing.model;

import jakarta.persistence.*;
import java.time.Instant;
import ravzeno.droppers_service.common.persistence.InstantStringConverter;

@Entity
@Table(name = "pairing_sessions")
public class PairingSession {
    @Id private String id;
    private String code;

    @Convert(converter = InstantStringConverter.class)
    @Column(name = "expires_at")
    private Instant expiresAt;

    @Convert(converter = InstantStringConverter.class)
    @Column(name = "consumed_at")
    private Instant consumedAt;

    protected PairingSession() {}

    public PairingSession(String id, String code, Instant expiresAt) {
        this.id = id;
        this.code = code;
        this.expiresAt = expiresAt;
    }

    public String id() {
        return id;
    }

    public String code() {
        return code;
    }

    public Instant expiresAt() {
        return expiresAt;
    }
}
