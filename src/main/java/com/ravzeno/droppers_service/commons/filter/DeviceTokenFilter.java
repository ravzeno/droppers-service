package com.ravzeno.droppers_service.commons.filter;

import com.ravzeno.droppers_service.commons.exception.core.SecurityViolationException;
import com.ravzeno.droppers_service.commons.security.enum_type.Role;
import com.ravzeno.droppers_service.domains.device.DeviceService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class DeviceTokenFilter extends OncePerRequestFilter {
    private static final String DEVICE_API_PREFIX = "/device/api/";

    private final DeviceService devices;

    public DeviceTokenFilter(DeviceService devices) {
        this.devices = devices;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith(DEVICE_API_PREFIX);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            devices.authenticate(request.getHeader("X-Droppers-Token"));
        } catch (SecurityViolationException exception) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Valid device token is required");
            return;
        }

        var authentication =
                UsernamePasswordAuthenticationToken.authenticated(
                        "device",
                        null,
                        List.of(new SimpleGrantedAuthority(Role.DEVICE.getAuthority())));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(request, response);
    }
}
