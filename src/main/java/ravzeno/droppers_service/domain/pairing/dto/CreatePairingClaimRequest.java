package ravzeno.droppers_service.domain.pairing.dto;

import jakarta.validation.constraints.NotBlank;

public record CreatePairingClaimRequest(
        @NotBlank String sessionId,
        @NotBlank String code,
        @NotBlank String deviceId,
        @NotBlank String deviceName) {}
