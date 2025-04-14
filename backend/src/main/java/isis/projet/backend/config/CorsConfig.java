package isis.projet.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(CorsConfig.class);

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] allowedOrigins = {
                "https://time-tracking.koyeb.app",
                "http://localhost:5173"
        };

        logger.info("Starting CORS configuration with allowed origins: " + Arrays.toString(allowedOrigins));
        System.out.println("Starting CORS configuration with allowed origins: " + Arrays.toString(allowedOrigins));

        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Authorization", "Content-Type")
                .allowCredentials(true);

        logger.info("CORS mapping configured for all endpoints (/**)");
        System.out.println("CORS mapping configured for all endpoints (/**)");
    }
}
