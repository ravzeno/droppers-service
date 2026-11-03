package com.ravzeno.droppers_service.domains.trash.dto.get_all;

import com.ravzeno.droppers_service.commons.dto.enum_type.SortOption;
import lombok.Builder;

@Builder
public record GetAllTrashSort(SortOption deletedAtSort, SortOption nameSort, SortOption sizeSort) {
    public static final GetAllTrashSort DEFAULT = builder().deletedAtSort(SortOption.DESC).build();
}
