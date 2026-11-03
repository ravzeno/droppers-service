package ravzeno.droppers_service.domains.trash;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ravzeno.droppers_service.domains.trash.model.Trash;

public interface TrashJpaRepository extends JpaRepository<Trash, String> {
    List<Trash> findAllByOrderByDeletedAtDesc();
}
