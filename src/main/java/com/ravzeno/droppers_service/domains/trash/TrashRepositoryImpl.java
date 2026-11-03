package com.ravzeno.droppers_service.domains.trash;

import com.ravzeno.droppers_service.commons.dto.PageOption;
import com.ravzeno.droppers_service.domains.trash.dto.get_all.GetAllTrashCriteria;
import com.ravzeno.droppers_service.domains.trash.dto.get_all.GetAllTrashSort;
import com.ravzeno.droppers_service.domains.trash.model.Trash;
import com.ravzeno.droppers_service.domains.trash.query.TrashRepositoryGetAllQuery;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class TrashRepositoryImpl {
    private final TrashRepositoryGetAllQuery getAllQuery;

    public List<Trash> getAll(
            GetAllTrashCriteria criteria, GetAllTrashSort sort, PageOption pageOption) {
        return getAllQuery.query(criteria, sort, pageOption);
    }

    public long count(GetAllTrashCriteria criteria) {
        return getAllQuery.count(criteria);
    }
}
