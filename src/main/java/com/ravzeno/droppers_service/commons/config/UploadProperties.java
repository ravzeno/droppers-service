package com.ravzeno.droppers_service.commons.config;

import com.ravzeno.droppers_service.commons.exception.core.InvalidRequestException;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "droppers.upload")
public record UploadProperties(
        long maxFileSizeBytes, long stagingTtlHours, long cleanupIntervalMillis) {
    private static final String MAX_FILE_SIZE_MESSAGE =
            "The upload exceeds the maximum allowed size";

    public UploadProperties {
        maxFileSizeBytes = maxFileSizeBytes <= 0 ? 1_073_741_824L : maxFileSizeBytes;
        stagingTtlHours = stagingTtlHours <= 0 ? 24 : stagingTtlHours;
        cleanupIntervalMillis = cleanupIntervalMillis <= 0 ? 3_600_000 : cleanupIntervalMillis;
    }

    public void validateFileSize(long fileSizeBytes) {
        if (fileSizeBytes < 0 || fileSizeBytes > maxFileSizeBytes) {
            throw new InvalidRequestException(MAX_FILE_SIZE_MESSAGE);
        }
    }

    public void validateNextChunkSize(long currentSizeBytes, int nextChunkBytes) {
        if (nextChunkBytes < 0 || currentSizeBytes > maxFileSizeBytes - nextChunkBytes) {
            throw new InvalidRequestException(MAX_FILE_SIZE_MESSAGE);
        }
    }
}
