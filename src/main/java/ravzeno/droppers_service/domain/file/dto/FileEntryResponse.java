package ravzeno.droppers_service.domain.file.dto;

import java.time.Instant;
import ravzeno.droppers_service.domain.file.model.FileEntry;

public record FileEntryResponse(
        String name, String path, String type, long size, String mimeType, Instant modifiedAt) {
    public static FileEntryResponse from(FileEntry entry) {
        return new FileEntryResponse(
                entry.name(),
                entry.path(),
                entry.type().name(),
                entry.size(),
                entry.mimeType(),
                entry.modifiedAt());
    }
}
