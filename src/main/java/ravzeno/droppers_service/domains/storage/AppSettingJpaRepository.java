package ravzeno.droppers_service.domains.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import ravzeno.droppers_service.domains.storage.model.AppSetting;

public interface AppSettingJpaRepository extends JpaRepository<AppSetting, String> {}
