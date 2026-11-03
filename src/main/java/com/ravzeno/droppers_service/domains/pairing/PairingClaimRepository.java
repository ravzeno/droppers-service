package com.ravzeno.droppers_service.domains.pairing;

import com.ravzeno.droppers_service.domains.pairing.model.PairingClaim;
import com.ravzeno.droppers_service.domains.pairing.model.enum_type.PairingClaimStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PairingClaimRepository extends JpaRepository<PairingClaim, UUID> {
    List<PairingClaim> findByStatusOrderByCreatedAtAsc(PairingClaimStatus status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            "update PairingClaim c set c.status = :next, c.updatedAt = :now where c.id = :id and c.status = :current")
    int changeStatus(
            @Param("id") UUID id,
            @Param("current") PairingClaimStatus current,
            @Param("next") PairingClaimStatus next,
            @Param("now") Instant now);
}
