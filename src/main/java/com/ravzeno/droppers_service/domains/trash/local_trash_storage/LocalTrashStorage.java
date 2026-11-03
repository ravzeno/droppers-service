package com.ravzeno.droppers_service.domains.trash.local_trash_storage;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
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

    public void move(Path source, Path destination) throws IOException {
        FileStore sourceStore = Files.getFileStore(source);
        FileStore destinationStore = Files.getFileStore(destination.getParent());
        if (sourceStore.equals(destinationStore)) {
            Files.move(source, destination);
            return;
        }

        copyAcrossFileStores(source, destination);
        remove(source);
    }

    public void remove(Path path) throws IOException {
        if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
            return;
        }
        Files.walkFileTree(
                path,
                new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attributes)
                            throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path directory, IOException error)
                            throws IOException {
                        if (error != null) {
                            throw error;
                        }
                        Files.delete(directory);
                        return FileVisitResult.CONTINUE;
                    }
                });
    }

    private void copyAcrossFileStores(Path source, Path destination) throws IOException {
        try {
            Files.walkFileTree(
                    source,
                    new SimpleFileVisitor<>() {
                        @Override
                        public FileVisitResult preVisitDirectory(
                                Path directory, BasicFileAttributes attributes) throws IOException {
                            Path target = destination.resolve(source.relativize(directory));
                            Files.createDirectory(target);
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attributes)
                                throws IOException {
                            Path target = destination.resolve(source.relativize(file));
                            if (attributes.isSymbolicLink()) {
                                Files.createSymbolicLink(target, Files.readSymbolicLink(file));
                            } else {
                                Files.copy(file, target, StandardCopyOption.COPY_ATTRIBUTES);
                            }
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult postVisitDirectory(Path directory, IOException error)
                                throws IOException {
                            if (error != null) {
                                throw error;
                            }
                            Path target = destination.resolve(source.relativize(directory));
                            Files.setLastModifiedTime(target, Files.getLastModifiedTime(directory));
                            return FileVisitResult.CONTINUE;
                        }
                    });
        } catch (IOException | RuntimeException exception) {
            try {
                remove(destination);
            } catch (IOException | RuntimeException cleanupException) {
                exception.addSuppressed(cleanupException);
            }
            throw exception;
        }
    }
}
