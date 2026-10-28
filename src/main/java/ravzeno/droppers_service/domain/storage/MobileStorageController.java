package ravzeno.droppers_service.domain.storage;

import org.springframework.web.bind.annotation.*;
import ravzeno.droppers_service.domain.device.DeviceService;
import ravzeno.droppers_service.domain.storage.model.StorageInfo;

@RestController
@RequestMapping("/api/v1/mobile/storage")
public class MobileStorageController {
    private final DeviceService devices;
    private final StorageService storage;

    public MobileStorageController(DeviceService devices, StorageService storage) {
        this.devices = devices;
        this.storage = storage;
    }

    @GetMapping
    public StorageInfo storage(
            @RequestHeader(value = "X-Droppers-Token", required = false) String token) {
        devices.authenticate(token);
        return storage.info();
    }
}
