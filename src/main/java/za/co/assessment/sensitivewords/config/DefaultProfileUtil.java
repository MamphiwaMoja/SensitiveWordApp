package za.co.assessment.sensitivewords.config;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;

public final class DefaultProfileUtil {

    private DefaultProfileUtil() {
    }

    public static void addDefaultProfile(SpringApplication app, String defaultProfile) {
        Map<String, Object> defaultProperties = new HashMap<>();
        defaultProperties.put("spring.profiles.default", defaultProfile);
        app.setDefaultProperties(defaultProperties);
    }
}
