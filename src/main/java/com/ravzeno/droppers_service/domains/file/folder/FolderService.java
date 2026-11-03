package com.ravzeno.droppers_service.domains.file.folder;

import com.ravzeno.droppers_service.domains.file.FileMapper;
import com.ravzeno.droppers_service.domains.file.LocalFileSystem;
import com.ravzeno.droppers_service.domains.file.dto.FileEntryResponse;
import com.ravzeno.droppers_service.domains.storage.SecurePathResolver;
import java.io.IOException;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class FolderService {
    private final SecurePathResolver securePathResolver;
    private final LocalFileSystem localFileSystem;
    private final FileMapper fileMapper;

    public FileEntryResponse createFolder(String parentPath, String name) throws IOException {
        val path = securePathResolver.resolveForCreation(parentPath, name);

        return fileMapper.toDto(localFileSystem.createDirectory(path));
    }
}
