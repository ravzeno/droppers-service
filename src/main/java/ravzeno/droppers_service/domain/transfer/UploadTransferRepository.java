package ravzeno.droppers_service.domain.transfer;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ravzeno.droppers_service.domain.transfer.model.TransferStatus;
import ravzeno.droppers_service.domain.transfer.model.UploadTransfer;
import ravzeno.droppers_service.domain.transfer.persistence.UploadTransferEntity;
import ravzeno.droppers_service.domain.transfer.persistence.UploadTransferJpaRepository;

@Repository
public class UploadTransferRepository {
    private final UploadTransferJpaRepository transfers;

    public UploadTransferRepository(UploadTransferJpaRepository transfers) {
        this.transfers = transfers;
    }

    @Transactional
    public void save(UploadTransfer transfer) {
        transfers.save(new UploadTransferEntity(transfer));
    }

    public Optional<UploadTransfer> findById(String id) {
        return transfers.findById(id).map(UploadTransferEntity::toModel);
    }

    public List<UploadTransfer> findRecent() {
        return transfers.findAllByOrderByUpdatedAtDesc(Limit.of(100)).stream()
                .map(UploadTransferEntity::toModel)
                .toList();
    }

    @Transactional
    public boolean updateProgress(String id, long transferredBytes) {
        return transfers.updateProgress(id, transferredBytes, Instant.now(), TransferStatus.STAGING)
                == 1;
    }

    @Transactional
    public boolean complete(String id, long transferredBytes) {
        return transfers.complete(
                        id,
                        transferredBytes,
                        TransferStatus.COMPLETED,
                        Instant.now(),
                        TransferStatus.STAGING)
                == 1;
    }

    @Transactional
    public boolean cancel(String id) {
        return transfers.updateStatus(
                        id, TransferStatus.CANCELLED, null, Instant.now(), TransferStatus.STAGING)
                == 1;
    }

    @Transactional
    public void fail(String id, TransferStatus status, String reason) {
        transfers.updateStatus(id, status, reason, Instant.now(), TransferStatus.STAGING);
    }
}
