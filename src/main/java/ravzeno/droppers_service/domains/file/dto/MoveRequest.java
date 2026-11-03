package ravzeno.droppers_service.domains.file.dto;

import jakarta.validation.constraints.NotBlank;

public record MoveRequest(@NotBlank String source, String destinationDirectory) {}
