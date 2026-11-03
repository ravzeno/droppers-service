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
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@AllArgsConstructor
@Slf4j
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

    @Transactional(rollbackFor = Exception.class)
    public void trash(String rawPath) throws IOException {
        Path source = files.existing(rawPath);
        if (paths.isRoot(source))
            throw new InvalidRequestException("Storage root operation is not allowed");
        long size = size(source);
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
        Path slot = null;
        Path target = null;
        try {
            slot = storage.createSlot(id);
            target = slot.resolve(source.getFileName());
            registerTrashRollback(source, target, slot);
            storage.move(source, target);
            entry.setStoredPath(id + "/" + source.getFileName());
            entry.setSizeBytes(size);
            entries.save(entry);
        } catch (IOException | RuntimeException e) {
            try {
                if (target != null && Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
                    if (!Files.exists(source, LinkOption.NOFOLLOW_LINKS)) {
                        storage.move(target, source);
                    } else {
                        log.error(
                                "Trash move left a second copy at {}; preserving it after failure",
                                target,
                                e);
                        slot = null;
                    }
                }
                if (slot != null) {
                    storage.remove(slot);
                }
                entries.delete(entry);
            } catch (IOException | RuntimeException cleanupException) {
                e.addSuppressed(cleanupException);
            }
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void restore(UUID id) throws IOException {
        Trash entry = require(id);
        Path stored = storage.root().resolve(entry.getStoredPath()).normalize();
        if (!stored.startsWith(storage.root()) || !Files.exists(stored))
            throw new ResourceNotFoundException("Trashed item is unavailable");
        Path original =
                paths.resolveForCreation(
                        parent(entry.getOriginalPath()), fileName(entry.getOriginalPath()));
        Path target = uniqueRestoredName(original);
        boolean moved = false;
        try {
            storage.move(stored, target);
            moved = true;
            entries.deleteById(id);
        } catch (IOException | RuntimeException e) {
            if (moved) {
                try {
                    storage.move(target, stored);
                } catch (IOException | RuntimeException rollbackException) {
                    e.addSuppressed(rollbackException);
                }
            }
            throw e;
        }
        registerRestoreCompletion(stored, target);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deletePermanently(UUID id) throws IOException {
        Trash entry = require(id);
        Path slot = storage.root().resolve(entry.getStoredPath()).getParent();
        entries.deleteById(id);
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        try {
                            storage.remove(slot);
                        } catch (IOException | RuntimeException cleanupException) {
                            log.warn(
                                    "Could not permanently remove trash data at {}",
                                    slot,
                                    cleanupException);
                        }
                    }
                });
    }

    private void registerTrashRollback(Path source, Path target, Path slot) {
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        if (status == STATUS_COMMITTED) {
                            return;
                        }
                        try {
                            if (Files.exists(target, LinkOption.NOFOLLOW_LINKS)
                                    && !Files.exists(source, LinkOption.NOFOLLOW_LINKS)) {
                                storage.move(target, source);
                            }
                            if (!Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
                                storage.remove(slot);
                            }
                        } catch (IOException | RuntimeException rollbackException) {
                            log.error(
                                    "Could not roll back trash move from {} to {}",
                                    source,
                                    target,
                                    rollbackException);
                        }
                    }
                });
    }

    private void registerRestoreCompletion(Path stored, Path target) {
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        try {
                            if (status == STATUS_COMMITTED) {
                                storage.remove(stored.getParent());
                            } else if (Files.exists(target, LinkOption.NOFOLLOW_LINKS)
                                    && !Files.exists(stored, LinkOption.NOFOLLOW_LINKS)) {
                                storage.move(target, stored);
                            }
                        } catch (IOException | RuntimeException completionException) {
                            log.error(
                                    "Could not complete trash restore from {} to {}",
                                    stored,
                                    target,
                                    completionException);
                        }
                    }
                });
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
        var size = new long[1];
        Files.walkFileTree(
                path,
                new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attributes)
                            throws IOException {
                        if (attributes.isRegularFile()) {
                            size[0] = Math.addExact(size[0], attributes.size());
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
        return size[0];
    }
}
