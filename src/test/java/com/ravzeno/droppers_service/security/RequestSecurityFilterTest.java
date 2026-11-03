package com.ravzeno.droppers_service.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.ravzeno.droppers_service.commons.filter.RequestSecurityFilter;
import com.ravzeno.droppers_service.commons.security.config.RequestSecurityProperties;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RequestSecurityFilterTest {
    private final RequestSecurityProperties properties =
            new RequestSecurityProperties("localhost,127.0.0.1", 1, true, 30, "", "");
    private final RequestSecurityFilter filter =
            new RequestSecurityFilter(
                    properties, Clock.fixed(Instant.parse("2026-10-22T00:00:00Z"), ZoneOffset.UTC));

    @Test
    void rejectsRequestsWithAnUnrecognizedHost() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServerName("untrusted.example");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    void permitsAnAllowedOriginAndAddsCorsHeaders() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServerName("127.0.0.1");
        request.addHeader("Origin", "http://localhost:3000");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getHeader("Access-Control-Allow-Origin"))
                .isEqualTo("http://localhost:3000");
    }

    @Test
    void limitsRequestsPerClient() throws Exception {
        MockHttpServletRequest firstRequest = allowedRequest();
        MockHttpServletResponse firstResponse = new MockHttpServletResponse();
        filter.doFilter(firstRequest, firstResponse, new MockFilterChain());

        MockHttpServletRequest secondRequest = allowedRequest();
        MockHttpServletResponse secondResponse = new MockHttpServletResponse();
        filter.doFilter(secondRequest, secondResponse, new MockFilterChain());

        assertThat(firstResponse.getStatus()).isEqualTo(200);
        assertThat(secondResponse.getStatus()).isEqualTo(429);
        assertThat(secondResponse.getHeader("Retry-After")).isEqualTo("60");
    }

    private MockHttpServletRequest allowedRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServerName("127.0.0.1");
        request.setRemoteAddr("192.0.2.1");
        request.setSecure(true);
        return request;
    }

    @Test
    void trustsForwardedTlsOnlyFromConfiguredProxy() throws Exception {
        RequestSecurityProperties proxyProperties =
                new RequestSecurityProperties("localhost", 10, true, 30, "", "10.0.0.2");
        RequestSecurityFilter proxyFilter =
                new RequestSecurityFilter(proxyProperties, Clock.systemUTC());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServerName("localhost");
        request.setRemoteAddr("10.0.0.2");
        request.addHeader("X-Forwarded-For", "192.0.2.10");
        request.addHeader("X-Forwarded-Proto", "https");
        MockHttpServletResponse response = new MockHttpServletResponse();

        proxyFilter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void doesNotTrustForwardedTlsFromUnconfiguredLoopbackProxy() throws Exception {
        RequestSecurityProperties proxyProperties =
                new RequestSecurityProperties("localhost", 10, true, 30, "", "");
        RequestSecurityFilter proxyFilter =
                new RequestSecurityFilter(proxyProperties, Clock.systemUTC());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServerName("localhost");
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("X-Forwarded-For", "192.0.2.10");
        request.addHeader("X-Forwarded-Proto", "https");
        MockHttpServletResponse response = new MockHttpServletResponse();

        proxyFilter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(426);
    }
}
