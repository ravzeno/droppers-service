package com.ravzeno.droppers_service.domains.trash.model;

import com.ravzeno.droppers_service.commons.model.converter.InstantStringConverter;
import com.ravzeno.droppers_service.domains.file.model.FileType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "trash_entries")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Trash {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String originalPath;

    @Column(nullable = false)
    private String storedPath;

    @Column(nullable = false)
    private String itemName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileType itemType;

    @Column private Long sizeBytes;

    @Convert(converter = InstantStringConverter.class)
    @Column(nullable = false)
    private Instant deletedAt;
}
