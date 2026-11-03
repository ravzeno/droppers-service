package com.ravzeno.droppers_service.domains.storage;

import com.ravzeno.droppers_service.commons.exception.core.InvalidRequestException;
import com.ravzeno.droppers_service.domains.device.DeviceService;
import com.ravzeno.droppers_service.domains.storage.dto.StorageInfoDto;
import com.ravzeno.droppers_service.domains.storage.dto.StorageRootResponse;
import com.ravzeno.droppers_service.domains.storage.model.AppSetting;
import java.io.IOException;
import java.nio.file.*;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class StorageService {
    private static final String STORAGE_ROOT_KEY = "storage.root";
    private final AppSettingRepository settings;
    private final StorageMapper mapper;
    private final DeviceService devices;

    @Transactional(readOnly = true)
    public StorageRootResponse root() {
        return mapper.toDto(configuredRoot());
    }

    @Transactional
    public StorageRootResponse setRoot(String raw) {
        try {
            if (raw == null || raw.isBlank()) {
                throw new InvalidRequestException("Path is required");
            }
            Path root = Path.of(raw).toRealPath();
            if (!Files.isDirectory(root) || !Files.isReadable(root) || !Files.isWritable(root)) {
                throw new InvalidRequestException("Select a readable and writable directory");
            }
            settings.save(
                    AppSetting.builder().key(STORAGE_ROOT_KEY).value(root.toString()).build());
            return mapper.toDto(root);
        } catch (IOException e) {
            throw new InvalidRequestException("Select an existing directory");
        }
    }

    @Transactional(readOnly = true)
    public StorageInfoDto info() {
        Optional<Path> root = configuredRoot();
        if (root.isEmpty()) {
            return mapper.toInfoDto(false, "", 0, 0);
        }
        try {
            FileStore fs = Files.getFileStore(root.get());
            long total = fs.getTotalSpace();
            long usable = fs.getUsableSpace();
            return mapper.toInfoDto(true, root.get().getFileName().toString(), total, usable);
        } catch (IOException e) {
            throw new InvalidRequestException("Storage root is unavailable");
        }
    }

    public StorageInfoDto mobileInfo(String token) {
        devices.authenticate(token);
        return info();
    }

    @Transactional(readOnly = true)
    public String healthStatus() {
        Optional<Path> root = configuredRoot();
        if (root.isEmpty()) {
            return "NOT_CONFIGURED";
        }
        Path path = root.get();
        return Files.isDirectory(path) && Files.isReadable(path) && Files.isWritable(path)
                ? "UP"
                : "DOWN";
    }

    private Optional<Path> configuredRoot() {
        return settings.findById(STORAGE_ROOT_KEY).map(AppSetting::getValue).map(Path::of);
    }
}
