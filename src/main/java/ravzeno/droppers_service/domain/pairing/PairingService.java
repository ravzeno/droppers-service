package ravzeno.droppers_service.domain.pairing;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ravzeno.droppers_service.common.exception.core.InvalidRequestException;
import ravzeno.droppers_service.common.exception.core.ResourceNotFoundException;
import ravzeno.droppers_service.common.security.TokenHasher;
import ravzeno.droppers_service.domain.device.DeviceService;
import ravzeno.droppers_service.domain.pairing.dto.PendingPairingClaim;
import ravzeno.droppers_service.domain.pairing.model.PairingClaim;
import ravzeno.droppers_service.domain.pairing.model.PairingClaimStatus;
import ravzeno.droppers_service.domain.pairing.model.PairingSession;

@Service
public class PairingService {
    private final PairingRepository sessions;
    private final PairingClaimRepository claims;
    private final DeviceService devices;
    private final SecureRandom random = new SecureRandom();

    public PairingService(
            PairingRepository sessions, PairingClaimRepository claims, DeviceService devices) {
        this.sessions = sessions;
        this.claims = claims;
        this.devices = devices;
    }

    public PairingSession create() {
        String code = String.format("%06d", random.nextInt(1_000_000));
        PairingSession session =
                new PairingSession(
                        UUID.randomUUID().toString(),
                        code,
                        Instant.now().plus(Duration.ofMinutes(10)));
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
                new PairingClaim(
                        "claim_" + UUID.randomUUID(),
                        session.id(),
                        deviceId.trim(),
                        deviceName.trim(),
                        TokenHasher.sha256(pairToken),
                        PairingClaimStatus.PENDING,
                        now,
                        now);
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
        if (!sessions.isActive(claim.sessionId(), Instant.now())) {
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
        if (!TokenHasher.sha256(pairToken).equals(claim.pairTokenHash())) {
            throw new InvalidRequestException("Pairing claim token is invalid");
        }
        if (!claims.complete(claimId)) {
            throw new InvalidRequestException("Pairing claim is not approved");
        }
        if (sessions.consumeActiveById(claim.sessionId(), Instant.now()) != 1) {
            throw new InvalidRequestException(
                    "Pairing session is expired or has already been used");
        }

        String accessToken = randomToken();
        devices.register(claim.deviceId(), claim.deviceName(), TokenHasher.sha256(accessToken));
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
