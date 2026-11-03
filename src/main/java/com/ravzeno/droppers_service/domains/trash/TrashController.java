package com.ravzeno.droppers_service.domains.trash;

import com.ravzeno.droppers_service.commons.dto.PageOption;
import com.ravzeno.droppers_service.commons.dto.PageResponse;
import com.ravzeno.droppers_service.commons.dto.SuccessResponse;
import com.ravzeno.droppers_service.domains.trash.dto.TrashEntryResponse;
import com.ravzeno.droppers_service.domains.trash.dto.get_all.GetAllTrashCriteria;
import com.ravzeno.droppers_service.domains.trash.dto.get_all.GetAllTrashSort;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/trash")
public class TrashController {
    private final TrashService trash;

    @GetMapping
    public SuccessResponse<PageResponse<TrashEntryResponse>> getAll(
            @Valid GetAllTrashCriteria criteria,
            @Valid GetAllTrashSort sort,
            @Valid PageOption pageOption) {
        if (criteria == null) criteria = GetAllTrashCriteria.DEFAULT;
        if (sort == null) sort = GetAllTrashSort.DEFAULT;

        return SuccessResponse.create(trash.getAll(criteria, sort, pageOption));
    }

    @GetMapping("/count")
    public SuccessResponse<Long> count(@Valid GetAllTrashCriteria criteria) {
        if (criteria == null) criteria = GetAllTrashCriteria.DEFAULT;

        return SuccessResponse.create(trash.count(criteria));
    }

    @PostMapping("/{id}/restore")
    public SuccessResponse<Void> restore(@PathVariable UUID id) throws IOException {
        trash.restore(id);

        return SuccessResponse.create();
    }

    @DeleteMapping("/{id}")
    public SuccessResponse<Void> permanentlyDelete(@PathVariable UUID id) throws IOException {
        trash.deletePermanently(id);

        return SuccessResponse.create();
    }
}
