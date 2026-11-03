package ravzeno.droppers_service.domains.storage;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
import ravzeno.droppers_service.domains.storage.dto.StorageInfoDto;

@RestController
@RequestMapping("/api/v1")
public class StorageController {
    private final StorageService storage;

    public StorageController(StorageService storage) {
        this.storage = storage;
    }

    public record RootRequest(@NotBlank String path) {}

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }

    @GetMapping("/storage")
    public StorageInfoDto info() {
        return storage.info();
    }

    @GetMapping("/storage/root")
    public Map<String, String> root() {
        return Map.of("root", storage.root().map(Object::toString).orElse(""));
    }

    @PutMapping("/storage/root")
    public Map<String, String> setRoot(@Valid @RequestBody RootRequest request) {
        return Map.of("root", storage.setRoot(request.path()).toString());
    }
}
