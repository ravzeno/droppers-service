package com.ravzeno.droppers_service.domains.device;

import com.ravzeno.droppers_service.commons.dto.PageOption;
import com.ravzeno.droppers_service.domains.device.dto.get_all.GetAllDeviceCriteria;
import com.ravzeno.droppers_service.domains.device.dto.get_all.GetAllDeviceSort;
import com.ravzeno.droppers_service.domains.device.model.Device;
import com.ravzeno.droppers_service.domains.device.query.DeviceRepositoryGetAllQuery;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class DeviceRepositoryImpl {

    private final DeviceRepositoryGetAllQuery getAllQuery;

    public List<Device> getAll(
            GetAllDeviceCriteria criteria, GetAllDeviceSort sort, PageOption pageOption) {
        return getAllQuery.query(criteria, sort, pageOption);
    }

    public Long count(GetAllDeviceCriteria criteria) {
        return getAllQuery.count(criteria);
    }
}
