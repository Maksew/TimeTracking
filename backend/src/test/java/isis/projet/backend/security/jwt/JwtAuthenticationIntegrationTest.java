package isis.projet.backend.security.jwt;

import isis.projet.backend.dto.AuthDTO;
import isis.projet.backend.entity.User;
import isis.projet.backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class JwtAuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password123";

    @BeforeEach
    void setUp() {
        // Nettoyer la base de données de test
        userRepository.deleteAll();

        // Créer un utilisateur de test
        User user = new User();
        user.setEmail(TEST_EMAIL);
        user.setPseudo("testuser");
        user.setPassword(passwordEncoder.encode(TEST_PASSWORD));
        user.setRole("USER");

        userRepository.save(user);
    }

    @Test
    void shouldAllowAccessToPublicEndpoints() throws Exception {
        mockMvc.perform(get("/api/auth/login"))
                .andExpect(status().isMethodNotAllowed()); // POST est la méthode attendue
    }

    @Test
    void shouldBlockAccessToProtectedEndpointsWithoutToken() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized()); // 401 au lieu de 403
    }

    @Test
    void shouldLoginSuccessfullyAndGetToken() throws Exception {
        // Préparer la requête de login
        AuthDTO.LoginRequest loginRequest = new AuthDTO.LoginRequest();
        loginRequest.setEmail(TEST_EMAIL);
        loginRequest.setPassword(TEST_PASSWORD);

        // Exécuter la requête
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.token").exists())
                .andReturn();

        // Extraire le token de la réponse
        AuthDTO.AuthResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                AuthDTO.AuthResponse.class
        );

        assertNotNull(response.getToken());
    }

    @Test
    void shouldAccessProtectedEndpointWithValidToken() throws Exception {
        // Préparer la requête de login
        AuthDTO.LoginRequest loginRequest = new AuthDTO.LoginRequest();
        loginRequest.setEmail(TEST_EMAIL);
        loginRequest.setPassword(TEST_PASSWORD);

        // Exécuter la requête de login
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        // Extraire le token de la réponse
        AuthDTO.AuthResponse response = objectMapper.readValue(
                loginResult.getResponse().getContentAsString(),
                AuthDTO.AuthResponse.class
        );

        // Utiliser le token pour accéder à un endpoint protégé
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + response.getToken()))
                .andExpect(status().isOk());
    }
}