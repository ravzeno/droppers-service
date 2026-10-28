package ravzeno.droppers_service.domain.file.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateFolderRequest(String parentPath, @NotBlank String name) {}
