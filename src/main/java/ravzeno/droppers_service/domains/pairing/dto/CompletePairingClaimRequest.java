package ravzeno.droppers_service.domains.pairing.dto;

import jakarta.validation.constraints.NotBlank;

public record CompletePairingClaimRequest(@NotBlank String pairToken) {}
