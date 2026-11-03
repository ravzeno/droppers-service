package com.ravzeno.droppers_service.domains.device;

import com.ravzeno.droppers_service.commons.dto.PageOption;
import com.ravzeno.droppers_service.commons.dto.PageResponse;
import com.ravzeno.droppers_service.commons.dto.SuccessResponse;
import com.ravzeno.droppers_service.domains.device.dto.DeviceDto;
import com.ravzeno.droppers_service.domains.device.dto.get_all.GetAllDeviceCriteria;
import com.ravzeno.droppers_service.domains.device.dto.get_all.GetAllDeviceSort;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/devices")
public class DeviceController {
    private final DeviceService deviceService;

    @GetMapping
    public SuccessResponse<PageResponse<DeviceDto>> getAll(
            @Valid GetAllDeviceCriteria criteria,
            @Valid GetAllDeviceSort sort,
            @Valid PageOption pageOption) {
        if (criteria == null) criteria = GetAllDeviceCriteria.DEFAULT;
        if (sort == null) sort = GetAllDeviceSort.DEFAULT;

        val result = deviceService.getAll(criteria, sort, pageOption);

        return SuccessResponse.create(result);
    }

    @GetMapping("/count")
    public SuccessResponse<Long> count(@Valid GetAllDeviceCriteria criteria) {
        if (criteria == null) criteria = GetAllDeviceCriteria.DEFAULT;

        long count = deviceService.count(criteria);

        return SuccessResponse.create(count);
    }

    @DeleteMapping("/{id}")
    public SuccessResponse<Void> revoke(@PathVariable UUID id) {
        deviceService.revoke(id);
        return SuccessResponse.create();
    }
}
