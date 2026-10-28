package ravzeno.droppers_service.domain.transfer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import ravzeno.droppers_service.common.exception.core.FileConflictException;
import ravzeno.droppers_service.common.exception.core.InvalidRequestException;
import ravzeno.droppers_service.common.exception.core.ResourceNotFoundException;
import ravzeno.droppers_service.domain.storage.SecurePathResolver;
import ravzeno.droppers_service.domain.transfer.model.TransferStatus;
import ravzeno.droppers_service.domain.transfer.model.UploadTransfer;

@Service
public class UploadTransferService {
    private static final int BUFFER_SIZE = 64 * 1024;
    private static final long PROGRESS_UPDATE_INTERVAL = 1024 * 1024;

    private final SecurePathResolver paths;
    private final UploadStagingStorage staging;
    private final UploadTransferRepository transfers;

    public UploadTransferService(
            SecurePathResolver paths,
            UploadStagingStorage staging,
            UploadTransferRepository transfers) {
        this.paths = paths;
        this.staging = staging;
        this.transfers = transfers;
    }

    public UploadTransfer create(String destinationPath, String fileName, Long expectedBytes) {
        paths.resolveDirectory(destinationPath);
        paths.validateFileName(fileName);
        Instant now = Instant.now();
        UploadTransfer transfer =
                new UploadTransfer(
                        "upload_" + UUID.randomUUID(),
                        normalizeDestination(destinationPath),
                        fileName,
                        expectedBytes,
                        0,
                        TransferStatus.STAGING,
                        null,
                        now,
                        now);
        transfers.save(transfer);
        return transfer;
    }

    public UploadTransfer upload(String id, InputStream input) throws IOException {
        UploadTransfer transfer = requireStaging(id);
        Path stagedFile = staging.fileFor(id);
        long transferredBytes = streamToStaging(transfer, stagedFile, input);
        if (!isStaging(id)) {
            staging.remove(id);
            return require(id);
        }

        if (transfer.expectedBytes() != null && transfer.expectedBytes() != transferredBytes) {
            fail(
                    id,
                    TransferStatus.FAILED,
                    "The uploaded byte count did not match the expected size");
            throw new InvalidRequestException(
                    "The uploaded byte count did not match the expected size");
        }

        Path target = paths.resolveForCreation(transfer.destinationPath(), transfer.fileName());
        if (Files.exists(target)) {
            fail(id, TransferStatus.CONFLICT, "A file with the same name already exists");
            throw new FileConflictException(target.getFileName() + " already exists");
        }

        try {
            Files.move(stagedFile, target);
        } catch (java.nio.file.FileAlreadyExistsException e) {
            fail(id, TransferStatus.CONFLICT, "A file with the same name already exists");
            throw new FileConflictException(target.getFileName() + " already exists");
        } catch (IOException e) {
            fail(id, TransferStatus.FAILED, "Unable to move the staged upload to its destination");
            throw e;
        }

        if (!transfers.complete(id, transferredBytes)) {
            Files.deleteIfExists(target);
        }
        return require(id);
    }

    public UploadTransfer find(String id) {
        return require(id);
    }

    public List<UploadTransfer> recent() {
        return transfers.findRecent();
    }

    public UploadTransfer cancel(String id) throws IOException {
        UploadTransfer transfer = require(id);
        if (transfer.status() == TransferStatus.STAGING) {
            transfers.cancel(id);
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
                if (!isStaging(transfer.id())) {
                    break;
                }
                output.write(buffer, 0, bytesRead);
                transferredBytes += bytesRead;
                if (transferredBytes >= nextProgressUpdate) {
                    transfers.updateProgress(transfer.id(), transferredBytes);
                    nextProgressUpdate = transferredBytes + PROGRESS_UPDATE_INTERVAL;
                }
            }
        }
        transfers.updateProgress(transfer.id(), transferredBytes);
        return transferredBytes;
    }

    private void fail(String id, TransferStatus status, String reason) throws IOException {
        transfers.fail(id, status, reason);
        staging.remove(id);
    }

    private boolean isStaging(String id) {
        return require(id).status() == TransferStatus.STAGING;
    }

    private UploadTransfer requireStaging(String id) {
        UploadTransfer transfer = require(id);
        if (transfer.status() != TransferStatus.STAGING) {
            throw new InvalidRequestException("This upload is no longer accepting data");
        }
        return transfer;
    }

    private UploadTransfer require(String id) {
        return transfers
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Upload transfer not found"));
    }

    private String normalizeDestination(String destinationPath) {
        return destinationPath == null ? "" : destinationPath;
    }
}
