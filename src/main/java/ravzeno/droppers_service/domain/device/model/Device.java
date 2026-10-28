package ravzeno.droppers_service.domain.device.model;

import java.time.Instant;

/**
 * Safe device representation returned by the API. Authentication data stays in persistence types.
 */
public record Device(String id, String name, Instant pairedAt, Instant lastSeenAt) {}
