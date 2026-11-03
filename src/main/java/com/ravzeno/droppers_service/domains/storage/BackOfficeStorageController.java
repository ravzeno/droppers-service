package com.ravzeno.droppers_service.domains.storage;

import com.ravzeno.droppers_service.commons.dto.SuccessResponse;
import com.ravzeno.droppers_service.domains.storage.dto.StorageRootRequest;
import com.ravzeno.droppers_service.domains.storage.dto.StorageRootResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/back-office/api/v1/storage/root")
public class BackOfficeStorageController {
    private final StorageService storage;

    @GetMapping
    public SuccessResponse<StorageRootResponse> root() {
        return SuccessResponse.create(storage.root());
    }

    @PutMapping
    public SuccessResponse<StorageRootResponse> setRoot(
            @Valid @RequestBody StorageRootRequest request) {
        return SuccessResponse.create(storage.setRoot(request.path()));
    }
}
