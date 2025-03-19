package isis.projet.backend.security.jwt;

import isis.projet.backend.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private JwtConfig jwtConfig;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Créer une instance réelle de JwtConfig
        jwtConfig = new JwtConfig();
        // Utiliser une clé suffisamment longue
        jwtConfig.setSecret("verySecureKeyForTestingPurposesOnlyDoNotUseInProduction12345678901234567890");
        jwtConfig.setExpirationMs(3600000L); // 1 heure
        jwtConfig.setTokenPrefix("Bearer ");
        jwtConfig.setHeaderName("Authorization");

        // Créer JwtTokenProvider et injecter la config manuellement
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtConfig", jwtConfig);

        // Créer l'utilisateur de test
        testUser = new User();
        testUser.setId(1);
        testUser.setEmail("test@example.com");
        testUser.setPseudo("testuser");
        testUser.setRole("USER");
    }

    @Test
    void generateTokenShouldCreateValidToken() {
        // Générer un token
        String token = jwtTokenProvider.generateToken(testUser);

        // Vérifier que le token n'est pas null
        assertNotNull(token);

        // Vérifier que le token contient les informations utilisateur
        assertEquals("test@example.com", jwtTokenProvider.getUsernameFromToken(token));
        assertEquals(1, jwtTokenProvider.getUserIdFromToken(token));
        assertEquals("USER", jwtTokenProvider.getRoleFromToken(token));
    }

    @Test
    void getExpirationDateShouldReturnFutureDate() {
        // Générer un token
        String token = jwtTokenProvider.generateToken(testUser);

        // Vérifier que la date d'expiration est dans le futur
        assertTrue(jwtTokenProvider.getExpirationDateFromToken(token).after(new Date()));
    }

    @Test
    void validateTokenShouldReturnTrueForValidToken() {
        // Générer un token
        String token = jwtTokenProvider.generateToken(testUser);

        // Valider le token
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void validateTokenShouldReturnFalseForInvalidToken() {
        // Token invalide (format incorrect)
        String invalidToken = "invalid.token.format";

        // La validation doit échouer
        assertFalse(jwtTokenProvider.validateToken(invalidToken));
    }

    @Test
    void validateTokenShouldReturnFalseForExpiredToken() {
        // Configurer une expiration négative (token déjà expiré)
        ReflectionTestUtils.setField(jwtConfig, "expirationMs", -10000L);

        // Générer un token (qui sera déjà expiré)
        String expiredToken = jwtTokenProvider.generateToken(testUser);

        // La validation doit échouer pour un token expiré
        assertFalse(jwtTokenProvider.validateToken(expiredToken));
    }
}