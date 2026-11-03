package com.ravzeno.droppers_service.domains.pairing.model;

import com.ravzeno.droppers_service.commons.model.converter.InstantStringConverter;
import com.ravzeno.droppers_service.domains.pairing.model.enum_type.PairingClaimStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "pairing_claims")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PairingClaim {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(columnDefinition = "TEXT")
    private UUID id;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(columnDefinition = "TEXT", nullable = false)
    private UUID sessionId;

    @Column(nullable = false)
    private String deviceId;

    @Column(nullable = false)
    private String deviceName;

    @Column(nullable = false)
    private String pairTokenHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PairingClaimStatus status;

    @Convert(converter = InstantStringConverter.class)
    @Column(nullable = false)
    private Instant createdAt;

    @Convert(converter = InstantStringConverter.class)
    @Column(nullable = false)
    private Instant updatedAt;
}
