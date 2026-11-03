package com.ravzeno.droppers_service.ci.generate_database;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles(resolver = GenerateDatabaseProfileForDiffValidateResolver.class)
class GenerateDatabaseForDiffValidate {
    @Test
    void contextLoads() {
        assertTrue(true);
    }
}
