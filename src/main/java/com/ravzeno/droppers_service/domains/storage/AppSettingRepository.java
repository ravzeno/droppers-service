package com.ravzeno.droppers_service.domains.storage;

import com.ravzeno.droppers_service.domains.storage.model.AppSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppSettingRepository extends JpaRepository<AppSetting, String> {}
