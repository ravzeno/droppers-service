package com.ravzeno.droppers_service.domains.pairing.session;

import com.ravzeno.droppers_service.domains.pairing.model.PairingSession;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PairingSessionRepository extends JpaRepository<PairingSession, UUID> {
    @Query(
            "select s from PairingSession s where s.id = :id and s.code = :code and s.consumedAt is null and s.expiresAt > :now")
    Optional<PairingSession> findActive(
            @Param("id") UUID id, @Param("code") String code, @Param("now") Instant now);

    @Query(
            "select count(s) > 0 from PairingSession s where s.id = :id and s.consumedAt is null and s.expiresAt > :now")
    boolean isActive(@Param("id") UUID id, @Param("now") Instant now);

    @Modifying
    @Query(
            """
            update PairingSession s set s.consumedAt = :now
            where s.id = :id and s.code = :code and s.consumedAt is null and s.expiresAt > :now
            """)
    int consumeActive(@Param("id") UUID id, @Param("code") String code, @Param("now") Instant now);

    @Modifying
    @Query(
            """
            update PairingSession s set s.consumedAt = :now
            where s.id = :id and s.consumedAt is null and s.expiresAt > :now
            """)
    int consumeActiveById(@Param("id") UUID id, @Param("now") Instant now);
}
