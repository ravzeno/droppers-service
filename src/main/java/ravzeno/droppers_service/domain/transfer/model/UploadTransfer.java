package ravzeno.droppers_service.domain.transfer.model;

import java.time.Instant;

public record UploadTransfer(
        String id,
        String destinationPath,
        String fileName,
        Long expectedBytes,
        long transferredBytes,
        TransferStatus status,
        String failureReason,
        Instant createdAt,
        Instant updatedAt) {}
