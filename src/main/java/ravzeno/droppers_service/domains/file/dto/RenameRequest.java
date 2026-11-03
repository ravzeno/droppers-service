package ravzeno.droppers_service.domains.file.dto;

import jakarta.validation.constraints.NotBlank;

public record RenameRequest(@NotBlank String path, @NotBlank String newName) {}
