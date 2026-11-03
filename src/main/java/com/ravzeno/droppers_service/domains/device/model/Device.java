package com.ravzeno.droppers_service.domains.device.model;

import com.ravzeno.droppers_service.commons.model.converter.InstantStringConverter;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "device")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String clientId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String tokenHash;

    @Convert(converter = InstantStringConverter.class)
    @Column(nullable = false)
    private Instant pairedAt;

    @Convert(converter = InstantStringConverter.class)
    @Column
    private Instant lastSeenAt;
}
