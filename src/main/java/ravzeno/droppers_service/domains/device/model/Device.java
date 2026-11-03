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
import ravzeno.droppers_service.domains.device.dto.DeviceDto;

@Entity
@Table(name = "devices")
@Getter
@Setter
@Builder
@NoArgsConstructor
public class Device {
    @Id private String id;

    @Column(nullable = false)
    private String name;

    @Column(name = "token_hash", nullable = false)
    private String tokenHash;

    @Convert(converter = InstantStringConverter.class)
    @Column(name = "paired_at", nullable = false)
    private Instant pairedAt;

    @Convert(converter = InstantStringConverter.class)
    @Column(name = "last_seen_at")
    private Instant lastSeenAt;

    public Device(String id, String name, String tokenHash, Instant pairedAt, Instant lastSeenAt) {
        this.id = id;
        this.name = name;
        this.tokenHash = tokenHash;
        this.pairedAt = pairedAt;
        this.lastSeenAt = lastSeenAt;
    }

    public DeviceDto toDto() {
        return DeviceDto.builder()
                .id(id)
                .name(name)
                .pairedAt(pairedAt)
                .lastSeenAt(lastSeenAt)
                .build();
    }
}
