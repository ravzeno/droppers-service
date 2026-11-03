package ravzeno.droppers_service.domains.pairing.dto;

import ravzeno.droppers_service.domains.pairing.model.PairingClaim;

public record PendingPairingClaim(PairingClaim claim, String pairToken) {}
