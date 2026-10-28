package ravzeno.droppers_service.domain.file.dto;

import jakarta.validation.constraints.NotBlank;

public record MoveRequest(@NotBlank String source, String destinationDirectory) {}
