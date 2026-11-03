package ravzeno.droppers_service.commons.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class TokenHasher {
    private static final String ALGORITHM = "SHA-256";

    private TokenHasher() {}

    public static String sha256(String value) {
        try {
            byte[] hash =
                    MessageDigest.getInstance(ALGORITHM)
                            .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }
}
