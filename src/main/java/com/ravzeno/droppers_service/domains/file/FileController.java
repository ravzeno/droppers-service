package com.ravzeno.droppers_service.domains.file;

import com.ravzeno.droppers_service.commons.dto.SuccessResponse;
import com.ravzeno.droppers_service.domains.file.dto.FileEntryResponse;
import com.ravzeno.droppers_service.domains.file.dto.FileListResponse;
import com.ravzeno.droppers_service.domains.file.dto.MoveRequest;
import com.ravzeno.droppers_service.domains.file.dto.RenameRequest;
import com.ravzeno.droppers_service.domains.trash.TrashService;
import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.file.Path;
import lombok.AllArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/files")
@AllArgsConstructor
public class FileController {

    private final FileService fileService;
    private final TrashService trashService;

    @GetMapping
    public SuccessResponse<FileListResponse> list(@RequestParam(defaultValue = "") String path)
            throws IOException {
        return SuccessResponse.create(fileService.list(path));
    }

    @PatchMapping("/rename")
    public SuccessResponse<FileEntryResponse> rename(@Valid @RequestBody RenameRequest request)
            throws IOException {
        return SuccessResponse.create(fileService.rename(request.path(), request.newName()));
    }

    @PostMapping("/move")
    public SuccessResponse<FileEntryResponse> move(@Valid @RequestBody MoveRequest request)
            throws IOException {
        return SuccessResponse.create(
                fileService.move(request.source(), request.destinationDirectory()));
    }

    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SuccessResponse<FileEntryResponse> upload(
            @RequestParam(defaultValue = "") String destinationPath,
            @RequestPart("file") MultipartFile file)
            throws IOException {
        return SuccessResponse.create(fileService.upload(destinationPath, file));
    }

    @DeleteMapping
    public SuccessResponse<Void> trash(@RequestParam String path) throws IOException {
        trashService.trash(path);
        return SuccessResponse.create();
    }

    @GetMapping("/download")
    public ResponseEntity<FileSystemResource> download(@RequestParam String path)
            throws IOException {
        Path file = fileService.download(path);
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
