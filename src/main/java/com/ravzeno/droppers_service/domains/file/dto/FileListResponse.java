package com.ravzeno.droppers_service.domains.file.dto;

import java.util.List;

public record FileListResponse(String path, String parentPath, List<FileEntryResponse> items) {}
