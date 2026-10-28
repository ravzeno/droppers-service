package ravzeno.droppers_service.domain.file.dto;

import jakarta.validation.constraints.NotBlank;

public record RenameRequest(@NotBlank String path, @NotBlank String newName) {}
