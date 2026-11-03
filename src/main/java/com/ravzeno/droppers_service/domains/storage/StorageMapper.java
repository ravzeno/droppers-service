package com.ravzeno.droppers_service.domains.storage;

import com.ravzeno.droppers_service.domains.storage.dto.StorageInfoDto;
import com.ravzeno.droppers_service.domains.storage.dto.StorageRootResponse;
import java.nio.file.Path;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class StorageMapper {
    public StorageRootResponse toDto(Optional<Path> root) {
        return new StorageRootResponse(root.map(Path::toString).orElse(""));
    }

    public StorageRootResponse toDto(Path root) {
        return new StorageRootResponse(root.toString());
    }

    public StorageInfoDto toInfoDto(
            boolean configured, String rootName, long totalBytes, long usableBytes) {
        return new StorageInfoDto(
                configured, rootName, totalBytes, usableBytes, totalBytes - usableBytes);
    }
}
