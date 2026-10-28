package ravzeno.droppers_service.domain.file.dto;

import java.util.List;

public record FileListResponse(String path, String parentPath, List<FileEntryResponse> items) {}
