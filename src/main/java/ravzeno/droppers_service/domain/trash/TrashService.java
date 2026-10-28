package ravzeno.droppers_service.domain.trash;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import org.springframework.stereotype.Service;
import ravzeno.droppers_service.common.exception.core.InvalidRequestException;
import ravzeno.droppers_service.common.exception.core.ResourceNotFoundException;
import ravzeno.droppers_service.domain.file.FileService;
import ravzeno.droppers_service.domain.file.model.FileType;
import ravzeno.droppers_service.domain.storage.SecurePathResolver;
import ravzeno.droppers_service.domain.trash.model.TrashEntry;

@Service
public class TrashService {
    private final FileService files;
    private final SecurePathResolver paths;
    private final LocalTrashStorage storage;
    private final TrashRepository entries;

    public TrashService(
            FileService files,
            SecurePathResolver paths,
            LocalTrashStorage storage,
            TrashRepository entries) {
        this.files = files;
        this.paths = paths;
        this.storage = storage;
        this.entries = entries;
    }

    public List<TrashEntry> list() {
        return entries.findAllByOrderByDeletedAtDesc();
    }

    public void trash(String rawPath) throws IOException {
        Path source = files.existing(rawPath);
        if (paths.isRoot(source))
            throw new InvalidRequestException("Storage root operation is not allowed");
        String id = "trash_" + UUID.randomUUID();
        Path slot = storage.createSlot(id), target = slot.resolve(source.getFileName());
        try {
            Files.move(source, target);
            long size = size(target);
            entries.save(
                    new TrashEntry(
                            id,
                            paths.relative(source),
                            id + "/" + source.getFileName(),
                            source.getFileName().toString(),
                            Files.isDirectory(target) ? FileType.DIRECTORY : FileType.FILE,
                            size,
                            Instant.now()));
        } catch (IOException | RuntimeException e) {
            storage.remove(slot);
            throw e;
        }
    }

    public void restore(String id) throws IOException {
        TrashEntry entry = require(id);
        Path stored = storage.root().resolve(entry.storedPath()).normalize();
        if (!stored.startsWith(storage.root()) || !Files.exists(stored))
            throw new ResourceNotFoundException("Trashed item is unavailable");
        Path original =
                paths.resolveForCreation(
                        parent(entry.originalPath()), fileName(entry.originalPath()));
        Path target = uniqueRestoredName(original);
        Files.move(stored, target);
        storage.remove(stored.getParent());
        entries.deleteById(id);
    }

    public void deletePermanently(String id) throws IOException {
        TrashEntry entry = require(id);
        storage.remove(storage.root().resolve(entry.storedPath()).getParent());
        entries.deleteById(id);
    }

    private TrashEntry require(String id) {
        return entries.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trash entry not found"));
    }

    private String parent(String p) {
        int index = p.lastIndexOf('/');
        return index < 0 ? "" : p.substring(0, index);
    }

    private String fileName(String p) {
        int index = p.lastIndexOf('/');
        return index < 0 ? p : p.substring(index + 1);
    }

    private Path uniqueRestoredName(Path desired) {
        if (!Files.exists(desired)) return desired;
        String name = desired.getFileName().toString();
        int dot = name.lastIndexOf('.');
        String stem = dot > 0 ? name.substring(0, dot) : name,
                ext = dot > 0 ? name.substring(dot) : "";
        for (int i = 1; ; i++) {
            Path candidate = desired.resolveSibling(stem + " (restored " + i + ")" + ext);
            if (!Files.exists(candidate)) return candidate;
        }
    }

    private long size(Path path) throws IOException {
        if (Files.isRegularFile(path)) return Files.size(path);
        try (var walk = Files.walk(path)) {
            return walk.filter(Files::isRegularFile)
                    .mapToLong(
                            p -> {
                                try {
                                    return Files.size(p);
                                } catch (IOException e) {
                                    return 0;
                                }
                            })
                    .sum();
        }
    }
}
