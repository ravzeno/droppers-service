package ravzeno.droppers_service.domains.file;

import jakarta.validation.Valid;
import java.io.IOException;
import org.springframework.web.bind.annotation.*;
import ravzeno.droppers_service.domains.file.dto.CreateFolderRequest;
import ravzeno.droppers_service.domains.file.dto.FileEntryResponse;

@RestController
@RequestMapping("/api/v1/folders")
public class FolderController {
    private final FileService files;

    public FolderController(FileService files) {
        this.files = files;
    }

    @PostMapping
    public FileEntryResponse create(@Valid @RequestBody CreateFolderRequest request)
            throws IOException {
        return FileEntryResponse.from(files.createFolder(request.parentPath(), request.name()));
    }
}
