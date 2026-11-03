package ravzeno.droppers_service.domains.trash.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ravzeno.droppers_service.commons.model.converter.InstantStringConverter;
import ravzeno.droppers_service.domains.file.model.FileType;
import ravzeno.droppers_service.domains.trash.dto.TrashEntryResponse;

@Entity
@Table(name = "trash_entries")
@Getter
@Setter
@Builder
@NoArgsConstructor
public class Trash {
    @Id private String id;

    @Column(name = "original_path", nullable = false)
    private String originalPath;

    @Column(name = "stored_path", nullable = false)
    private String storedPath;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false)
    private FileType itemType;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Convert(converter = InstantStringConverter.class)
    @Column(name = "deleted_at", nullable = false)
    private Instant deletedAt;

    public TrashEntryResponse toDto() {
        return TrashEntryResponse.builder()
                .id(id)
                .name(itemName)
                .originalPath(originalPath)
                .type(itemType.name())
                .size(sizeBytes == null ? 0 : sizeBytes)
                .deletedAt(deletedAt)
                .build();
    }

    public Trash(
            String id,
            String originalPath,
            String storedPath,
            String itemName,
            FileType itemType,
            Long sizeBytes,
            Instant deletedAt) {
        this.id = id;
        this.originalPath = originalPath;
        this.storedPath = storedPath;
        this.itemName = itemName;
        this.itemType = itemType;
        this.sizeBytes = sizeBytes;
        this.deletedAt = deletedAt;
    }
}
