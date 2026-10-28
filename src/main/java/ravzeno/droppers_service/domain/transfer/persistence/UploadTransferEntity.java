package ravzeno.droppers_service.domain.transfer.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import ravzeno.droppers_service.common.persistence.InstantStringConverter;
import ravzeno.droppers_service.domain.transfer.model.TransferStatus;
import ravzeno.droppers_service.domain.transfer.model.UploadTransfer;

@Entity
@Table(name = "upload_transfers")
public class UploadTransferEntity {
    @Id private String id;

    @Column(name = "destination_path")
    private String destinationPath;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "expected_bytes")
    private Long expectedBytes;

    @Column(name = "transferred_bytes")
    private long transferredBytes;

    @Enumerated(EnumType.STRING)
    private TransferStatus status;

    @Column(name = "failure_reason")
    private String failureReason;

    @Convert(converter = InstantStringConverter.class)
    @Column(name = "created_at")
    private Instant createdAt;

    @Convert(converter = InstantStringConverter.class)
    @Column(name = "updated_at")
    private Instant updatedAt;

    protected UploadTransferEntity() {}

    public UploadTransferEntity(UploadTransfer transfer) {
        id = transfer.id();
        destinationPath = transfer.destinationPath();
        fileName = transfer.fileName();
        expectedBytes = transfer.expectedBytes();
        transferredBytes = transfer.transferredBytes();
        status = transfer.status();
        failureReason = transfer.failureReason();
        createdAt = transfer.createdAt();
        updatedAt = transfer.updatedAt();
    }

    public UploadTransfer toModel() {
        return new UploadTransfer(
                id,
                destinationPath,
                fileName,
                expectedBytes,
                transferredBytes,
                status,
                failureReason,
                createdAt,
                updatedAt);
    }
}
