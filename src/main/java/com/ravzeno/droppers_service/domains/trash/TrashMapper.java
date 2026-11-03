package com.ravzeno.droppers_service.domains.trash;

import com.ravzeno.droppers_service.domains.trash.dto.TrashEntryResponse;
import com.ravzeno.droppers_service.domains.trash.model.Trash;
import org.springframework.stereotype.Service;

@Service
public class TrashMapper {
    public TrashEntryResponse toDto(Trash entry) {
        return TrashEntryResponse.builder()
                .id(entry.getId())
                .name(entry.getItemName())
                .originalPath(entry.getOriginalPath())
                .type(entry.getItemType().name())
                .size(entry.getSizeBytes() == null ? 0 : entry.getSizeBytes())
                .deletedAt(entry.getDeletedAt())
                .build();
    }
}
