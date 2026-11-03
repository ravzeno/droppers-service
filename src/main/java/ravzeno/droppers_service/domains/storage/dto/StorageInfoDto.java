package ravzeno.droppers_service.domains.storage.dto;

public record StorageInfoDto(
        boolean configured, String rootName, long totalBytes, long usableBytes, long usedBytes) {}
