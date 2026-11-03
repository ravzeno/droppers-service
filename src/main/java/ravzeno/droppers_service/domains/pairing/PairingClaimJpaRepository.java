package ravzeno.droppers_service.domains.pairing;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ravzeno.droppers_service.domains.pairing.model.PairingClaim;
import ravzeno.droppers_service.domains.pairing.model.enum_type.PairingClaimStatus;

public interface PairingClaimJpaRepository extends JpaRepository<PairingClaim, String> {
    List<PairingClaim> findByStatusOrderByCreatedAtAsc(PairingClaimStatus status);

    default List<PairingClaim> findPending() {
        return findByStatusOrderByCreatedAtAsc(PairingClaimStatus.PENDING);
    }

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            "update PairingClaim c set c.status = :next, c.updatedAt = :now where c.id = :id and c.status = :current")
    int changeStatus(
            @Param("id") String id,
            @Param("current") PairingClaimStatus current,
            @Param("next") PairingClaimStatus next,
            @Param("now") Instant now);

    default boolean approve(String id) {
        return changeStatus(
                        id, PairingClaimStatus.PENDING, PairingClaimStatus.APPROVED, Instant.now())
                == 1;
    }

    default boolean complete(String id) {
        return changeStatus(
                        id,
                        PairingClaimStatus.APPROVED,
                        PairingClaimStatus.COMPLETED,
                        Instant.now())
                == 1;
    }
}
