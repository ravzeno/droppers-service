package com.ravzeno.droppers_service.domains.transfer.dto;

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
        Instant updatedAt) {}
