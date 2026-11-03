package ravzeno.droppers_service.domains.file;

import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.file.Path;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ravzeno.droppers_service.domains.file.dto.FileEntryResponse;
import ravzeno.droppers_service.domains.file.dto.FileListResponse;
import ravzeno.droppers_service.domains.file.dto.MoveRequest;
import ravzeno.droppers_service.domains.file.dto.RenameRequest;
import ravzeno.droppers_service.domains.trash.TrashService;

@RestController
@RequestMapping("/api/v1/files")
public class FileController {
    private final FileService files;
    private final TrashService trash;

    public FileController(FileService files, TrashService trash) {
        this.files = files;
        this.trash = trash;
    }

    @GetMapping
    public FileListResponse list(@RequestParam(defaultValue = "") String path) throws IOException {
        var items = files.list(path).stream().map(FileEntryResponse::from).toList();
        String parent =
                path.isBlank()
                        ? ""
                        : path.contains("/") ? path.substring(0, path.lastIndexOf('/')) : "";
        return new FileListResponse(path, parent, items);
    }

    @PatchMapping("/rename")
    public FileEntryResponse rename(@Valid @RequestBody RenameRequest request) throws IOException {
        return FileEntryResponse.from(files.rename(request.path(), request.newName()));
    }

    @PostMapping("/move")
    public FileEntryResponse move(@Valid @RequestBody MoveRequest request) throws IOException {
        return FileEntryResponse.from(files.move(request.source(), request.destinationDirectory()));
    }

    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public FileEntryResponse upload(
            @RequestParam(defaultValue = "") String destinationPath,
            @RequestPart("file") MultipartFile file)
            throws IOException {
        return FileEntryResponse.from(files.upload(destinationPath, file));
    }

    @DeleteMapping
    public void trash(@RequestParam String path) throws IOException {
        trash.trash(path);
    }

    @GetMapping("/download")
    public ResponseEntity<FileSystemResource> download(@RequestParam String path)
            throws IOException {
        Path file = files.download(path);
        var resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .contentType(
                        MediaTypeFactory.getMediaType(resource)
                                .orElse(MediaType.APPLICATION_OCTET_STREAM))
                .contentLength(resource.contentLength())
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(file.getFileName().toString())
                                .build()
                                .toString())
                .body(resource);
    }
}
