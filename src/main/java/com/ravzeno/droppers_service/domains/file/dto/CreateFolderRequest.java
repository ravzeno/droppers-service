package com.ravzeno.droppers_service.domains.file.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateFolderRequest(String parentPath, @NotBlank String name) {}
