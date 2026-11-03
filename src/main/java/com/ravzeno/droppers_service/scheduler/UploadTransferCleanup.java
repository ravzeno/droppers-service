package com.ravzeno.droppers_service.scheduler;

import com.ravzeno.droppers_service.commons.config.UploadProperties;
import com.ravzeno.droppers_service.domains.transfer.UploadStagingStorage;
import com.ravzeno.droppers_service.domains.transfer.UploadTransferRepository;
import com.ravzeno.droppers_service.domains.transfer.model.enum_type.TransferStatus;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class UploadTransferCleanup {
    private final UploadTransferRepository transfers;
    private final UploadStagingStorage staging;
    private final UploadProperties properties;

    @Scheduled(fixedDelayString = "${droppers.upload.cleanup-interval-millis:3600000}")
    public void expireStaleUploads() {
        Instant cutoff = Instant.now().minus(properties.stagingTtlHours(), ChronoUnit.HOURS);
        transfers
                .findAllByStatusAndUpdatedAtBefore(TransferStatus.STAGING, cutoff)
                .forEach(
                        transfer -> {
                            int expired =
                                    transfers.updateStatus(
                                            transfer.getId(),
                                            TransferStatus.FAILED,
                                            "Upload expired before completion",
                                            Instant.now(),
                                            TransferStatus.STAGING);
                            if (expired == 1) {
                                try {
                                    staging.remove(transfer.getId());
                                } catch (IOException exception) {
                                    log.warn(
                                            "Could not remove staging data for expired upload {}",
                                            transfer.getId(),
                                            exception);
                                }
                            }
                        });
    }
}
