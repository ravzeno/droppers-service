package ravzeno.droppers_service.domain.trash;

import java.io.IOException;
import java.util.List;
import org.springframework.web.bind.annotation.*;
import ravzeno.droppers_service.domain.trash.dto.TrashEntryResponse;

@RestController
@RequestMapping("/api/v1/trash")
public class TrashController {
    private final TrashService trash;

    public TrashController(TrashService trash) {
        this.trash = trash;
    }

    @GetMapping
    public List<TrashEntryResponse> list() {
        return trash.list().stream()
                .map(
                        e ->
                                new TrashEntryResponse(
                                        e.id(),
                                        e.itemName(),
                                        e.originalPath(),
                                        e.itemType().name(),
                                        e.sizeBytes(),
                                        e.deletedAt()))
                .toList();
    }

    @PostMapping("/{id}/restore")
    public void restore(@PathVariable String id) throws IOException {
        trash.restore(id);
    }

    @DeleteMapping("/{id}")
    public void permanentlyDelete(@PathVariable String id) throws IOException {
        trash.deletePermanently(id);
    }
}
