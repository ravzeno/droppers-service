package com.ravzeno.droppers_service.domains.storage;

import com.ravzeno.droppers_service.commons.dto.SuccessResponse;
import com.ravzeno.droppers_service.domains.storage.dto.StorageInfoDto;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/mobile/storage")
public class MobileStorageController {
    private final StorageService storageService;

    @GetMapping
    public SuccessResponse<StorageInfoDto> storage(
            @RequestHeader(value = "X-Droppers-Token", required = false) String token) {
        return SuccessResponse.create(storageService.mobileInfo(token));
    }
}
