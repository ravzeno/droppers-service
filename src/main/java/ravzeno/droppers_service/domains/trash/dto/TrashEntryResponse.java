package ravzeno.droppers_service.domains.trash.dto;

import java.time.Instant;
import lombok.Builder;

@Builder
public record TrashEntryResponse(
        String id, String name, String originalPath, String type, long size, Instant deletedAt) {}
