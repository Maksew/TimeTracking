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
        // Log entry into the configuration method
        logger.info("Starting CORS configuration...");
        System.out.println("Starting CORS configuration...");

        // Define allowed origins
        String[] allowedOrigins = {
                "https://time-tracking.koyeb.app",
                "http://localhost:5173"
        };

        // Log the allowed origins
        logger.info("Allowed Origins: " + Arrays.toString(allowedOrigins));
        System.out.println("Allowed Origins: " + Arrays.toString(allowedOrigins));

        // Apply CORS configuration to all endpoints
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Authorization", "Content-Type")
                .allowCredentials(true);

        // Log configuration completion
        logger.info("CORS mapping configured for all endpoints (/**)");
        System.out.println("CORS mapping configured for all endpoints (/**)");
    }
}
