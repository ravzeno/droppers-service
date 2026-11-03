package ravzeno.droppers_service.domains.trash;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import org.springframework.stereotype.Component;

@Component
public class LocalTrashStorage {
    public Path root() throws IOException {
        Path root =
                Path.of(System.getProperty("user.home"), ".local", "share", "droppers", "trash");
        Files.createDirectories(root);
        return root;
    }

    public Path createSlot(String id) throws IOException {
        Path slot = root().resolve(id);
        Files.createDirectory(slot);
        return slot;
    }

    public void remove(Path path) throws IOException {
        if (!Files.exists(path)) return;
        try (var walk = Files.walk(path)) {
            walk.sorted(Comparator.reverseOrder())
                    .forEach(
                            p -> {
                                try {
                                    Files.deleteIfExists(p);
                                } catch (IOException e) {
                                    throw new java.io.UncheckedIOException(e);
                                }
                            });
        }
    }
}
