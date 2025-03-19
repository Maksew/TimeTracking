package isis.projet.backend.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import isis.projet.backend.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Composant responsable de la génération et validation des tokens JWT
 */
@Component
public class JwtTokenProvider {

    @Autowired
    private JwtConfig jwtConfig;

    /**
     * Génère un token JWT pour un utilisateur
     * @param user l'utilisateur
     * @return le token JWT généré
     */
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("role", user.getRole());
        claims.put("pseudo", user.getPseudo());

        return createToken(claims, user.getEmail());
    }

    /**
     * Crée un token JWT avec les claims et le sujet fournis
     * @param claims les claims à inclure dans le token
     * @param subject le sujet du token (généralement l'email/username)
     * @return le token JWT généré
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtConfig.getExpirationMs());

        // Utiliser HS256 au lieu de HS512 - nécessite une clé moins longue
        SecretKey key = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Récupère le SecretKey pour valider/signer les tokens
     * @return la clé secrète
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Extrait toutes les claims d'un token
     * @param token le token JWT
     * @return les claims contenues dans le token
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Extrait une claim spécifique d'un token
     * @param token le token JWT
     * @param claimsResolver fonction pour extraire la claim désirée
     * @return la claim extraite
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Valide un token JWT
     * @param token le token à valider
     * @return true si le token est valide
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Vérifie si un token est expiré
     * @param token le token JWT
     * @return true si le token est expiré
     */
    private boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * Extrait l'username (email) du token JWT
     * @param token le token JWT
     * @return l'email extrait
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * Extrait la date d'expiration du token JWT
     * @param token le token JWT
     * @return la date d'expiration
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * Extrait l'ID utilisateur du token JWT
     * @param token le token JWT
     * @return l'ID utilisateur
     */
    public Integer getUserIdFromToken(String token) {
        final Claims claims = getAllClaimsFromToken(token);
        return ((Number) claims.get("id")).intValue();
    }

    /**
     * Extrait le rôle utilisateur du token JWT
     * @param token le token JWT
     * @return le rôle utilisateur
     */
    public String getRoleFromToken(String token) {
        final Claims claims = getAllClaimsFromToken(token);
        return (String) claims.get("role");
    }
}