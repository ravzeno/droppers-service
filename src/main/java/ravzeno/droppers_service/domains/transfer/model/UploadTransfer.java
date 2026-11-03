package ravzeno.droppers_service.domains.transfer.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ravzeno.droppers_service.commons.model.converter.InstantStringConverter;
import ravzeno.droppers_service.domains.transfer.model.enum_type.TransferStatus;

@Entity
@Table(name = "upload_transfers")
@Getter
@Setter
@Builder
@NoArgsConstructor
public class UploadTransfer {
    @Id private String id;

    @Column(name = "destination_path", nullable = false)
    private String destinationPath;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "expected_bytes")
    private Long expectedBytes;

    @Column(name = "transferred_bytes", nullable = false)
    private long transferredBytes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransferStatus status;

    @Column(name = "failure_reason")
    private String failureReason;

    @Convert(converter = InstantStringConverter.class)
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Convert(converter = InstantStringConverter.class)
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UploadTransfer(
            String id,
            String destinationPath,
            String fileName,
            Long expectedBytes,
            long transferredBytes,
            TransferStatus status,
            String failureReason,
            Instant createdAt,
            Instant updatedAt) {
        this.id = id;
        this.destinationPath = destinationPath;
        this.fileName = fileName;
        this.expectedBytes = expectedBytes;
        this.transferredBytes = transferredBytes;
        this.status = status;
        this.failureReason = failureReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static UploadTransfer fromDto(
            ravzeno.droppers_service.domains.transfer.dto.UploadTransfer dto) {
        return UploadTransfer.builder()
                .id(dto.id())
                .destinationPath(dto.destinationPath())
                .fileName(dto.fileName())
                .expectedBytes(dto.expectedBytes())
                .transferredBytes(dto.transferredBytes())
                .status(dto.status())
                .failureReason(dto.failureReason())
                .createdAt(dto.createdAt())
                .updatedAt(dto.updatedAt())
                .build();
    }

    public ravzeno.droppers_service.domains.transfer.dto.UploadTransfer toDto() {
        return ravzeno.droppers_service.domains.transfer.dto.UploadTransfer.builder()
                .id(id)
                .destinationPath(destinationPath)
                .fileName(fileName)
                .expectedBytes(expectedBytes)
                .transferredBytes(transferredBytes)
                .status(status)
                .failureReason(failureReason)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
