package ravzeno.droppers_service.domains.device.dto;

import java.time.Instant;
import lombok.Builder;

/** Safe device representation returned by the API. Authentication data stays in JPA entities. */
@Builder
public record DeviceDto(String id, String name, Instant pairedAt, Instant lastSeenAt) {}
