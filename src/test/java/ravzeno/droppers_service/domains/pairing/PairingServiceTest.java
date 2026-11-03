package ravzeno.droppers_service.domains.pairing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ravzeno.droppers_service.commons.exception.core.InvalidRequestException;
import ravzeno.droppers_service.commons.security.TokenHasher;
import ravzeno.droppers_service.domains.device.DeviceService;
import ravzeno.droppers_service.domains.pairing.model.PairingClaim;
import ravzeno.droppers_service.domains.pairing.model.enum_type.PairingClaimStatus;

class PairingServiceTest {
    private final PairingJpaRepository sessions = mock(PairingJpaRepository.class);
    private final PairingClaimJpaRepository claims = mock(PairingClaimJpaRepository.class);
    private final DeviceService devices = mock(DeviceService.class);
    private final PairingService pairing = new PairingService(sessions, claims, devices);

    @Test
    void claimConsumesThePairingSessionBeforeRegisteringTheDevice() {
        when(sessions.consumeActive(eq("session-1"), eq("123456"), any(Instant.class)))
                .thenReturn(1);

        String accessToken = pairing.claim("session-1", "123456", "device-1", "My Phone");

        ArgumentCaptor<String> tokenHash = ArgumentCaptor.forClass(String.class);
        verify(sessions).consumeActive(eq("session-1"), eq("123456"), any(Instant.class));
        verify(devices).register(eq("device-1"), eq("My Phone"), tokenHash.capture());
        assertThat(tokenHash.getValue()).isEqualTo(TokenHasher.sha256(accessToken));
    }

    @Test
    void claimDoesNotRegisterADeviceWhenThePairingSessionCannotBeConsumed() {
        when(sessions.consumeActive(eq("session-1"), eq("123456"), any(Instant.class)))
                .thenReturn(0);

        assertThatThrownBy(() -> pairing.claim("session-1", "123456", "device-1", "My Phone"))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("Pairing code is invalid or expired");

        verifyNoInteractions(devices);
    }

    @Test
    void completingAnApprovedClaimConsumesTheSessionAndRegistersTheDevice() {
        String pairToken = "pair-token";
        PairingClaim claim =
                new PairingClaim(
                        "claim-1",
                        "session-1",
                        "device-1",
                        "My Phone",
                        TokenHasher.sha256(pairToken),
                        PairingClaimStatus.APPROVED,
                        Instant.now(),
                        Instant.now());
        when(claims.findById("claim-1")).thenReturn(java.util.Optional.of(claim));
        when(claims.complete("claim-1")).thenReturn(true);
        when(sessions.consumeActiveById(eq("session-1"), any(Instant.class))).thenReturn(1);

        String accessToken = pairing.completeClaim("claim-1", pairToken);

        ArgumentCaptor<String> tokenHash = ArgumentCaptor.forClass(String.class);
        verify(claims).complete("claim-1");
        verify(sessions).consumeActiveById(eq("session-1"), any(Instant.class));
        verify(devices).register(eq("device-1"), eq("My Phone"), tokenHash.capture());
        assertThat(tokenHash.getValue()).isEqualTo(TokenHasher.sha256(accessToken));
    }
}
