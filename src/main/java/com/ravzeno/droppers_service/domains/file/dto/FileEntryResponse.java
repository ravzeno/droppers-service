package com.ravzeno.droppers_service.domains.file.dto;

import java.time.Instant;

public record FileEntryResponse(
        String name, String path, String type, long size, String mimeType, Instant modifiedAt) {}
