package com.ravzeno.droppers_service.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.ravzeno.droppers_service.commons.exception.core.SecurityViolationException;
import com.ravzeno.droppers_service.commons.filter.BackOfficeTokenFilter;
import com.ravzeno.droppers_service.commons.filter.DeviceTokenFilter;
import com.ravzeno.droppers_service.commons.security.config.RequestSecurityProperties;
import com.ravzeno.droppers_service.commons.security.enum_type.Role;
import com.ravzeno.droppers_service.domains.device.DeviceService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

class ApiTokenFilterTest {
    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void backOfficeFilterRejectsMissingAdminToken() throws Exception {
        var filter =
                new BackOfficeTokenFilter(
                        new RequestSecurityProperties("localhost", 10, false, 30, "secret", ""));
        var request = request("/back-office/api/v1/devices");
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, mock(FilterChain.class));

        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void backOfficeFilterAddsBackOfficeRoleForValidToken() throws Exception {
        var filter =
                new BackOfficeTokenFilter(
                        new RequestSecurityProperties("localhost", 10, false, 30, "secret", ""));
        var request = request("/back-office/api/v1/devices");
        request.addHeader("X-Droppers-Admin-Token", "secret");
        var response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .extracting("authority")
                .containsExactly(Role.ADMIN.getAuthority());
        verify(chain).doFilter(request, response);
    }

    @Test
    void deviceFilterValidatesDeviceTokenAndAddsDeviceRole() throws Exception {
        DeviceService devices = mock(DeviceService.class);
        var filter = new DeviceTokenFilter(devices);
        var request = request("/device/api/v1/files");
        request.addHeader("X-Droppers-Token", "device-token");
        var response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(devices).authenticate("device-token");
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .extracting("authority")
                .containsExactly(Role.DEVICE.getAuthority());
        verify(chain).doFilter(request, response);
    }

    @Test
    void deviceFilterRejectsInvalidDeviceToken() throws Exception {
        DeviceService devices = mock(DeviceService.class);
        doThrow(new SecurityViolationException("invalid")).when(devices).authenticate("bad-token");
        var filter = new DeviceTokenFilter(devices);
        var request = request("/device/api/v1/files");
        request.addHeader("X-Droppers-Token", "bad-token");
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, mock(FilterChain.class));

        assertThat(response.getStatus()).isEqualTo(401);
    }

    private MockHttpServletRequest request(String path) {
        var request = new MockHttpServletRequest();
        request.setRequestURI(path);
        return request;
    }
}
