package com.ravzeno.droppers_service.commons.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "droppers.security")
public record RequestSecurityProperties(
        String allowedHosts,
        int requestsPerMinute,
        boolean requireTlsForLan,
        int deviceSessionTtlDays,
        String adminToken,
        String trustedProxies) {
    public RequestSecurityProperties {
        allowedHosts =
                allowedHosts == null || allowedHosts.isBlank()
                        ? "localhost,127.0.0.1,::1"
                        : allowedHosts;
        requestsPerMinute = requestsPerMinute <= 0 ? 120 : requestsPerMinute;
        deviceSessionTtlDays = deviceSessionTtlDays <= 0 ? 30 : deviceSessionTtlDays;
        adminToken = adminToken == null ? "" : adminToken;
        trustedProxies = trustedProxies == null ? "" : trustedProxies;
    }
}
