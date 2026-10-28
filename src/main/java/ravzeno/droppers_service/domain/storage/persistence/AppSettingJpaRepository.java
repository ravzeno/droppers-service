package ravzeno.droppers_service.domain.storage.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AppSettingJpaRepository extends JpaRepository<AppSettingEntity, String> {}
