package com.ravzeno.droppers_service.domains.device;

import com.ravzeno.droppers_service.domains.device.dto.get_all.DeviceDto;
import com.ravzeno.droppers_service.domains.device.model.Device;
import org.springframework.stereotype.Service;

@Service
public class DeviceMapper {
    public DeviceDto toDto(Device device) {
        return DeviceDto.builder()
                .id(device.getId())
                .name(device.getName())
                .pairedAt(device.getPairedAt())
                .lastSeenAt(device.getLastSeenAt())
                .build();
    }
}
