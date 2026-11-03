package com.ravzeno.droppers_service.domains.file;

import com.ravzeno.droppers_service.commons.exception.core.FileConflictException;
import com.ravzeno.droppers_service.domains.file.model.FileEntry;
import com.ravzeno.droppers_service.domains.file.model.FileType;
import com.ravzeno.droppers_service.domains.storage.SecurePathResolver;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import org.springframework.stereotype.Component;

@Component
public class LocalFileSystem {
    private final SecurePathResolver paths;

    public LocalFileSystem(SecurePathResolver paths) {
        this.paths = paths;
    }

    public List<FileEntry> list(Path directory) throws IOException {
        Path storageRoot = paths.root();
        List<FileEntry> entries = new ArrayList<>();
        try (DirectoryStream<Path> children = Files.newDirectoryStream(directory)) {
            for (Path child : children) {
                if (!Files.isSymbolicLink(child)) {
                    entries.add(entry(child, storageRoot));
                }
            }
        }
        entries.sort(
                Comparator.comparing((FileEntry entry) -> entry.type() != FileType.DIRECTORY)
                        .thenComparing(FileEntry::name, String.CASE_INSENSITIVE_ORDER));
        return List.copyOf(entries);
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
        return entry(path, paths.root());
    }

    private FileEntry entry(Path path, Path storageRoot) throws IOException {
        boolean directory = Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS);
        return new FileEntry(
                path.getFileName().toString(),
                storageRoot.relativize(path).toString().replace('\\', '/'),
                directory ? FileType.DIRECTORY : FileType.FILE,
                directory ? 0 : Files.size(path),
                directory ? null : Files.probeContentType(path),
                Files.getLastModifiedTime(path).toInstant(),
                path.getFileName().toString().startsWith("."));
    }
}
