package com.ravzeno.droppers_service.domains.file;

import com.ravzeno.droppers_service.domains.file.dto.FileEntryResponse;
import com.ravzeno.droppers_service.domains.file.dto.FileListResponse;
import com.ravzeno.droppers_service.domains.file.model.FileEntry;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class FileMapper {
    public FileEntryResponse toDto(FileEntry entry) {
        return new FileEntryResponse(
                entry.name(),
                entry.path(),
                entry.type().name(),
                entry.size(),
                entry.mimeType(),
                entry.modifiedAt());
    }

    public FileListResponse toListDto(String path, List<FileEntry> entries) {
        String parentPath =
                path.isBlank()
                        ? ""
                        : path.contains("/") ? path.substring(0, path.lastIndexOf('/')) : "";
        return new FileListResponse(path, parentPath, entries.stream().map(this::toDto).toList());
    }
}
