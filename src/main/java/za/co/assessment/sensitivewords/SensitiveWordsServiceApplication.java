package za.co.assessment.sensitivewords;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.retry.annotation.EnableRetry;
import za.co.assessment.sensitivewords.config.ApplicationProperties;
import za.co.assessment.sensitivewords.config.Constants;
import za.co.assessment.sensitivewords.config.DefaultProfileUtil;

@SpringBootApplication
@EnableConfigurationProperties(ApplicationProperties.class)
@EnableRetry
public class SensitiveWordsServiceApplication {

    private static final Logger log = LoggerFactory.getLogger(SensitiveWordsServiceApplication.class);

    private final Environment env;

    public SensitiveWordsServiceApplication(Environment env) {
        this.env = env;
        validateProfiles();
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(SensitiveWordsServiceApplication.class);
        DefaultProfileUtil.addDefaultProfile(app, Constants.SPRING_PROFILE_LOCAL);
        Environment env = app.run(args).getEnvironment();
        logApplicationStartup(env);
    }

    private void validateProfiles() {
        Collection<String> activeProfiles = Arrays.asList(env.getActiveProfiles());
        if (
            activeProfiles.contains(Constants.SPRING_PROFILE_LOCAL) &&
            activeProfiles.contains(Constants.SPRING_PROFILE_PROD)
        ) {
            log.error("Misconfigured application: 'local' and 'prod' profiles must not run together.");
        }
        if (
            activeProfiles.contains(Constants.SPRING_PROFILE_TEST) &&
            activeProfiles.contains(Constants.SPRING_PROFILE_PROD)
        ) {
            log.error("Misconfigured application: 'test' and 'prod' profiles must not run together.");
        }
    }

    private static void logApplicationStartup(Environment env) {
        String protocol = Optional.ofNullable(env.getProperty("server.ssl.key-store")).map(value -> "https").orElse("http");
        String applicationName = env.getProperty("spring.application.name", "application");
        String serverPort = env.getProperty("server.port", "8080");
        String contextPath = Optional.ofNullable(env.getProperty("server.servlet.context-path"))
            .filter(path -> !path.isBlank())
            .orElse("/");
        String hostAddress = "localhost";
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException ex) {
            log.warn("Host name could not be determined, using localhost");
        }

        log.info(
            "\n----------------------------------------------------------\n\t" +
            "Application '{}' is running. Access URLs:\n\t" +
            "Local: \t\t{}://localhost:{}{}\n\t" +
            "External: \t{}://{}:{}{}\n\t" +
            "Profile(s): \t{}\n----------------------------------------------------------",
            applicationName,
            protocol,
            serverPort,
            contextPath,
            protocol,
            hostAddress,
            serverPort,
            contextPath,
            env.getActiveProfiles().length == 0 ? Arrays.toString(env.getDefaultProfiles()) : Arrays.toString(env.getActiveProfiles())
        );
    }
}
