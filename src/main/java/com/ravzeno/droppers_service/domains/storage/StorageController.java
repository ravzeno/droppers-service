package com.ravzeno.droppers_service.domains.storage;

import com.ravzeno.droppers_service.commons.dto.SuccessResponse;
import com.ravzeno.droppers_service.domains.storage.dto.StorageInfoDto;
import com.ravzeno.droppers_service.domains.storage.dto.StorageRootRequest;
import com.ravzeno.droppers_service.domains.storage.dto.StorageRootResponse;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1")
public class StorageController {
    private final StorageService storage;

    @GetMapping("/health")
    public SuccessResponse<Map<String, String>> health() {
        return SuccessResponse.create(Map.of("status", "UP"));
    }

    @GetMapping("/storage")
    public SuccessResponse<StorageInfoDto> info() {
        return SuccessResponse.create(storage.info());
    }

    @GetMapping("/storage/root")
    public SuccessResponse<StorageRootResponse> root() {
        return SuccessResponse.create(storage.root());
    }

    @PutMapping("/storage/root")
    public SuccessResponse<StorageRootResponse> setRoot(
            @Valid @RequestBody StorageRootRequest request) {
        return SuccessResponse.create(storage.setRoot(request.path()));
    }
}
