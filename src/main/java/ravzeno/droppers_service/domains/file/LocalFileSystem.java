package ravzeno.droppers_service.domains.file;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.*;
import org.springframework.stereotype.Component;
import ravzeno.droppers_service.commons.exception.core.FileConflictException;
import ravzeno.droppers_service.domains.file.model.FileEntry;
import ravzeno.droppers_service.domains.file.model.FileType;
import ravzeno.droppers_service.domains.storage.SecurePathResolver;

@Component
public class LocalFileSystem {
    private final SecurePathResolver paths;

    public LocalFileSystem(SecurePathResolver paths) {
        this.paths = paths;
    }

    public List<FileEntry> list(Path directory) throws IOException {
        try (var stream = Files.list(directory)) {
            return stream.filter(p -> !Files.isSymbolicLink(p))
                    .map(this::entryUnchecked)
                    .sorted(
                            Comparator.comparing((FileEntry e) -> e.type() != FileType.DIRECTORY)
                                    .thenComparing(FileEntry::name, String.CASE_INSENSITIVE_ORDER))
                    .toList();
        }
    }

    public FileEntry createDirectory(Path path) throws IOException {
        if (Files.exists(path, LinkOption.NOFOLLOW_LINKS))
            throw new FileConflictException(path.getFileName() + " already exists");
        Files.createDirectory(path);
        return entry(path);
    }

    public FileEntry move(Path source, Path destination) throws IOException {
        if (Files.exists(destination, LinkOption.NOFOLLOW_LINKS))
            throw new FileConflictException(destination.getFileName() + " already exists");
        Files.move(source, destination);
        return entry(destination);
    }

    public FileEntry entry(Path path) throws IOException {
        boolean directory = Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS);
        return new FileEntry(
                path.getFileName().toString(),
                paths.relative(path),
                directory ? FileType.DIRECTORY : FileType.FILE,
                directory ? 0 : Files.size(path),
                directory ? null : Files.probeContentType(path),
                Files.getLastModifiedTime(path).toInstant(),
                path.getFileName().toString().startsWith("."));
    }

    private FileEntry entryUnchecked(Path path) {
        try {
            return entry(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
