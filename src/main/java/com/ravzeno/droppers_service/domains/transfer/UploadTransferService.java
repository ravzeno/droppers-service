package com.ravzeno.droppers_service.domains.transfer;

import com.ravzeno.droppers_service.commons.exception.core.FileConflictException;
import com.ravzeno.droppers_service.commons.exception.core.InvalidRequestException;
import com.ravzeno.droppers_service.commons.exception.core.ResourceNotFoundException;
import com.ravzeno.droppers_service.domains.storage.SecurePathResolver;
import com.ravzeno.droppers_service.domains.transfer.dto.UploadTransferResponse;
import com.ravzeno.droppers_service.domains.transfer.model.UploadTransfer;
import com.ravzeno.droppers_service.domains.transfer.model.enum_type.TransferStatus;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class UploadTransferService {
    private static final int BUFFER_SIZE = 64 * 1024;
    private static final long PROGRESS_UPDATE_INTERVAL = 1024 * 1024;

    private final SecurePathResolver paths;
    private final UploadStagingStorage staging;
    private final UploadTransferRepository transfers;
    private final UploadTransferMapper mapper;

    @Transactional
    public void create(String destinationPath, String fileName, Long expectedBytes) {
        paths.resolveDirectory(destinationPath);
        paths.validateFileName(fileName);

        Instant now = Instant.now();

        var transfer =
                UploadTransfer.builder()
                        .destinationPath(normalizeDestination(destinationPath))
                        .fileName(fileName)
                        .expectedBytes(expectedBytes)
                        .transferredBytes(0)
                        .status(TransferStatus.STAGING)
                        .createdAt(now)
                        .updatedAt(now)
                        .build();

        transfers.save(transfer);
    }

    public UploadTransferResponse upload(UUID id, InputStream input) throws IOException {
        var transfer = requireStaging(id);
        Path stagedFile = staging.fileFor(id);
        long transferredBytes = streamToStaging(transfer, stagedFile, input);
        if (!isStaging(id)) {
            staging.remove(id);
            return require(id);
        }

        if (transfer.getExpectedBytes() != null
                && transfer.getExpectedBytes() != transferredBytes) {
            fail(
                    id,
                    TransferStatus.FAILED,
                    "The uploaded byte count did not match the expected size");
            throw new InvalidRequestException(
                    "The uploaded byte count did not match the expected size");
        }

        Path target =
                paths.resolveForCreation(transfer.getDestinationPath(), transfer.getFileName());
        if (Files.exists(target)) {
            fail(id, TransferStatus.CONFLICT, "A file with the same name already exists");
            throw new FileConflictException(target.getFileName() + " already exists");
        }

        try {
            Files.move(stagedFile, target);
        } catch (FileAlreadyExistsException e) {
            fail(id, TransferStatus.CONFLICT, "A file with the same name already exists");
            throw new FileConflictException(target.getFileName() + " already exists");
        } catch (IOException e) {
            fail(id, TransferStatus.FAILED, "Unable to move the staged upload to its destination");
            throw e;
        }

        if (transfers.complete(
                        id,
                        transferredBytes,
                        TransferStatus.COMPLETED,
                        Instant.now(),
                        TransferStatus.STAGING)
                != 1) {
            Files.deleteIfExists(target);
        }
        return require(id);
    }

    @Transactional(readOnly = true)
    public UploadTransferResponse find(UUID id) {
        return require(id);
    }

    @Transactional(readOnly = true)
    public List<UploadTransferResponse> recent() {
        return transfers.findAllByOrderByUpdatedAtDesc(Limit.of(100)).stream()
                .map(mapper::toDto)
                .toList();
    }

    public UploadTransferResponse cancel(UUID id) throws IOException {
        var transfer = requireEntity(id);
        if (transfer.getStatus() == TransferStatus.STAGING) {
            transfers.updateStatus(
                    id, TransferStatus.CANCELLED, null, Instant.now(), TransferStatus.STAGING);
            staging.remove(id);
        }
        return require(id);
    }

    private long streamToStaging(UploadTransfer transfer, Path stagedFile, InputStream input)
            throws IOException {
        long transferredBytes = 0;
        long nextProgressUpdate = PROGRESS_UPDATE_INTERVAL;
        byte[] buffer = new byte[BUFFER_SIZE];
        try (var output =
                Files.newOutputStream(
                        stagedFile,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.WRITE)) {
            for (int bytesRead; (bytesRead = input.read(buffer)) >= 0; ) {
                if (!isStaging(transfer.getId())) {
                    break;
                }
                output.write(buffer, 0, bytesRead);
                transferredBytes += bytesRead;
                if (transferredBytes >= nextProgressUpdate) {
                    transfers.updateProgress(
                            transfer.getId(),
                            transferredBytes,
                            Instant.now(),
                            TransferStatus.STAGING);
                    nextProgressUpdate = transferredBytes + PROGRESS_UPDATE_INTERVAL;
                }
            }
        }
        transfers.updateProgress(
                transfer.getId(), transferredBytes, Instant.now(), TransferStatus.STAGING);
        return transferredBytes;
    }

    private void fail(UUID id, TransferStatus status, String reason) throws IOException {
        transfers.updateStatus(id, status, reason, Instant.now(), TransferStatus.STAGING);
        staging.remove(id);
    }

    private boolean isStaging(UUID id) {
        return requireEntity(id).getStatus() == TransferStatus.STAGING;
    }

    private UploadTransfer requireStaging(UUID id) {
        var transfer = requireEntity(id);
        if (transfer.getStatus() != TransferStatus.STAGING) {
            throw new InvalidRequestException("This upload is no longer accepting data");
        }
        return transfer;
    }

    private UploadTransferResponse require(UUID id) {
        return mapper.toDto(requireEntity(id));
    }

    private UploadTransfer requireEntity(UUID id) {
        return transfers
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Upload transfer not found"));
    }

    private String normalizeDestination(String destinationPath) {
        return destinationPath == null ? "" : destinationPath;
    }
}
