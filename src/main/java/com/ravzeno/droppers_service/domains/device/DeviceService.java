package com.ravzeno.droppers_service.domains.device;

import com.ravzeno.droppers_service.commons.dto.PageOption;
import com.ravzeno.droppers_service.commons.dto.PageResponse;
import com.ravzeno.droppers_service.commons.exception.core.InvalidRequestException;
import com.ravzeno.droppers_service.commons.exception.core.ResourceNotFoundException;
import com.ravzeno.droppers_service.commons.exception.core.SecurityViolationException;
import com.ravzeno.droppers_service.commons.security.TokenHasher;
import com.ravzeno.droppers_service.commons.security.config.RequestSecurityProperties;
import com.ravzeno.droppers_service.domains.device.dto.get_all.DeviceDto;
import com.ravzeno.droppers_service.domains.device.dto.get_all.GetAllDeviceCriteria;
import com.ravzeno.droppers_service.domains.device.dto.get_all.GetAllDeviceSort;
import com.ravzeno.droppers_service.domains.device.model.Device;
import com.ravzeno.droppers_service.domains.device.model.DeviceSession;
import com.ravzeno.droppers_service.domains.device.session.DeviceSessionRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class DeviceService {
    private final DeviceRepository deviceRepository;
    private final DeviceSessionRepository deviceSessionRepository;
    private final RequestSecurityProperties requestSecurityProperties;
    private final DeviceRepositoryImpl deviceRepositoryImpl;
    private final DeviceMapper deviceMapper;

    @Transactional(readOnly = true)
    public PageResponse<DeviceDto> getAll(
            GetAllDeviceCriteria criteria, GetAllDeviceSort sort, PageOption pageOption) {
        validateCriteria(criteria);
        val items =
                deviceRepositoryImpl.getAll(criteria, sort, pageOption).stream()
                        .map(deviceMapper::toDto)
                        .toList();
        val counts = deviceRepositoryImpl.count(criteria);

        return PageResponse.create(items, counts, pageOption);
    }

    @Transactional(readOnly = true)
    public long count(GetAllDeviceCriteria criteria) {
        validateCriteria(criteria);
        return deviceRepositoryImpl.count(criteria);
    }

    private void validateCriteria(GetAllDeviceCriteria criteria) {
        if (criteria.pairedAtFrom() != null
                && criteria.pairedAtTo() != null
                && criteria.pairedAtFrom().isAfter(criteria.pairedAtTo())) {
            throw new InvalidRequestException("pairedAtFrom must be before or equal to pairedAtTo");
        }
        if (criteria.lastSeenAtFrom() != null
                && criteria.lastSeenAtTo() != null
                && criteria.lastSeenAtFrom().isAfter(criteria.lastSeenAtTo())) {
            throw new InvalidRequestException(
                    "lastSeenAtFrom must be before or equal to lastSeenAtTo");
        }
    }

    @Transactional
    public void register(String id, String name, String tokenHash) {
        Instant now = Instant.now();
        Device device =
                deviceRepository
                        .findByClientId(id)
                        .orElseGet(() -> Device.builder().clientId(id).pairedAt(now).build());
        device.setName(name);
        device.setTokenHash(tokenHash);
        deviceRepository.save(device);
        deviceSessionRepository.save(
                DeviceSession.builder()
                        .deviceId(id)
                        .tokenHash(tokenHash)
                        .expiresAt(
                                now.plus(
                                        Duration.ofDays(
                                                requestSecurityProperties.deviceSessionTtlDays())))
                        .build());
    }

    @Transactional
    public void revoke(UUID id) {
        Device device =
                deviceRepository
                        .findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Device not found"));
        deviceSessionRepository.deleteByDeviceId(device.getClientId());
        deviceRepository.delete(device);
    }

    @Transactional
    public void authenticate(String token) {
        if (token == null || token.isBlank()) {
            throw new SecurityViolationException("Valid device token is required");
        }

        String tokenHash = TokenHasher.sha256(token);
        if (!deviceSessionRepository.existsByTokenHashAndRevokedAtIsNullAndExpiresAtAfter(
                        tokenHash, Instant.now())
                && !deviceRepository.existsUnSessionTokenHash(tokenHash)) {
            throw new SecurityViolationException("Valid device token is required");
        }
        deviceRepository.updateLastSeenAt(tokenHash, Instant.now());
    }
}
