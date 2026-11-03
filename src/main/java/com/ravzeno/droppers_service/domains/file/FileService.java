package com.ravzeno.droppers_service.domains.file;

import com.ravzeno.droppers_service.commons.exception.core.FileConflictException;
import com.ravzeno.droppers_service.commons.exception.core.InvalidRequestException;
import com.ravzeno.droppers_service.domains.file.dto.FileEntryResponse;
import com.ravzeno.droppers_service.domains.file.dto.FileListResponse;
import com.ravzeno.droppers_service.domains.storage.SecurePathResolver;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@AllArgsConstructor
public class FileService {
    private final SecurePathResolver paths;
    private final LocalFileSystem fs;
    private final FileMapper mapper;

    public FileListResponse list(String path) throws IOException {
        return mapper.toListDto(path, fs.list(paths.resolveDirectory(path)));
    }

    public FileEntryResponse createFolder(String parentPath, String name) throws IOException {
        return mapper.toDto(fs.createDirectory(paths.resolveForCreation(parentPath, name)));
    }

    public FileEntryResponse rename(String rawPath, String newName) throws IOException {
        Path source = paths.resolveExisting(rawPath);
        ensureNotRoot(source);
        paths.validateFileName(newName);
        return mapper.toDto(fs.move(source, source.resolveSibling(newName)));
    }

    public FileEntryResponse move(String rawSource, String rawDestination) throws IOException {
        Path source = paths.resolveExisting(rawSource);
        Path destinationDir = paths.resolveDirectory(rawDestination);
        ensureNotRoot(source);
        if (Files.isDirectory(source) && destinationDir.startsWith(source)) {
            throw new InvalidRequestException("A folder cannot be moved into itself");
        }
        return mapper.toDto(fs.move(source, destinationDir.resolve(source.getFileName())));
    }

    public Path download(String path) {
        return paths.resolveFile(path);
    }

    public FileEntryResponse upload(String destinationPath, MultipartFile upload)
            throws IOException {
        if (upload == null || upload.isEmpty()) {
            throw new InvalidRequestException("Choose a file to upload");
        }
        String originalName = upload.getOriginalFilename();
        if (originalName == null) {
            throw new InvalidRequestException("Uploaded file has no name");
        }
        String fileName = Path.of(originalName).getFileName().toString();
        Path target = paths.resolveForCreation(destinationPath, fileName);
        if (Files.exists(target)) {
            throw new FileConflictException(fileName + " already exists");
        }
        try (var input = upload.getInputStream()) {
            Files.copy(input, target);
        }
        return mapper.toDto(fs.entry(target));
    }

    public Path existing(String path) {
        return paths.resolveExisting(path);
    }

    private void ensureNotRoot(Path path) {
        if (paths.isRoot(path))
            throw new InvalidRequestException("Storage root operation is not allowed");
    }
}
