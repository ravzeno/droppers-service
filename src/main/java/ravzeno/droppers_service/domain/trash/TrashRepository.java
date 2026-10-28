package ravzeno.droppers_service.domain.trash;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ravzeno.droppers_service.domain.trash.model.TrashEntry;

public interface TrashRepository extends JpaRepository<TrashEntry, String> {
    List<TrashEntry> findAllByOrderByDeletedAtDesc();
}
