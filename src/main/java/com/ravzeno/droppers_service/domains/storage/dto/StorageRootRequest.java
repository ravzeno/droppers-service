package com.ravzeno.droppers_service.domains.storage.dto;

import jakarta.validation.constraints.NotBlank;

public record StorageRootRequest(@NotBlank String path) {}
