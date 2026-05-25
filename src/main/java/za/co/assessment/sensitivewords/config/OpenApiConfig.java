package za.co.assessment.sensitivewords.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI sensitiveWordsOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sensitive Words Service API")
                        .version("v1")
                        .description("API for managing sensitive words and sanitizing input text.")
                        .contact(new Contact().name("Engineering Team")));
    }
}
