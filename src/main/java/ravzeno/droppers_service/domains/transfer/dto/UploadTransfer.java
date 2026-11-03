package ravzeno.droppers_service.domains.transfer.dto;

import java.time.Instant;
import lombok.Builder;
import ravzeno.droppers_service.domains.transfer.model.enum_type.TransferStatus;

@Builder
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
