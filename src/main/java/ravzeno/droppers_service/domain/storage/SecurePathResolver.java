package ravzeno.droppers_service.domain.storage;

import java.io.IOException;
import java.nio.file.*;
import org.springframework.stereotype.Component;
import ravzeno.droppers_service.common.exception.core.InvalidRequestException;
import ravzeno.droppers_service.common.exception.core.ResourceNotFoundException;
import ravzeno.droppers_service.common.exception.core.SecurityViolationException;

@Component
public class SecurePathResolver {
    private final StorageSettingsRepository settings;

    public SecurePathResolver(StorageSettingsRepository settings) {
        this.settings = settings;
    }

    public Path root() {
        try {
            Path root =
                    settings.findRoot()
                            .orElseThrow(
                                    () ->
                                            new InvalidRequestException(
                                                    "Storage root is not configured"));
            if (!Files.isDirectory(root))
                throw new ResourceNotFoundException("Storage root is unavailable");
            return root.toRealPath();
        } catch (IOException e) {
            throw new InvalidRequestException("Storage root is unavailable");
        }
    }

    public Path resolveExisting(String relativePath) {
        Path root = root(), requested = normalize(root, relativePath);
        if (!Files.exists(requested, LinkOption.NOFOLLOW_LINKS))
            throw new ResourceNotFoundException("File not found");
        try {
            Path real = requested.toRealPath();
            ensureInside(root, real);
            return real;
        } catch (IOException e) {
            throw new ResourceNotFoundException("File not found");
        }
    }

    public Path resolveDirectory(String path) {
        Path target = resolveExisting(path);
        if (!Files.isDirectory(target))
            throw new InvalidRequestException("Path is not a directory");
        return target;
    }

    public Path resolveFile(String path) {
        Path target = resolveExisting(path);
        if (!Files.isRegularFile(target)) throw new InvalidRequestException("Path is not a file");
        return target;
    }

    public Path resolveForCreation(String parentPath, String name) {
        validateFileName(name);
        return resolveDirectory(parentPath).resolve(name).normalize();
    }

    public void validateFileName(String name) {
        if (name == null
                || name.isBlank()
                || name.equals(".")
                || name.equals("..")
                || name.indexOf('\0') >= 0
                || name.contains("/")
                || name.contains("\\")) throw new InvalidRequestException("Invalid file name");
    }

    public String relative(Path path) {
        return root().relativize(path).toString().replace('\\', '/');
    }

    public boolean isRoot(Path path) {
        return path.equals(root());
    }

    private Path normalize(Path root, String relative) {
        if (relative == null || relative.isBlank()) return root;
        if (relative.matches("^[a-zA-Z]:.*") || Path.of(relative).isAbsolute())
            throw new SecurityViolationException("Absolute path is not allowed");
        Path target = root.resolve(relative).normalize();
        ensureInside(root, target);
        return target;
    }

    private void ensureInside(Path root, Path target) {
        if (!target.startsWith(root))
            throw new SecurityViolationException("Path escapes storage root");
    }
}
