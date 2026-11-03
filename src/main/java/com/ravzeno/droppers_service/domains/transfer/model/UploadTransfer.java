package com.ravzeno.droppers_service.domains.transfer.model;

import com.ravzeno.droppers_service.commons.model.converter.InstantStringConverter;
import com.ravzeno.droppers_service.domains.transfer.model.enum_type.TransferStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadTransfer {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String destinationPath;

    @Column(nullable = false)
    private String fileName;

    private Long expectedBytes;

    @Column(nullable = false)
    private long transferredBytes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransferStatus status;

    private String failureReason;

    @Convert(converter = InstantStringConverter.class)
    @Column(nullable = false)
    private Instant createdAt;

    @Convert(converter = InstantStringConverter.class)
    @Column(nullable = false)
    private Instant updatedAt;
}
