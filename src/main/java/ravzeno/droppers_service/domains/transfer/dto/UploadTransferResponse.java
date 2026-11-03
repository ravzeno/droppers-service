package ravzeno.droppers_service.domains.transfer.dto;

import java.time.Instant;
import lombok.Builder;

@Builder
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
        return UploadTransferResponse.builder()
                .id(transfer.id())
                .destinationPath(transfer.destinationPath())
                .fileName(transfer.fileName())
                .expectedBytes(transfer.expectedBytes())
                .transferredBytes(transfer.transferredBytes())
                .status(transfer.status().name())
                .failureReason(transfer.failureReason())
                .createdAt(transfer.createdAt())
                .updatedAt(transfer.updatedAt())
                .build();
    }
}
