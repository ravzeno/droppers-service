package com.ravzeno.droppers_service.domains.pairing;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import com.ravzeno.droppers_service.domains.pairing.dto.ClaimPairingResponse;
import com.ravzeno.droppers_service.domains.pairing.dto.CompletePairingClaimResponse;
import com.ravzeno.droppers_service.domains.pairing.dto.CreatePairingClaimResponse;
import com.ravzeno.droppers_service.domains.pairing.dto.PairingClaimResponse;
import com.ravzeno.droppers_service.domains.pairing.dto.PairingResponse;
import com.ravzeno.droppers_service.domains.pairing.model.PairingClaim;
import com.ravzeno.droppers_service.domains.pairing.model.PairingSession;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PairingMapper {
    private final String devicePortalUrl;

    public PairingMapper(@Value("${droppers.device-portal-url:}") String devicePortalUrl) {
        this.devicePortalUrl = devicePortalUrl;
    }

    public PairingResponse toDto(PairingSession session) {
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
                session.getId().toString(),
                session.getCode(),
                qrPayload,
                createQrDataUrl(qrPayload),
                session.getExpiresAt());
    }

    public CreatePairingClaimResponse toDto(PairingClaim claim, String pairToken) {
        return new CreatePairingClaimResponse(
                claim.getId().toString(), pairToken, claim.getStatus().name());
    }

    public ClaimPairingResponse toDto(String deviceId, String accessToken) {
        return new ClaimPairingResponse(deviceId, accessToken);
    }

    public CompletePairingClaimResponse toCompleteClaimDto(String deviceId, String accessToken) {
        return new CompletePairingClaimResponse(deviceId, accessToken);
    }

    public PairingClaimResponse toDto(PairingClaim claim) {
        return PairingClaimResponse.builder()
                .id(claim.getId().toString())
                .sessionId(claim.getSessionId().toString())
                .deviceId(claim.getDeviceId())
                .deviceName(claim.getDeviceName())
                .status(claim.getStatus().name())
                .createdAt(claim.getCreatedAt())
                .updatedAt(claim.getUpdatedAt())
                .build();
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
}
