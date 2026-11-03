package com.ravzeno.droppers_service.commons.filter;

import com.ravzeno.droppers_service.commons.security.config.RequestSecurityProperties;
import com.ravzeno.droppers_service.commons.security.enum_type.Role;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class BackOfficeTokenFilter extends OncePerRequestFilter {
    private static final String BACK_OFFICE_API_PREFIX = "/back-office/api/";

    private final RequestSecurityProperties properties;

    public BackOfficeTokenFilter(RequestSecurityProperties properties) {
        this.properties = properties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith(BACK_OFFICE_API_PREFIX);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @Nonnull HttpServletResponse response,
            @Nonnull FilterChain chain)
            throws ServletException, IOException {
        String configuredToken = properties.adminToken();
        String providedToken = request.getHeader("X-Droppers-Admin-Token");
        if (providedToken == null
                || configuredToken.isBlank()
                || !MessageDigest.isEqual(
                        configuredToken.getBytes(StandardCharsets.UTF_8),
                        providedToken.getBytes(StandardCharsets.UTF_8))) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Valid admin token is required");
            return;
        }

        var authentication =
                UsernamePasswordAuthenticationToken.authenticated(
                        "back-office",
                        null,
                        List.of(new SimpleGrantedAuthority(Role.ADMIN.getAuthority())));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(request, response);
    }
}
