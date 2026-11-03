package com.ravzeno.droppers_service.domains.pairing.dto;

import java.time.Instant;
import lombok.Builder;

@Builder
public record PairingClaimResponse(
        String id,
        String sessionId,
        String deviceId,
        String deviceName,
        String status,
        Instant createdAt,
        Instant updatedAt) {}
