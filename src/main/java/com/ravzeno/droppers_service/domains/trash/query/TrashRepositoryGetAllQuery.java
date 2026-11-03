package com.ravzeno.droppers_service.domains.trash.query;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQuery;
import com.ravzeno.droppers_service.commons.dto.PageOption;
import com.ravzeno.droppers_service.commons.utils.query_dsl.QueryDslHelperUtils;
import com.ravzeno.droppers_service.domains.trash.dto.get_all.GetAllTrashCriteria;
import com.ravzeno.droppers_service.domains.trash.dto.get_all.GetAllTrashSort;
import com.ravzeno.droppers_service.domains.trash.model.QTrash;
import com.ravzeno.droppers_service.domains.trash.model.Trash;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class TrashRepositoryGetAllQuery {
    private final EntityManager em;

    private BooleanBuilder buildCriteria(GetAllTrashCriteria criteria) {
        val trash = QTrash.trash;
        val builder = new BooleanBuilder();
        if (criteria.nameLike() != null && !criteria.nameLike().isBlank()) {
            builder.and(trash.itemName.containsIgnoreCase(criteria.nameLike().trim()));
        }
        if (criteria.originalPathLike() != null && !criteria.originalPathLike().isBlank()) {
            builder.and(trash.originalPath.containsIgnoreCase(criteria.originalPathLike().trim()));
        }
        if (criteria.deletedAtFrom() != null) {
            builder.and(trash.deletedAt.goe(criteria.deletedAtFrom()));
        }
        if (criteria.deletedAtTo() != null) {
            builder.and(trash.deletedAt.loe(criteria.deletedAtTo()));
        }
        return builder;
    }

    private OrderSpecifier<?>[] buildSort(GetAllTrashSort sort) {
        val trash = QTrash.trash;
        List<OrderSpecifier<?>> specifiers = new ArrayList<>();
        if (sort.deletedAtSort() != null) {
            specifiers.add(
                    QueryDslHelperUtils.buildSortSpec(trash.deletedAt, sort.deletedAtSort()));
        }

        if (sort.nameSort() != null) {
            specifiers.add(QueryDslHelperUtils.buildSortSpec(trash.itemName, sort.nameSort()));
        }

        if (sort.sizeSort() != null) {
            specifiers.add(QueryDslHelperUtils.buildSortSpec(trash.sizeBytes, sort.sizeSort()));
        }
        return specifiers.toArray(new OrderSpecifier<?>[0]);
    }

    public List<Trash> query(
            GetAllTrashCriteria criteria, GetAllTrashSort sort, PageOption pageOption) {
        val trash = QTrash.trash;
        return new JPAQuery<Trash>(em)
                .select(trash)
                .from(trash)
                .where(buildCriteria(criteria))
                .orderBy(buildSort(sort))
                .offset(pageOption.getOffset())
                .limit(pageOption.getPageSize())
                .fetch();
    }

    public long count(GetAllTrashCriteria criteria) {
        val trash = QTrash.trash;
        Long result =
                new JPAQuery<Long>(em)
                        .select(trash.count())
                        .from(trash)
                        .where(buildCriteria(criteria))
                        .fetchOne();
        return result == null ? 0 : result;
    }
}
