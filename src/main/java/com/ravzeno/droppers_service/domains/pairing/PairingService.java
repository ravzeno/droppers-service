package com.ravzeno.droppers_service.domains.pairing;

import com.ravzeno.droppers_service.commons.exception.core.InvalidRequestException;
import com.ravzeno.droppers_service.commons.exception.core.ResourceNotFoundException;
import com.ravzeno.droppers_service.commons.security.TokenHasher;
import com.ravzeno.droppers_service.domains.device.DeviceService;
import com.ravzeno.droppers_service.domains.pairing.dto.ClaimPairingRequest;
import com.ravzeno.droppers_service.domains.pairing.dto.ClaimPairingResponse;
import com.ravzeno.droppers_service.domains.pairing.dto.CompletePairingClaimRequest;
import com.ravzeno.droppers_service.domains.pairing.dto.CompletePairingClaimResponse;
import com.ravzeno.droppers_service.domains.pairing.dto.CreatePairingClaimRequest;
import com.ravzeno.droppers_service.domains.pairing.dto.CreatePairingClaimResponse;
import com.ravzeno.droppers_service.domains.pairing.dto.PairingClaimResponse;
import com.ravzeno.droppers_service.domains.pairing.dto.PairingResponse;
import com.ravzeno.droppers_service.domains.pairing.model.PairingClaim;
import com.ravzeno.droppers_service.domains.pairing.model.PairingSession;
import com.ravzeno.droppers_service.domains.pairing.model.enum_type.PairingClaimStatus;
import com.ravzeno.droppers_service.domains.pairing.session.PairingSessionRepository;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class PairingService {
    private static final SecureRandom RANDOM = new SecureRandom();

    private final PairingSessionRepository sessions;
    private final PairingClaimRepository claims;
    private final DeviceService devices;
    private final PairingMapper mapper;

    @Transactional
    public PairingResponse create() {
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        PairingSession session =
                PairingSession.builder()
                        .code(code)
                        .expiresAt(Instant.now().plus(Duration.ofMinutes(10)))
                        .build();
        return mapper.toDto(sessions.save(session));
    }

    @Transactional
    public ClaimPairingResponse claim(ClaimPairingRequest request) {
        validateDevice(request.deviceId(), request.deviceName());
        Instant now = Instant.now();
        if (sessions.consumeActive(parseSessionId(request.sessionId()), request.code(), now) != 1) {
            throw new InvalidRequestException("Pairing code is invalid or expired");
        }
        String token = randomToken();
        devices.register(
                request.deviceId(), request.deviceName().trim(), TokenHasher.sha256(token));
        return mapper.toDto(request.deviceId(), token);
    }

    @Transactional
    public CreatePairingClaimResponse createClaim(CreatePairingClaimRequest request) {
        PairingSession session =
                sessions.findActive(
                                parseSessionId(request.sessionId()), request.code(), Instant.now())
                        .orElseThrow(
                                () ->
                                        new InvalidRequestException(
                                                "Pairing code is invalid or expired"));
        validateDevice(request.deviceId(), request.deviceName());

        String pairToken = randomToken();
        Instant now = Instant.now();
        PairingClaim claim =
                PairingClaim.builder()
                        .sessionId(session.getId())
                        .deviceId(request.deviceId().trim())
                        .deviceName(request.deviceName().trim())
                        .pairTokenHash(TokenHasher.sha256(pairToken))
                        .status(PairingClaimStatus.PENDING)
                        .createdAt(now)
                        .updatedAt(now)
                        .build();
        return mapper.toDto(claims.save(claim), pairToken);
    }

    @Transactional(readOnly = true)
    public PairingClaimResponse findClaim(String claimId) {
        return mapper.toDto(requireClaim(claimId));
    }

    @Transactional(readOnly = true)
    public List<PairingClaimResponse> pendingClaims() {
        return claims.findByStatusOrderByCreatedAtAsc(PairingClaimStatus.PENDING).stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional
    public PairingClaimResponse approveClaim(String claimId) {
        PairingClaim claim = requireClaim(claimId);
        if (!sessions.isActive(claim.getSessionId(), Instant.now())) {
            throw new InvalidRequestException(
                    "Pairing session is expired or has already been used");
        }
        if (claims.changeStatus(
                        parseClaimId(claimId),
                        PairingClaimStatus.PENDING,
                        PairingClaimStatus.APPROVED,
                        Instant.now())
                != 1) {
            throw new InvalidRequestException("Pairing claim is no longer pending");
        }
        return mapper.toDto(requireClaim(claimId));
    }

    @Transactional
    public CompletePairingClaimResponse completeClaim(
            String claimId, CompletePairingClaimRequest request) {
        PairingClaim claim = requireClaim(claimId);
        if (!TokenHasher.sha256(request.pairToken()).equals(claim.getPairTokenHash())) {
            throw new InvalidRequestException("Pairing claim token is invalid");
        }
        if (claims.changeStatus(
                        parseClaimId(claimId),
                        PairingClaimStatus.APPROVED,
                        PairingClaimStatus.COMPLETED,
                        Instant.now())
                != 1) {
            throw new InvalidRequestException("Pairing claim is not approved");
        }
        if (sessions.consumeActiveById(claim.getSessionId(), Instant.now()) != 1) {
            throw new InvalidRequestException(
                    "Pairing session is expired or has already been used");
        }

        String accessToken = randomToken();
        devices.register(
                claim.getDeviceId(), claim.getDeviceName(), TokenHasher.sha256(accessToken));
        return mapper.toCompleteClaimDto(claim.getDeviceId(), accessToken);
    }

    private PairingClaim requireClaim(String claimId) {
        return claims.findById(parseClaimId(claimId))
                .orElseThrow(() -> new ResourceNotFoundException("Pairing claim not found"));
    }

    private UUID parseSessionId(String sessionId) {
        try {
            return UUID.fromString(sessionId);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new InvalidRequestException("Pairing code is invalid or expired");
        }
    }

    private UUID parseClaimId(String claimId) {
        try {
            return UUID.fromString(claimId);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new ResourceNotFoundException("Pairing claim not found");
        }
    }

    private void validateDevice(String deviceId, String deviceName) {
        if (deviceId == null || deviceId.isBlank() || deviceName == null || deviceName.isBlank()) {
            throw new InvalidRequestException("Device id and name are required");
        }
    }

    private String randomToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
