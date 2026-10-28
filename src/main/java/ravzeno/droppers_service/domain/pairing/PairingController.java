package ravzeno.droppers_service.domain.pairing;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;
import org.springframework.web.bind.annotation.*;
import ravzeno.droppers_service.domain.pairing.dto.CompletePairingClaimRequest;
import ravzeno.droppers_service.domain.pairing.dto.CompletePairingClaimResponse;
import ravzeno.droppers_service.domain.pairing.dto.CreatePairingClaimRequest;
import ravzeno.droppers_service.domain.pairing.dto.CreatePairingClaimResponse;
import ravzeno.droppers_service.domain.pairing.dto.PairingClaimResponse;

@RestController
@RequestMapping("/api/v1/pairing")
public class PairingController {
    private final PairingService pairing;

    public PairingController(PairingService pairing) {
        this.pairing = pairing;
    }

    public record PairingResponse(
            String sessionId, String code, String qrPayload, Instant expiresAt) {}

    public record ClaimRequest(
            @NotBlank String sessionId,
            @NotBlank String code,
            @NotBlank String deviceId,
            @NotBlank String deviceName) {}

    public record ClaimResponse(String deviceId, String accessToken) {}

    @PostMapping
    public PairingResponse create() {
        var session = pairing.create();
        return new PairingResponse(
                session.id(),
                session.code(),
                "droppers://pair?sessionId=" + session.id() + "&code=" + session.code(),
                session.expiresAt());
    }

    @PostMapping("/claim")
    public ClaimResponse claim(@Valid @RequestBody ClaimRequest request) {
        return new ClaimResponse(
                request.deviceId(),
                pairing.claim(
                        request.sessionId(),
                        request.code(),
                        request.deviceId(),
                        request.deviceName()));
    }

    @PostMapping("/claims")
    public CreatePairingClaimResponse createClaim(
            @Valid @RequestBody CreatePairingClaimRequest request) {
        var pending =
                pairing.createClaim(
                        request.sessionId(),
                        request.code(),
                        request.deviceId(),
                        request.deviceName());
        return new CreatePairingClaimResponse(
                pending.claim().id(), pending.pairToken(), pending.claim().status().name());
    }

    @GetMapping("/claims/pending")
    public List<PairingClaimResponse> pendingClaims() {
        return pairing.pendingClaims().stream().map(PairingClaimResponse::from).toList();
    }

    @GetMapping("/claims/{claimId}")
    public PairingClaimResponse findClaim(@PathVariable String claimId) {
        return PairingClaimResponse.from(pairing.findClaim(claimId));
    }

    @PostMapping("/claims/{claimId}/approve")
    public PairingClaimResponse approveClaim(@PathVariable String claimId) {
        return PairingClaimResponse.from(pairing.approveClaim(claimId));
    }

    @PostMapping("/claims/{claimId}/complete")
    public CompletePairingClaimResponse completeClaim(
            @PathVariable String claimId, @Valid @RequestBody CompletePairingClaimRequest request) {
        var claim = pairing.findClaim(claimId);
        return new CompletePairingClaimResponse(
                claim.deviceId(), pairing.completeClaim(claimId, request.pairToken()));
    }
}
