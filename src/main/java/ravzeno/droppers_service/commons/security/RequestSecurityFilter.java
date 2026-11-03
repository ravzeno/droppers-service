package ravzeno.droppers_service.commons.security;

import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Clock;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ravzeno.droppers_service.domains.device.DeviceService;

@Component
public class RequestSecurityFilter extends OncePerRequestFilter {
    private static final long RATE_LIMIT_WINDOW_MILLIS = 60_000;
    private static final int STATUS_UPGRADE_REQUIRED = 426;

    private final RequestSecurityProperties properties;
    private final Set<String> allowedHosts;
    private final ConcurrentHashMap<String, RateLimitWindow> rateLimits = new ConcurrentHashMap<>();
    private final Clock clock;
    private final DeviceService devices;

    @Autowired
    public RequestSecurityFilter(RequestSecurityProperties properties, DeviceService devices) {
        this(properties, Clock.systemUTC(), devices);
    }

    public RequestSecurityFilter(RequestSecurityProperties properties, Clock clock) {
        this(properties, clock, null);
    }

    private RequestSecurityFilter(
            RequestSecurityProperties properties, Clock clock, DeviceService devices) {
        this.properties = properties;
        this.clock = clock;
        this.devices = devices;
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
        if (properties.requireTlsForLan() && isLanRequest(request) && !request.isSecure()) {
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
        if (isLanRequest(request) && requiresDeviceToken(request.getRequestURI())) {
            try {
                if (devices == null) {
                    reject(
                            response,
                            HttpServletResponse.SC_UNAUTHORIZED,
                            "Valid device token is required");
                    return;
                }
                devices.authenticate(request.getHeader("X-Droppers-Token"));
            } catch (RuntimeException exception) {
                reject(
                        response,
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Valid device token is required");
                return;
            }
        }
        if (!withinRateLimit(request.getRemoteAddr())) {
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
        String host = request.getServerName().toLowerCase();
        return !host.equals("localhost") && !host.equals("127.0.0.1") && !host.equals("::1");
    }

    private boolean requiresDeviceToken(String path) {
        return path.startsWith("/api/v1/files")
                || path.startsWith("/api/v1/folders")
                || path.startsWith("/api/v1/trash")
                || path.startsWith("/api/v1/transfers/uploads")
                || path.equals("/api/v1/mobile/storage");
    }

    private boolean withinRateLimit(String remoteAddress) {
        long now = clock.millis();
        RateLimitWindow window =
                rateLimits.compute(
                        remoteAddress,
                        (key, current) -> {
                            if (current == null
                                    || now - current.startedAtMillis()
                                            >= RATE_LIMIT_WINDOW_MILLIS) {
                                return new RateLimitWindow(now, 1);
                            }
                            return new RateLimitWindow(
                                    current.startedAtMillis(), current.requestCount() + 1);
                        });
        return window.requestCount() <= properties.requestsPerMinute();
    }

    private void addCorsHeaders(HttpServletResponse response, String origin) {
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
        response.setHeader(
                HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET,POST,PUT,PATCH,DELETE,OPTIONS");
        response.setHeader(
                HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
                "Content-Type, X-Droppers-Token, X-Pairing-Claim-Token");
        response.setHeader(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "600");
        response.setHeader(HttpHeaders.VARY, HttpHeaders.ORIGIN);
    }

    private void reject(HttpServletResponse response, int status, String message)
            throws IOException {
        response.sendError(status, message);
    }

    private record RateLimitWindow(long startedAtMillis, int requestCount) {}
}
