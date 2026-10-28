package ravzeno.droppers_service.domain.device.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import ravzeno.droppers_service.common.persistence.InstantStringConverter;
import ravzeno.droppers_service.domain.device.model.Device;

@Entity
@Table(name = "devices")
public class DeviceEntity {
    @Id private String id;
    private String name;

    @Column(name = "token_hash")
    private String tokenHash;

    @Convert(converter = InstantStringConverter.class)
    @Column(name = "paired_at")
    private Instant pairedAt;

    @Convert(converter = InstantStringConverter.class)
    @Column(name = "last_seen_at")
    private Instant lastSeenAt;

    protected DeviceEntity() {}

    public DeviceEntity(String id, String name, String tokenHash, Instant pairedAt) {
        this.id = id;
        this.name = name;
        this.tokenHash = tokenHash;
        this.pairedAt = pairedAt;
    }

    public Device toModel() {
        return new Device(id, name, pairedAt, lastSeenAt);
    }
}
