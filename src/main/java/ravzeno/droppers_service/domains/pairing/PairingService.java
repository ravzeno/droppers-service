package ravzeno.droppers_service.domains.pairing;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ravzeno.droppers_service.commons.exception.core.InvalidRequestException;
import ravzeno.droppers_service.commons.exception.core.ResourceNotFoundException;
import ravzeno.droppers_service.commons.security.TokenHasher;
import ravzeno.droppers_service.domains.device.DeviceService;
import ravzeno.droppers_service.domains.pairing.dto.PendingPairingClaim;
import ravzeno.droppers_service.domains.pairing.model.PairingClaim;
import ravzeno.droppers_service.domains.pairing.model.PairingSession;
import ravzeno.droppers_service.domains.pairing.model.enum_type.PairingClaimStatus;

@Service
public class PairingService {
    private final PairingJpaRepository sessions;
    private final PairingClaimJpaRepository claims;
    private final DeviceService devices;
    private final SecureRandom random = new SecureRandom();

    public PairingService(
            PairingJpaRepository sessions,
            PairingClaimJpaRepository claims,
            DeviceService devices) {
        this.sessions = sessions;
        this.claims = claims;
        this.devices = devices;
    }

    public PairingSession create() {
        String code = String.format("%06d", random.nextInt(1_000_000));
        PairingSession session =
                PairingSession.builder()
                        .id(UUID.randomUUID().toString())
                        .code(code)
                        .expiresAt(Instant.now().plus(Duration.ofMinutes(10)))
                        .build();
        sessions.save(session);
        return session;
    }

    @Transactional
    public String claim(String sessionId, String code, String deviceId, String deviceName) {
        validateDevice(deviceId, deviceName);
        Instant now = Instant.now();
        if (sessions.consumeActive(sessionId, code, now) != 1) {
            throw new InvalidRequestException("Pairing code is invalid or expired");
        }
        String token = randomToken();
        devices.register(deviceId, deviceName.trim(), TokenHasher.sha256(token));
        return token;
    }

    public PendingPairingClaim createClaim(
            String sessionId, String code, String deviceId, String deviceName) {
        PairingSession session =
                sessions.findActive(sessionId, code, Instant.now())
                        .orElseThrow(
                                () ->
                                        new InvalidRequestException(
                                                "Pairing code is invalid or expired"));
        validateDevice(deviceId, deviceName);

        String pairToken = randomToken();
        Instant now = Instant.now();
        PairingClaim claim =
                PairingClaim.builder()
                        .id("claim_" + UUID.randomUUID())
                        .sessionId(session.getId())
                        .deviceId(deviceId.trim())
                        .deviceName(deviceName.trim())
                        .pairTokenHash(TokenHasher.sha256(pairToken))
                        .status(PairingClaimStatus.PENDING)
                        .createdAt(now)
                        .updatedAt(now)
                        .build();
        claims.save(claim);
        return new PendingPairingClaim(claim, pairToken);
    }

    public PairingClaim findClaim(String claimId) {
        return requireClaim(claimId);
    }

    public List<PairingClaim> pendingClaims() {
        return claims.findPending();
    }

    @Transactional
    public PairingClaim approveClaim(String claimId) {
        PairingClaim claim = requireClaim(claimId);
        if (!sessions.isActive(claim.getSessionId(), Instant.now())) {
            throw new InvalidRequestException(
                    "Pairing session is expired or has already been used");
        }
        if (!claims.approve(claimId)) {
            throw new InvalidRequestException("Pairing claim is no longer pending");
        }
        return requireClaim(claimId);
    }

    @Transactional
    public String completeClaim(String claimId, String pairToken) {
        PairingClaim claim = requireClaim(claimId);
        if (!TokenHasher.sha256(pairToken).equals(claim.getPairTokenHash())) {
            throw new InvalidRequestException("Pairing claim token is invalid");
        }
        if (!claims.complete(claimId)) {
            throw new InvalidRequestException("Pairing claim is not approved");
        }
        if (sessions.consumeActiveById(claim.getSessionId(), Instant.now()) != 1) {
            throw new InvalidRequestException(
                    "Pairing session is expired or has already been used");
        }

        String accessToken = randomToken();
        devices.register(
                claim.getDeviceId(), claim.getDeviceName(), TokenHasher.sha256(accessToken));
        return accessToken;
    }

    private PairingClaim requireClaim(String claimId) {
        return claims.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Pairing claim not found"));
    }

    private void validateDevice(String deviceId, String deviceName) {
        if (deviceId == null || deviceId.isBlank() || deviceName == null || deviceName.isBlank()) {
            throw new InvalidRequestException("Device id and name are required");
        }
    }

    private String randomToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
