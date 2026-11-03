package ravzeno.droppers_service.domains.device;

import java.util.List;
import org.springframework.web.bind.annotation.*;
import ravzeno.droppers_service.domains.device.dto.DeviceDto;

@RestController
@RequestMapping("/api/v1/devices")
public class DeviceController {
    private final DeviceService devices;

    public DeviceController(DeviceService devices) {
        this.devices = devices;
    }

    @GetMapping
    public List<DeviceDto> list() {
        return devices.list();
    }

    @DeleteMapping("/{id}")
    public void revoke(@PathVariable String id) {
        devices.revoke(id);
    }
}
