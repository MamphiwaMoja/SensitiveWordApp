package za.co.assessment.sensitivewords.config;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;

public final class DefaultProfileUtil {

    private DefaultProfileUtil() {
    }

    public static void addDefaultProfile(SpringApplication app, String defaultProfile) {
        // Keep local startup explicit without requiring every run command to pass a profile.
        Map<String, Object> defaultProperties = new HashMap<>();
        defaultProperties.put("spring.profiles.default", defaultProfile);
        app.setDefaultProperties(defaultProperties);
    }
}
