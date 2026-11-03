package ravzeno.droppers_service.domains.pairing.dto;

import java.time.Instant;
import lombok.Builder;
import ravzeno.droppers_service.domains.pairing.model.PairingClaim;

@Builder
public record PairingClaimResponse(
        String id,
        String sessionId,
        String deviceId,
        String deviceName,
        String status,
        Instant createdAt,
        Instant updatedAt) {
    public static PairingClaimResponse from(PairingClaim claim) {
        return PairingClaimResponse.builder()
                .id(claim.getId())
                .sessionId(claim.getSessionId())
                .deviceId(claim.getDeviceId())
                .deviceName(claim.getDeviceName())
                .status(claim.getStatus().name())
                .createdAt(claim.getCreatedAt())
                .updatedAt(claim.getUpdatedAt())
                .build();
    }
}
