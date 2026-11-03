package com.ravzeno.droppers_service.domains.pairing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.ravzeno.droppers_service.commons.exception.core.InvalidRequestException;
import com.ravzeno.droppers_service.commons.security.TokenHasher;
import com.ravzeno.droppers_service.domains.device.DeviceService;
import com.ravzeno.droppers_service.domains.pairing.dto.ClaimPairingRequest;
import com.ravzeno.droppers_service.domains.pairing.dto.CompletePairingClaimRequest;
import com.ravzeno.droppers_service.domains.pairing.model.PairingClaim;
import com.ravzeno.droppers_service.domains.pairing.model.enum_type.PairingClaimStatus;
import com.ravzeno.droppers_service.domains.pairing.session.PairingSessionRepository;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class PairingServiceTest {
    private final PairingSessionRepository sessions = mock(PairingSessionRepository.class);
    private final PairingClaimRepository claims = mock(PairingClaimRepository.class);
    private final DeviceService devices = mock(DeviceService.class);
    private final PairingService pairing =
            new PairingService(sessions, claims, devices, new PairingMapper(""));

    @Test
    void claimConsumesThePairingSessionBeforeRegisteringTheDevice() {
        UUID sessionId = UUID.randomUUID();
        when(sessions.consumeActive(eq(sessionId), eq("123456"), any(Instant.class))).thenReturn(1);

        String accessToken =
                pairing.claim(
                                new ClaimPairingRequest(
                                        sessionId.toString(), "123456", "device-1", "My Phone"))
                        .accessToken();

        ArgumentCaptor<String> tokenHash = ArgumentCaptor.forClass(String.class);
        verify(sessions).consumeActive(eq(sessionId), eq("123456"), any(Instant.class));
        verify(devices).register(eq("device-1"), eq("My Phone"), tokenHash.capture());
        assertThat(tokenHash.getValue()).isEqualTo(TokenHasher.sha256(accessToken));
    }

    @Test
    void claimDoesNotRegisterADeviceWhenThePairingSessionCannotBeConsumed() {
        UUID sessionId = UUID.randomUUID();
        when(sessions.consumeActive(eq(sessionId), eq("123456"), any(Instant.class))).thenReturn(0);

        assertThatThrownBy(
                        () ->
                                pairing.claim(
                                        new ClaimPairingRequest(
                                                sessionId.toString(),
                                                "123456",
                                                "device-1",
                                                "My Phone")))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("Pairing code is invalid or expired");

        verifyNoInteractions(devices);
    }

    @Test
    void completingAnApprovedClaimConsumesTheSessionAndRegistersTheDevice() {
        String pairToken = "pair-token";
        UUID claimId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        PairingClaim claim =
                new PairingClaim(
                        claimId,
                        sessionId,
                        "device-1",
                        "My Phone",
                        TokenHasher.sha256(pairToken),
                        PairingClaimStatus.APPROVED,
                        Instant.now(),
                        Instant.now());
        when(claims.findById(claimId)).thenReturn(java.util.Optional.of(claim));
        when(claims.changeStatus(
                        eq(claimId),
                        eq(PairingClaimStatus.APPROVED),
                        eq(PairingClaimStatus.COMPLETED),
                        any(Instant.class)))
                .thenReturn(1);
        when(sessions.consumeActiveById(eq(sessionId), any(Instant.class))).thenReturn(1);

        String accessToken =
                pairing.completeClaim(
                                claimId.toString(), new CompletePairingClaimRequest(pairToken))
                        .accessToken();

        ArgumentCaptor<String> tokenHash = ArgumentCaptor.forClass(String.class);
        verify(claims)
                .changeStatus(
                        eq(claimId),
                        eq(PairingClaimStatus.APPROVED),
                        eq(PairingClaimStatus.COMPLETED),
                        any(Instant.class));
        verify(sessions).consumeActiveById(eq(sessionId), any(Instant.class));
        verify(devices).register(eq("device-1"), eq("My Phone"), tokenHash.capture());
        assertThat(tokenHash.getValue()).isEqualTo(TokenHasher.sha256(accessToken));
    }
}
