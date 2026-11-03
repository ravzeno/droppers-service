package com.ravzeno.droppers_service.domains.storage;

import com.ravzeno.droppers_service.commons.dto.SuccessResponse;
import com.ravzeno.droppers_service.domains.storage.dto.StorageInfoDto;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/public/api/v1")
public class PublicStorageController {
    private final StorageService storage;
    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/health")
    public SuccessResponse<Map<String, String>> health() {
        String databaseStatus;
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            databaseStatus = "UP";
        } catch (DataAccessException exception) {
            databaseStatus = "DOWN";
        }
        String storageStatus = "DOWN".equals(databaseStatus) ? "UNKNOWN" : storage.healthStatus();
        String overallStatus =
                "DOWN".equals(databaseStatus) || "DOWN".equals(storageStatus)
                        ? "DOWN"
                        : "NOT_CONFIGURED".equals(storageStatus) ? "DEGRADED" : "UP";
        return SuccessResponse.create(
                Map.of(
                        "status", overallStatus,
                        "database", databaseStatus,
                        "storage", storageStatus));
    }

    @GetMapping("/storage")
    public SuccessResponse<StorageInfoDto> info() {
        return SuccessResponse.create(storage.info());
    }
}
