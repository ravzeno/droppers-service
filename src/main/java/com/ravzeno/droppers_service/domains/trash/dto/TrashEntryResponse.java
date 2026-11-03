package com.ravzeno.droppers_service.domains.trash.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record TrashEntryResponse(
        UUID id, String name, String originalPath, String type, long size, Instant deletedAt) {}
