package ravzeno.droppers_service.domain.pairing.dto;

import jakarta.validation.constraints.NotBlank;

public record CompletePairingClaimRequest(@NotBlank String pairToken) {}
