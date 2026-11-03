package com.ravzeno.droppers_service.domains.file.model;

import java.time.Instant;

public record FileEntry(
        String name,
        String path,
        FileType type,
        long size,
        String mimeType,
        Instant modifiedAt,
        boolean hidden) {}
