package ravzeno.droppers_service.domain.storage.model;

public record StorageInfo(
        boolean configured, String rootName, long totalBytes, long usableBytes, long usedBytes) {}
