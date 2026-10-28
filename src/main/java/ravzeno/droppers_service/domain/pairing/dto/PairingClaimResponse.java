package ravzeno.droppers_service.domain.pairing.dto;

import java.time.Instant;
import ravzeno.droppers_service.domain.pairing.model.PairingClaim;

public record PairingClaimResponse(
        String id,
        String sessionId,
        String deviceId,
        String deviceName,
        String status,
        Instant createdAt,
        Instant updatedAt) {
    public static PairingClaimResponse from(PairingClaim claim) {
        return new PairingClaimResponse(
                claim.id(),
                claim.sessionId(),
                claim.deviceId(),
                claim.deviceName(),
                claim.status().name(),
                claim.createdAt(),
                claim.updatedAt());
    }
}
