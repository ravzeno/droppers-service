package com.ravzeno.droppers_service.domains.file;

import com.ravzeno.droppers_service.commons.dto.SuccessResponse;
import com.ravzeno.droppers_service.domains.file.dto.CreateFolderRequest;
import com.ravzeno.droppers_service.domains.file.dto.FileEntryResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/folders")
public class FolderController {
    private final FileService files;

    @PostMapping
    public SuccessResponse<FileEntryResponse> create(
            @Valid @RequestBody CreateFolderRequest request) throws IOException {
        return SuccessResponse.create(files.createFolder(request.parentPath(), request.name()));
    }
}
