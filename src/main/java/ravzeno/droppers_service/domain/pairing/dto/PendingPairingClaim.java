package ravzeno.droppers_service.domain.pairing.dto;

import ravzeno.droppers_service.domain.pairing.model.PairingClaim;

public record PendingPairingClaim(PairingClaim claim, String pairToken) {}
