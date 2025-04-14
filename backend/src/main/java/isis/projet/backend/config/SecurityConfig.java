package isis.projet.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // Disable CSRF protection for specific endpoints to allow public access
        http.csrf()
                .ignoringAntMatchers(
                        "/api/auth/register",
                        "/api/auth/login",
                        "/api/auth/refresh"
                )
                .and()
                .authorizeRequests()
                // Permit all access to registration, login, and token refresh endpoints.
                .antMatchers(
                        "/api/auth/register",
                        "/api/auth/login",
                        "/api/auth/refresh"
                ).permitAll()
                // All other endpoints require authentication.
                .anyRequest().authenticated();

        // Optionally log security configuration details for debugging
        System.out.println("Security configuration: CSRF disabled for public endpoints and permitted for registration, login, and refresh.");
    }
}
