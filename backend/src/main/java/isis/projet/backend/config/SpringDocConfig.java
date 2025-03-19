package isis.projet.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springdoc.core.models.GroupedOpenApi;

@Configuration
public class SpringDocConfig {

    @Bean
    @Primary
    public GroupedOpenApi mainApi() {
        return GroupedOpenApi.builder()
                .group("api")
                .pathsToMatch("/api/**")
                .packagesToScan("isis.projet.backend.controller")
                .build();
    }
}