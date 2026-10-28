package ravzeno.droppers_service.domain.storage;

import java.io.IOException;
import java.nio.file.*;
import java.util.Optional;
import org.springframework.stereotype.Service;
import ravzeno.droppers_service.common.exception.core.InvalidRequestException;
import ravzeno.droppers_service.domain.storage.model.StorageInfo;

@Service
public class StorageService {
    private final StorageSettingsRepository settings;

    public StorageService(StorageSettingsRepository settings) {
        this.settings = settings;
    }

    public Optional<Path> root() {
        return settings.findRoot();
    }

    public Path setRoot(String raw) {
        try {
            if (raw == null || raw.isBlank()) throw new InvalidRequestException("Path is required");
            Path root = Path.of(raw).toRealPath();
            if (!Files.isDirectory(root) || !Files.isReadable(root) || !Files.isWritable(root))
                throw new InvalidRequestException("Select a readable and writable directory");
            settings.saveRoot(root);
            return root;
        } catch (IOException e) {
            throw new InvalidRequestException("Select an existing directory");
        }
    }

    public StorageInfo info() {
        var root = root();
        if (root.isEmpty()) return new StorageInfo(false, "", 0, 0, 0);
        try {
            FileStore fs = Files.getFileStore(root.get());
            long total = fs.getTotalSpace(), usable = fs.getUsableSpace();
            return new StorageInfo(
                    true, root.get().getFileName().toString(), total, usable, total - usable);
        } catch (IOException e) {
            throw new InvalidRequestException("Storage root is unavailable");
        }
    }
}
