package ravzeno.droppers_service.domain.transfer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ravzeno.droppers_service.domain.transfer.dto.CreateUploadRequest;
import ravzeno.droppers_service.domain.transfer.dto.UploadTransferResponse;

@RestController
@RequestMapping("/api/v1/transfers/uploads")
public class UploadTransferController {
    private final UploadTransferService uploads;

    public UploadTransferController(UploadTransferService uploads) {
        this.uploads = uploads;
    }

    @PostMapping
    public UploadTransferResponse create(@Valid @RequestBody CreateUploadRequest request) {
        return UploadTransferResponse.from(
                uploads.create(
                        request.destinationPath(), request.fileName(), request.expectedBytes()));
    }

    @PutMapping(path = "/{id}/content", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public UploadTransferResponse upload(@PathVariable String id, HttpServletRequest request)
            throws IOException {
        return UploadTransferResponse.from(uploads.upload(id, request.getInputStream()));
    }

    @GetMapping("/{id}")
    public UploadTransferResponse find(@PathVariable String id) {
        return UploadTransferResponse.from(uploads.find(id));
    }

    @GetMapping
    public List<UploadTransferResponse> recent() {
        return uploads.recent().stream().map(UploadTransferResponse::from).toList();
    }

    @DeleteMapping("/{id}")
    public UploadTransferResponse cancel(@PathVariable String id) throws IOException {
        return UploadTransferResponse.from(uploads.cancel(id));
    }
}
