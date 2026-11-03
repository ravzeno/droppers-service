package ravzeno.droppers_service.domains.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ravzeno.droppers_service.commons.exception.core.FileConflictException;
import ravzeno.droppers_service.commons.exception.core.InvalidRequestException;
import ravzeno.droppers_service.domains.file.model.FileEntry;
import ravzeno.droppers_service.domains.storage.SecurePathResolver;

@Service
public class FileService {
    private final SecurePathResolver paths;
    private final LocalFileSystem fs;

    public FileService(SecurePathResolver paths, LocalFileSystem fs) {
        this.paths = paths;
        this.fs = fs;
    }

    public List<FileEntry> list(String path) throws IOException {
        return fs.list(paths.resolveDirectory(path));
    }

    public FileEntry createFolder(String parentPath, String name) throws IOException {
        return fs.createDirectory(paths.resolveForCreation(parentPath, name));
    }

    public FileEntry rename(String rawPath, String newName) throws IOException {
        Path source = paths.resolveExisting(rawPath);
        ensureNotRoot(source);
        paths.validateFileName(newName);
        return fs.move(source, source.resolveSibling(newName));
    }

    public FileEntry move(String rawSource, String rawDestination) throws IOException {
        Path source = paths.resolveExisting(rawSource),
                destinationDir = paths.resolveDirectory(rawDestination);
        ensureNotRoot(source);
        if (Files.isDirectory(source) && destinationDir.startsWith(source))
            throw new InvalidRequestException("A folder cannot be moved into itself");
        return fs.move(source, destinationDir.resolve(source.getFileName()));
    }

    public Path download(String path) {
        return paths.resolveFile(path);
    }

    public FileEntry upload(String destinationPath, MultipartFile upload) throws IOException {
        if (upload == null || upload.isEmpty())
            throw new InvalidRequestException("Choose a file to upload");
        String originalName = upload.getOriginalFilename();
        if (originalName == null) throw new InvalidRequestException("Uploaded file has no name");
        String fileName = Path.of(originalName).getFileName().toString();
        Path target = paths.resolveForCreation(destinationPath, fileName);
        if (Files.exists(target)) throw new FileConflictException(fileName + " already exists");
        try (var input = upload.getInputStream()) {
            Files.copy(input, target);
        }
        return fs.entry(target);
    }

    public Path existing(String path) {
        return paths.resolveExisting(path);
    }

    private void ensureNotRoot(Path path) {
        if (paths.isRoot(path))
            throw new InvalidRequestException("Storage root operation is not allowed");
    }
}
