package com.ravzeno.droppers_service.domains.device.dto.get_all;

import java.time.Instant;
import lombok.Builder;

@Builder
public record GetAllDeviceCriteria(
        String nameLike,
        String clientIdLike,
        Instant pairedAtFrom,
        Instant pairedAtTo,
        Instant lastSeenAtFrom,
        Instant lastSeenAtTo) {
    public static final GetAllDeviceCriteria DEFAULT = builder().build();
}
