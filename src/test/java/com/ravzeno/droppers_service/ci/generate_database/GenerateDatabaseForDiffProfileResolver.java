package com.ravzeno.droppers_service.ci.generate_database;

import java.util.List;
import org.assertj.core.util.Lists;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.test.context.ActiveProfilesResolver;

public class GenerateDatabaseForDiffProfileResolver implements ActiveProfilesResolver {

    @Override
    public String[] resolve(Class<?> aClass) {
        List<String> springProfiles = Lists.newArrayList("gen-db-for-diff");
        String activeProfile = System.getenv("SPRING_PROFILES_ACTIVE");
        System.out.println("Active Spring Profile: " + activeProfile); // Debuggi
        // ng line
        if (StringUtils.isNotBlank(activeProfile)) {
            springProfiles.add(activeProfile);
        }

        return springProfiles.toArray(String[]::new);
    }
}
