package com.ravzeno.droppers_service.domains.device.dto.get_all;

import com.ravzeno.droppers_service.commons.dto.enum_type.SortOption;
import lombok.Builder;

@Builder
public record GetAllDeviceSort(SortOption pairedAtSort, SortOption lastSeenAtSort) {
    public static final GetAllDeviceSort DEFAULT = builder().pairedAtSort(SortOption.DESC).build();
}
