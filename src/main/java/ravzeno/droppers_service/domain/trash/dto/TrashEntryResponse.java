package ravzeno.droppers_service.domain.trash.dto;

import java.time.Instant;

public record TrashEntryResponse(
        String id, String name, String originalPath, String type, long size, Instant deletedAt) {}
