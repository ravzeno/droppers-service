package com.ravzeno.droppers_service.commons.filter;

import com.ravzeno.droppers_service.commons.security.config.RequestSecurityProperties;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Clock;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.filter.OncePerRequestFilter;

public class RequestSecurityFilter extends OncePerRequestFilter {
    private static final long RATE_LIMIT_WINDOW_MILLIS = 60_000;
    private static final int MAX_RATE_LIMIT_CLIENTS = 10_000;
    private static final int STATUS_UPGRADE_REQUIRED = 426;

    private final RequestSecurityProperties properties;
    private final Set<String> allowedHosts;
    private final Map<String, RateLimitWindow> rateLimits = new LinkedHashMap<>(16, 0.75f, true);
    private final Clock clock;

    public RequestSecurityFilter(RequestSecurityProperties properties) {
        this(properties, Clock.systemUTC());
    }

    public RequestSecurityFilter(RequestSecurityProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
        allowedHosts =
                Arrays.stream(properties.allowedHosts().split(","))
                        .map(String::trim)
                        .filter(host -> !host.isEmpty())
                        .map(String::toLowerCase)
                        .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    protected void doFilterInternal(
            @Nonnull HttpServletRequest request,
            @Nonnull HttpServletResponse response,
            @Nonnull FilterChain filterChain)
            throws ServletException, IOException {
        if (!isAllowedHost(request)) {
            reject(response, HttpServletResponse.SC_BAD_REQUEST, "Unrecognized Host header");
            return;
        }
        if (properties.requireTlsForLan() && isLanRequest(request) && !isSecureRequest(request)) {
            reject(response, STATUS_UPGRADE_REQUIRED, "TLS is required for LAN access");
            return;
        }

        String origin = request.getHeader(HttpHeaders.ORIGIN);
        if (origin != null && !isAllowedOrigin(origin)) {
            reject(response, HttpServletResponse.SC_FORBIDDEN, "Origin is not allowed");
            return;
        }
        if (origin != null) {
            addCorsHeaders(response, origin);
        }
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }
        if (!withinRateLimit(clientAddress(request))) {
            response.setHeader(HttpHeaders.RETRY_AFTER, "60");
            reject(response, 429, "Too many requests");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean isAllowedHost(HttpServletRequest request) {
        return allowedHosts.contains(request.getServerName().toLowerCase());
    }

    private boolean isAllowedOrigin(String origin) {
        try {
            URI uri = new URI(origin);
            return (uri.getScheme().equals("http") || uri.getScheme().equals("https"))
                    && uri.getHost() != null
                    && allowedHosts.contains(uri.getHost().toLowerCase());
        } catch (URISyntaxException e) {
            return false;
        }
    }

    private boolean isLanRequest(HttpServletRequest request) {
        if (!isTrustedProxy(request.getRemoteAddr())
                && isLoopback(request.getRemoteAddr())
                && (request.getHeader("X-Forwarded-For") != null
                        || request.getHeader("X-Forwarded-Proto") != null)) {
            return true;
        }
        return !isLoopback(clientAddress(request));
    }

    private boolean isLoopback(String address) {
        try {
            return InetAddress.getByName(address).isLoopbackAddress();
        } catch (Exception exception) {
            return false;
        }
    }

    private String clientAddress(HttpServletRequest request) {
        String remoteAddress = request.getRemoteAddr();
        if (!isTrustedProxy(remoteAddress)) {
            return remoteAddress;
        }
        String forwardedFor = request.getHeader("X-Forwarded-For");
        return forwardedFor == null || forwardedFor.isBlank()
                ? remoteAddress
                : forwardedFor.split(",", 2)[0].trim();
    }

    private boolean isSecureRequest(HttpServletRequest request) {
        return request.isSecure()
                || (isTrustedProxy(request.getRemoteAddr())
                        && "https"
                                .equalsIgnoreCase(
                                        firstHeaderValue(request.getHeader("X-Forwarded-Proto"))));
    }

    private String firstHeaderValue(String value) {
        return value == null ? "" : value.split(",", 2)[0].trim();
    }

    private boolean isTrustedProxy(String remoteAddress) {
        return Arrays.stream(properties.trustedProxies().split(","))
                .map(String::trim)
                .anyMatch(proxy -> !proxy.isEmpty() && proxy.equals(remoteAddress));
    }

    private synchronized boolean withinRateLimit(String remoteAddress) {
        long now = clock.millis();
        RateLimitWindow current = rateLimits.get(remoteAddress);
        RateLimitWindow window;
        if (current == null || now - current.startedAtMillis() >= RATE_LIMIT_WINDOW_MILLIS) {
            if (current == null && rateLimits.size() >= MAX_RATE_LIMIT_CLIENTS) {
                Iterator<String> oldestClient = rateLimits.keySet().iterator();
                oldestClient.next();
                oldestClient.remove();
            }
            window = new RateLimitWindow(now, 1);
        } else {
            window = new RateLimitWindow(current.startedAtMillis(), current.requestCount() + 1);
        }
        rateLimits.put(remoteAddress, window);
        return window.requestCount() <= properties.requestsPerMinute();
    }

    private void addCorsHeaders(HttpServletResponse response, String origin) {
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
        response.setHeader(
                HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET,POST,PUT,PATCH,DELETE,OPTIONS");
        response.setHeader(
                HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
                "Content-Type, X-Droppers-Token, X-Droppers-Admin-Token, X-Pairing-Claim-Token");
        response.setHeader(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "600");
        response.setHeader(HttpHeaders.VARY, HttpHeaders.ORIGIN);
    }

    private void reject(HttpServletResponse response, int status, String message)
            throws IOException {
        response.sendError(status, message);
    }

    private record RateLimitWindow(long startedAtMillis, int requestCount) {}
}
