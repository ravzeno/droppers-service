package com.ravzeno.droppers_service.ci.key_generate;

import java.security.SecureRandom;
import org.junit.jupiter.api.Test;

public class KeyGeneratorCI {
    private static final String CHARACTERS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public static String generateRandomString(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }

    @Test
    public void generateEncryptSecretKey() {
        String randomString = generateRandomString(16);
        System.out.println("Encrypt secret key: " + randomString);
    }

    @Test
    public void generateJwtSecretKey() {
        String randomString = generateRandomString(16);
        System.out.println("Jwt secret key: " + randomString);
    }
}
