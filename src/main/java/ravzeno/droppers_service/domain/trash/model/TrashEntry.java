package ravzeno.droppers_service.domain.trash.model;

import jakarta.persistence.*;
import java.time.Instant;
import ravzeno.droppers_service.common.persistence.InstantStringConverter;
import ravzeno.droppers_service.domain.file.model.FileType;

@Entity
@Table(name = "trash_entries")
public class TrashEntry {
    @Id private String id;

    @Column(name = "original_path")
    private String originalPath;

    @Column(name = "stored_path")
    private String storedPath;

    @Column(name = "item_name")
    private String itemName;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type")
    private FileType itemType;

    @Column(name = "size_bytes")
    private long sizeBytes;

    @Convert(converter = InstantStringConverter.class)
    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected TrashEntry() {}

    public TrashEntry(
            String id,
            String originalPath,
            String storedPath,
            String itemName,
            FileType itemType,
            long sizeBytes,
            Instant deletedAt) {
        this.id = id;
        this.originalPath = originalPath;
        this.storedPath = storedPath;
        this.itemName = itemName;
        this.itemType = itemType;
        this.sizeBytes = sizeBytes;
        this.deletedAt = deletedAt;
    }

    public String id() {
        return id;
    }

    public String originalPath() {
        return originalPath;
    }

    public String storedPath() {
        return storedPath;
    }

    public String itemName() {
        return itemName;
    }

    public FileType itemType() {
        return itemType;
    }

    public long sizeBytes() {
        return sizeBytes;
    }

    public Instant deletedAt() {
        return deletedAt;
    }
}
