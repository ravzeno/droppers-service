package com.ravzeno.droppers_service.domains.pairing.dto;

import java.time.Instant;

public record PairingResponse(
        String sessionId, String code, String qrPayload, String qrCodeDataUrl, Instant expiresAt) {}
