package com.ravzeno.droppers_service.domains.pairing;

import com.ravzeno.droppers_service.commons.dto.SuccessResponse;
import com.ravzeno.droppers_service.domains.pairing.dto.PairingClaimResponse;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/back-office/api/v1/pairing/claims")
public class BackOfficePairingController {
    private final PairingService pairing;

    @GetMapping
    public SuccessResponse<List<PairingClaimResponse>> pendingClaims() {
        return SuccessResponse.create(pairing.pendingClaims());
    }

    @PostMapping("/{claimId}/approve")
    public SuccessResponse<PairingClaimResponse> approveClaim(@PathVariable String claimId) {
        return SuccessResponse.create(pairing.approveClaim(claimId));
    }
}
