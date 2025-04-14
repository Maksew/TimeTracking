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
        // Disable CSRF protection for simplicity on endpoints that do not require it.
        // This is not a recommended default for production, so adjust accordingly.
        http.csrf().disable();

        // Permit requests to the registration endpoint (and possibly others)
        http.authorizeRequests()
                .antMatchers("/api/auth/register").permitAll() // allow registration without authentication
                .anyRequest().authenticated();

        // Log security configuration details (this is rudimentary logging)
        System.out.println("Security configuration: CSRF disabled, /api/auth/register permitted for all.");
    }
}
