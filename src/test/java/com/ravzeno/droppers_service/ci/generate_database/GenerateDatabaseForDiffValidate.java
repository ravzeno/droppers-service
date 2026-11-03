package com.ravzeno.droppers_service.ci.generate_database;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles(resolver = GenerateDatabaseProfileForDiffValidateResolver.class)
class GenerateDatabaseForDiffValidate {
    @Test
    void contextLoads() {
        assertTrue(true);
    }
}
