package com.ravzeno.droppers_service.domains.pairing;

import com.ravzeno.droppers_service.commons.dto.SuccessResponse;
import com.ravzeno.droppers_service.domains.pairing.dto.ClaimPairingRequest;
import com.ravzeno.droppers_service.domains.pairing.dto.ClaimPairingResponse;
import com.ravzeno.droppers_service.domains.pairing.dto.CompletePairingClaimRequest;
import com.ravzeno.droppers_service.domains.pairing.dto.CompletePairingClaimResponse;
import com.ravzeno.droppers_service.domains.pairing.dto.CreatePairingClaimRequest;
import com.ravzeno.droppers_service.domains.pairing.dto.CreatePairingClaimResponse;
import com.ravzeno.droppers_service.domains.pairing.dto.PairingClaimResponse;
import com.ravzeno.droppers_service.domains.pairing.dto.PairingResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/public/api/v1/pairing")
public class PublicPairingController {
    private final PairingService pairing;

    @PostMapping
    public SuccessResponse<PairingResponse> create() {
        return SuccessResponse.create(pairing.create());
    }

    @PostMapping("/claim")
    public SuccessResponse<ClaimPairingResponse> claim(
            @Valid @RequestBody ClaimPairingRequest request) {
        return SuccessResponse.create(pairing.claim(request));
    }

    @PostMapping("/claims")
    public SuccessResponse<CreatePairingClaimResponse> createClaim(
            @Valid @RequestBody CreatePairingClaimRequest request) {
        return SuccessResponse.create(pairing.createClaim(request));
    }

    @GetMapping("/claims/{claimId}")
    public SuccessResponse<PairingClaimResponse> findClaim(@PathVariable String claimId) {
        return SuccessResponse.create(pairing.findClaim(claimId));
    }

    @PostMapping("/claims/{claimId}/complete")
    public SuccessResponse<CompletePairingClaimResponse> completeClaim(
            @PathVariable String claimId, @Valid @RequestBody CompletePairingClaimRequest request) {
        return SuccessResponse.create(pairing.completeClaim(claimId, request));
    }
}
