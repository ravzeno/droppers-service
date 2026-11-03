package com.ravzeno.droppers_service.domains.trash;

import com.ravzeno.droppers_service.commons.dto.PageOption;
import com.ravzeno.droppers_service.commons.dto.PageResponse;
import com.ravzeno.droppers_service.commons.exception.core.InvalidRequestException;
import com.ravzeno.droppers_service.commons.exception.core.ResourceNotFoundException;
import com.ravzeno.droppers_service.domains.file.FileService;
import com.ravzeno.droppers_service.domains.file.model.FileType;
import com.ravzeno.droppers_service.domains.storage.SecurePathResolver;
import com.ravzeno.droppers_service.domains.trash.dto.TrashEntryResponse;
import com.ravzeno.droppers_service.domains.trash.dto.get_all.GetAllTrashCriteria;
import com.ravzeno.droppers_service.domains.trash.dto.get_all.GetAllTrashSort;
import com.ravzeno.droppers_service.domains.trash.local_trash_storage.LocalTrashStorage;
import com.ravzeno.droppers_service.domains.trash.model.Trash;
import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class TrashService {
    private final FileService files;
    private final SecurePathResolver paths;
    private final LocalTrashStorage storage;
    private final TrashRepository entries;
    private final TrashRepositoryImpl entriesCustom;
    private final TrashMapper mapper;

    @Transactional(readOnly = true)
    public PageResponse<TrashEntryResponse> getAll(
            GetAllTrashCriteria criteria, GetAllTrashSort sort, PageOption pageOption) {
        validateCriteria(criteria);
        val items =
                entriesCustom.getAll(criteria, sort, pageOption).stream()
                        .map(mapper::toDto)
                        .toList();
        return PageResponse.create(items, entriesCustom.count(criteria), pageOption);
    }

    @Transactional(readOnly = true)
    public long count(GetAllTrashCriteria criteria) {
        validateCriteria(criteria);
        return entriesCustom.count(criteria);
    }

    private void validateCriteria(GetAllTrashCriteria criteria) {
        if (criteria.deletedAtFrom() != null
                && criteria.deletedAtTo() != null
                && criteria.deletedAtFrom().isAfter(criteria.deletedAtTo())) {
            throw new InvalidRequestException(
                    "deletedAtFrom must be before or equal to deletedAtTo");
        }
    }

    @Transactional
    public void trash(String rawPath) throws IOException {
        Path source = files.existing(rawPath);
        if (paths.isRoot(source))
            throw new InvalidRequestException("Storage root operation is not allowed");
        Trash entry =
                entries.save(
                        Trash.builder()
                                .originalPath(paths.relative(source))
                                .storedPath("")
                                .itemName(source.getFileName().toString())
                                .itemType(
                                        Files.isDirectory(source)
                                                ? FileType.DIRECTORY
                                                : FileType.FILE)
                                .sizeBytes(0L)
                                .deletedAt(Instant.now())
                                .build());
        String id = entry.getId().toString();
        Path slot = storage.createSlot(id), target = slot.resolve(source.getFileName());
        try {
            Files.move(source, target);
            long size = size(target);
            entry.setStoredPath(id + "/" + source.getFileName());
            entry.setSizeBytes(size);
            entries.save(entry);
        } catch (IOException | RuntimeException e) {
            storage.remove(slot);
            entries.delete(entry);
            throw e;
        }
    }

    @Transactional
    public void restore(UUID id) throws IOException {
        Trash entry = require(id);
        Path stored = storage.root().resolve(entry.getStoredPath()).normalize();
        if (!stored.startsWith(storage.root()) || !Files.exists(stored))
            throw new ResourceNotFoundException("Trashed item is unavailable");
        Path original =
                paths.resolveForCreation(
                        parent(entry.getOriginalPath()), fileName(entry.getOriginalPath()));
        Path target = uniqueRestoredName(original);
        Files.move(stored, target);
        storage.remove(stored.getParent());
        entries.deleteById(id);
    }

    @Transactional
    public void deletePermanently(UUID id) throws IOException {
        Trash entry = require(id);
        storage.remove(storage.root().resolve(entry.getStoredPath()).getParent());
        entries.deleteById(id);
    }

    private Trash require(UUID id) {
        return entries.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trash entry not found"));
    }

    private String parent(String p) {
        int index = p.lastIndexOf('/');
        return index < 0 ? "" : p.substring(0, index);
    }

    private String fileName(String p) {
        int index = p.lastIndexOf('/');
        return index < 0 ? p : p.substring(index + 1);
    }

    private Path uniqueRestoredName(Path desired) {
        if (!Files.exists(desired)) return desired;
        String name = desired.getFileName().toString();
        int dot = name.lastIndexOf('.');
        String stem = dot > 0 ? name.substring(0, dot) : name,
                ext = dot > 0 ? name.substring(dot) : "";
        for (int i = 1; ; i++) {
            Path candidate = desired.resolveSibling(stem + " (restored " + i + ")" + ext);
            if (!Files.exists(candidate)) return candidate;
        }
    }

    private long size(Path path) throws IOException {
        if (Files.isRegularFile(path)) return Files.size(path);
        try (var walk = Files.walk(path)) {
            return walk.filter(Files::isRegularFile)
                    .mapToLong(
                            p -> {
                                try {
                                    return Files.size(p);
                                } catch (IOException e) {
                                    return 0;
                                }
                            })
                    .sum();
        }
    }
}
