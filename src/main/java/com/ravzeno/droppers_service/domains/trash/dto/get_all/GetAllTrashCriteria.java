package com.ravzeno.droppers_service.domains.trash.dto.get_all;

import java.time.Instant;
import lombok.Builder;

@Builder
public record GetAllTrashCriteria(
        String nameLike, String originalPathLike, Instant deletedAtFrom, Instant deletedAtTo) {
    public static final GetAllTrashCriteria DEFAULT = builder().build();
}
