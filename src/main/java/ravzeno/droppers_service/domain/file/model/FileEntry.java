package ravzeno.droppers_service.domain.file.model;

import java.time.Instant;

public record FileEntry(
        String name,
        String path,
        FileType type,
        long size,
        String mimeType,
        Instant modifiedAt,
        boolean hidden) {}
