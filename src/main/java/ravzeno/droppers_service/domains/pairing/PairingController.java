package ravzeno.droppers_service.domains.pairing;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import ravzeno.droppers_service.domains.pairing.dto.CompletePairingClaimRequest;
import ravzeno.droppers_service.domains.pairing.dto.CompletePairingClaimResponse;
import ravzeno.droppers_service.domains.pairing.dto.CreatePairingClaimRequest;
import ravzeno.droppers_service.domains.pairing.dto.CreatePairingClaimResponse;
import ravzeno.droppers_service.domains.pairing.dto.PairingClaimResponse;

@RestController
@RequestMapping("/api/v1/pairing")
public class PairingController {
    private final PairingService pairing;
    private final String devicePortalUrl;

    public PairingController(
            PairingService pairing,
            @Value("${droppers.device-portal-url:}") String devicePortalUrl) {
        this.pairing = pairing;
        this.devicePortalUrl = devicePortalUrl;
    }

    public record PairingResponse(
            String sessionId,
            String code,
            String qrPayload,
            String qrCodeDataUrl,
            Instant expiresAt) {}

    public record ClaimRequest(
            @NotBlank String sessionId,
            @NotBlank String code,
            @NotBlank String deviceId,
            @NotBlank String deviceName) {}

    public record ClaimResponse(String deviceId, String accessToken) {}

    @PostMapping
    public PairingResponse create() {
        var session = pairing.create();
        String baseUrl = devicePortalUrl == null ? "" : devicePortalUrl.trim();
        String qrPayload =
                baseUrl.isEmpty()
                        ? "droppers://pair?sessionId="
                                + session.getId()
                                + "&code="
                                + session.getCode()
                        : baseUrl
                                + (baseUrl.contains("?") ? "&" : "?")
                                + "sessionId="
                                + session.getId()
                                + "&code="
                                + session.getCode();
        return new PairingResponse(
                session.getId(),
                session.getCode(),
                qrPayload,
                createQrDataUrl(qrPayload),
                session.getExpiresAt());
    }

    private String createQrDataUrl(String payload) {
        try {
            var matrix = new QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, 320, 320);
            var output = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", output);
            return "data:image/png;base64,"
                    + Base64.getEncoder().encodeToString(output.toByteArray());
        } catch (Exception exception) {
            throw new IllegalStateException("Could not generate pairing QR code", exception);
        }
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
                pending.claim().getId(), pending.pairToken(), pending.claim().getStatus().name());
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
                claim.getDeviceId(), pairing.completeClaim(claimId, request.pairToken()));
    }
}
