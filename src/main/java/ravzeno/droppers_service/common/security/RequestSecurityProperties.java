package ravzeno.droppers_service.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "droppers.security")
public record RequestSecurityProperties(
        String allowedHosts,
        int requestsPerMinute,
        boolean requireTlsForLan,
        int deviceSessionTtlDays) {
    public RequestSecurityProperties {
        allowedHosts =
                allowedHosts == null || allowedHosts.isBlank()
                        ? "localhost,127.0.0.1,::1"
                        : allowedHosts;
        requestsPerMinute = requestsPerMinute <= 0 ? 120 : requestsPerMinute;
        deviceSessionTtlDays = deviceSessionTtlDays <= 0 ? 30 : deviceSessionTtlDays;
    }
}
