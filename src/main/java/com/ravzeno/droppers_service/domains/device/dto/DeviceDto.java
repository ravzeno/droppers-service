package com.ravzeno.droppers_service.domains.device.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

/** Safe device representation returned by the API. Authentication data stays in JPA entities. */
@Builder
public record DeviceDto(UUID id, String name, Instant pairedAt, Instant lastSeenAt) {}
