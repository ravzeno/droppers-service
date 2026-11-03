package com.ravzeno.droppers_service.domains.transfer;

import com.ravzeno.droppers_service.commons.dto.SuccessResponse;
import com.ravzeno.droppers_service.domains.transfer.dto.CreateUploadRequest;
import com.ravzeno.droppers_service.domains.transfer.dto.UploadTransferResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/transfers/uploads")
public class UploadTransferController {
    private final UploadTransferService uploadTransferService;

    @PostMapping
    public SuccessResponse<Void> create(@Valid @RequestBody CreateUploadRequest request) {
        uploadTransferService.create(
                request.destinationPath(), request.fileName(), request.expectedBytes());

        return SuccessResponse.create();
    }

    @PutMapping(path = "/{id}/content", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public SuccessResponse<UploadTransferResponse> upload(
            @PathVariable UUID id, HttpServletRequest request) throws IOException {
        val result = uploadTransferService.upload(id, request.getInputStream());

        return SuccessResponse.create(result);
    }

    @GetMapping("/{id}")
    public SuccessResponse<UploadTransferResponse> find(@PathVariable UUID id) {
        val result = uploadTransferService.find(id);
        return SuccessResponse.create(result);
    }

    @GetMapping
    public SuccessResponse<List<UploadTransferResponse>> recent() {
        val result = uploadTransferService.recent();
        return SuccessResponse.create(result);
    }

    @DeleteMapping("/{id}")
    public SuccessResponse<Void> cancel(@PathVariable UUID id) throws IOException {
        uploadTransferService.cancel(id);
        return SuccessResponse.create();
    }
}
