package za.co.assessment.sensitivewords.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(ApplicationProperties.class)
public class SecurityConfiguration {

    @Bean
    @Profile(Constants.SPRING_PROFILE_LOCAL)
    SecurityFilterChain localSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                .formLogin(formLogin -> formLogin.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .logout(logout -> logout.disable())
                .build();
    }

    @Bean
    @Profile(Constants.SPRING_PROFILE_LOCAL)
    UserDetailsService localUserDetailsService(PasswordEncoder passwordEncoder) {
        return new InMemoryUserDetailsManager(
                User.withUsername("local")
                        .password(passwordEncoder.encode("local"))
                        .roles("API_USER")
                        .build()
        );
    }

    @Bean
    @Profile("!" + Constants.SPRING_PROFILE_LOCAL)
    SecurityFilterChain securedSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/v1/health").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .formLogin(formLogin -> formLogin.disable())
                .logout(logout -> logout.disable())
                .build();
    }

    @Bean
    @Profile("!" + Constants.SPRING_PROFILE_LOCAL)
    UserDetailsService userDetailsService(ApplicationProperties properties, PasswordEncoder passwordEncoder) {
        ApplicationProperties.Security.Basic basic = properties.getSecurity().getBasic();
        String username = requiredValue(basic.getUsername(), "sensitive-words.security.basic.username");
        String password = requiredValue(basic.getPassword(), "sensitive-words.security.basic.password");
        String role = normalizeRole(requiredValue(basic.getRole(), "sensitive-words.security.basic.role"));

        return new InMemoryUserDetailsManager(
                User.withUsername(username)
                        .password(passwordEncoder.encode(password))
                        .roles(role)
                        .build()
        );
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    private String requiredValue(String value, String propertyName) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(propertyName + " must be configured for non-local profiles");
        }
        return value.trim();
    }

    private String normalizeRole(String role) {
        return role.startsWith("ROLE_") ? role.substring("ROLE_".length()) : role;
    }
}
