package isis.projet.backend.security.jwt;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Point d'entrée pour gérer les erreurs d'authentification
 * et retourner un statut 401 Unauthorized au lieu de 403 Forbidden
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        // Définir le code de statut HTTP à 401 (Unauthorized)
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        // Écrire le message d'erreur
        response.getWriter().write("{\"error\":\"Non authentifié\",\"message\":\""
                + authException.getMessage() + "\"}");
    }
}