package ravzeno.droppers_service.domains.storage;

import java.io.IOException;
import java.nio.file.*;
import java.util.Optional;
import org.springframework.stereotype.Service;
import ravzeno.droppers_service.commons.exception.core.InvalidRequestException;
import ravzeno.droppers_service.domains.storage.dto.StorageInfoDto;
import ravzeno.droppers_service.domains.storage.model.AppSetting;

@Service
public class StorageService {
    private static final String STORAGE_ROOT_KEY = "storage.root";
    private final AppSettingJpaRepository settings;

    public StorageService(AppSettingJpaRepository settings) {
        this.settings = settings;
    }

    public Optional<Path> root() {
        return settings.findById(STORAGE_ROOT_KEY).map(AppSetting::getValue).map(Path::of);
    }

    public Path setRoot(String raw) {
        try {
            if (raw == null || raw.isBlank()) throw new InvalidRequestException("Path is required");
            Path root = Path.of(raw).toRealPath();
            if (!Files.isDirectory(root) || !Files.isReadable(root) || !Files.isWritable(root))
                throw new InvalidRequestException("Select a readable and writable directory");
            settings.save(
                    AppSetting.builder().key(STORAGE_ROOT_KEY).value(root.toString()).build());
            return root;
        } catch (IOException e) {
            throw new InvalidRequestException("Select an existing directory");
        }
    }

    public StorageInfoDto info() {
        var root = root();
        if (root.isEmpty()) return new StorageInfoDto(false, "", 0, 0, 0);
        try {
            FileStore fs = Files.getFileStore(root.get());
            long total = fs.getTotalSpace(), usable = fs.getUsableSpace();
            return new StorageInfoDto(
                    true, root.get().getFileName().toString(), total, usable, total - usable);
        } catch (IOException e) {
            throw new InvalidRequestException("Storage root is unavailable");
        }
    }
}
