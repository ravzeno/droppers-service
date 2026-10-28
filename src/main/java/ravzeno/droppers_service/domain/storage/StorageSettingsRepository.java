package ravzeno.droppers_service.domain.storage;

import java.nio.file.Path;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ravzeno.droppers_service.domain.storage.persistence.AppSettingEntity;
import ravzeno.droppers_service.domain.storage.persistence.AppSettingJpaRepository;

@Repository
public class StorageSettingsRepository {
    private static final String STORAGE_ROOT_KEY = "storage.root";
    private final AppSettingJpaRepository settings;

    public StorageSettingsRepository(AppSettingJpaRepository settings) {
        this.settings = settings;
    }

    public Optional<Path> findRoot() {
        return settings.findById(STORAGE_ROOT_KEY).map(AppSettingEntity::value).map(Path::of);
    }

    @Transactional
    public void saveRoot(Path root) {
        settings.save(new AppSettingEntity(STORAGE_ROOT_KEY, root.toString()));
    }
}
