package ravzeno.droppers_service.domain.transfer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateUploadRequest(
        String destinationPath, @NotBlank String fileName, @PositiveOrZero Long expectedBytes) {}
