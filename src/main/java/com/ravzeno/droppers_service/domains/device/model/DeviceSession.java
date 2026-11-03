package com.ravzeno.droppers_service.domains.device.model;

import com.ravzeno.droppers_service.commons.model.converter.InstantStringConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
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

@Entity
@Table(name = "device_session")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceSession {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String deviceId;

    @Column(nullable = false, unique = true)
    private String tokenHash;

    @Convert(converter = InstantStringConverter.class)
    @Column(nullable = false)
    private Instant expiresAt;

    @Convert(converter = InstantStringConverter.class)
    @Column
    private Instant revokedAt;
}
