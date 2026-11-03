package com.ravzeno.droppers_service.domains.device.query;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQuery;
import com.ravzeno.droppers_service.commons.dto.PageOption;
import com.ravzeno.droppers_service.commons.utils.query_dsl.QueryDslHelperUtils;
import com.ravzeno.droppers_service.domains.device.dto.get_all.GetAllDeviceCriteria;
import com.ravzeno.droppers_service.domains.device.dto.get_all.GetAllDeviceSort;
import com.ravzeno.droppers_service.domains.device.model.Device;
import com.ravzeno.droppers_service.domains.device.model.QDevice;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class DeviceRepositoryGetAllQuery {

    private final EntityManager em;

    public BooleanBuilder buildCriteria(GetAllDeviceCriteria criteria) {
        val device = QDevice.device;
        val b = new BooleanBuilder();

        if (criteria.nameLike() != null && !criteria.nameLike().isBlank()) {
            b.and(device.name.containsIgnoreCase(criteria.nameLike().trim()));
        }

        if (criteria.clientIdLike() != null && !criteria.clientIdLike().isBlank()) {
            b.and(device.clientId.containsIgnoreCase(criteria.clientIdLike().trim()));
        }

        if (criteria.pairedAtFrom() != null) {
            b.and(device.pairedAt.goe(criteria.pairedAtFrom()));
        }

        if (criteria.pairedAtTo() != null) {
            b.and(device.pairedAt.loe(criteria.pairedAtTo()));
        }

        if (criteria.lastSeenAtFrom() != null) {
            b.and(device.lastSeenAt.goe(criteria.lastSeenAtFrom()));
        }

        if (criteria.lastSeenAtTo() != null) {
            b.and(device.lastSeenAt.loe(criteria.lastSeenAtTo()));
        }

        return b;
    }

    public OrderSpecifier<?>[] buildSort(GetAllDeviceSort sort) {
        val device = QDevice.device;
        List<OrderSpecifier<?>> specifiers = new ArrayList<>();

        val pairedAtSort = sort.pairedAtSort();
        if (pairedAtSort != null) {
            specifiers.add(QueryDslHelperUtils.buildSortSpec(device.pairedAt, pairedAtSort));
        }

        val lastSeenAtSort = sort.lastSeenAtSort();
        if (lastSeenAtSort != null) {
            specifiers.add(QueryDslHelperUtils.buildSortSpec(device.lastSeenAt, lastSeenAtSort));
        }

        return specifiers.toArray(new OrderSpecifier<?>[0]);
    }

    public List<Device> query(
            GetAllDeviceCriteria criteria, GetAllDeviceSort sort, PageOption pageOption) {
        val device = QDevice.device;

        val query = new JPAQuery<Void>(em);

        val q =
                query.select(device)
                        .from(device)
                        .where(buildCriteria(criteria))
                        .orderBy(buildSort(sort))
                        .offset(pageOption.getOffset())
                        .limit(pageOption.getPageSize());

        return q.fetch();
    }

    public Long count(GetAllDeviceCriteria criteria) {
        val device = QDevice.device;

        val query = new JPAQuery<Void>(em);

        val q = query.select(device.countDistinct()).from(device).where(buildCriteria(criteria));

        return q.fetchOne();
    }
}
