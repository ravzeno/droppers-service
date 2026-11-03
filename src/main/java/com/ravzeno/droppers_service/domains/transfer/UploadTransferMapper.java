package com.ravzeno.droppers_service.domains.transfer;

import com.ravzeno.droppers_service.domains.transfer.dto.UploadTransferResponse;
import com.ravzeno.droppers_service.domains.transfer.model.UploadTransfer;
import org.springframework.stereotype.Service;

@Service
public class UploadTransferMapper {
    public UploadTransferResponse toDto(UploadTransfer transfer) {
        return UploadTransferResponse.builder()
                .id(transfer.getId().toString())
                .destinationPath(transfer.getDestinationPath())
                .fileName(transfer.getFileName())
                .expectedBytes(transfer.getExpectedBytes())
                .transferredBytes(transfer.getTransferredBytes())
                .status(transfer.getStatus().name())
                .failureReason(transfer.getFailureReason())
                .createdAt(transfer.getCreatedAt())
                .updatedAt(transfer.getUpdatedAt())
                .build();
    }
}
