package ravzeno.droppers_service.domain.transfer.dto;

import java.time.Instant;
import ravzeno.droppers_service.domain.transfer.model.UploadTransfer;

public record UploadTransferResponse(
        String id,
        String destinationPath,
        String fileName,
        Long expectedBytes,
        long transferredBytes,
        String status,
        String failureReason,
        Instant createdAt,
        Instant updatedAt) {
    public static UploadTransferResponse from(UploadTransfer transfer) {
        return new UploadTransferResponse(
                transfer.id(),
                transfer.destinationPath(),
                transfer.fileName(),
                transfer.expectedBytes(),
                transfer.transferredBytes(),
                transfer.status().name(),
                transfer.failureReason(),
                transfer.createdAt(),
                transfer.updatedAt());
    }
}
