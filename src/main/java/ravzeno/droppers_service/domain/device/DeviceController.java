package ravzeno.droppers_service.domain.device;

import java.util.List;
import org.springframework.web.bind.annotation.*;
import ravzeno.droppers_service.domain.device.model.Device;

@RestController
@RequestMapping("/api/v1/devices")
public class DeviceController {
    private final DeviceService devices;

    public DeviceController(DeviceService devices) {
        this.devices = devices;
    }

    @GetMapping
    public List<Device> list() {
        return devices.list();
    }

    @DeleteMapping("/{id}")
    public void revoke(@PathVariable String id) {
        devices.revoke(id);
    }
}
