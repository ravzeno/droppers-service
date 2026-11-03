package com.ravzeno.droppers_service.domains.transfer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class UploadStagingStorage {
    private static final String STAGING_DIRECTORY = "staging";

    public Path fileFor(UUID transferId) throws IOException {
        Path directory =
                Path.of(
                        System.getProperty("user.home"),
                        ".local",
                        "share",
                        "droppers",
                        STAGING_DIRECTORY);
        Files.createDirectories(directory);
        return directory.resolve(transferId + ".upload");
    }

    public void remove(UUID transferId) throws IOException {
        Files.deleteIfExists(fileFor(transferId));
    }
}
