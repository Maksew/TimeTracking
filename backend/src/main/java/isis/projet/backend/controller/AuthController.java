package isis.projet.backend.controller;

import isis.projet.backend.dto.AuthDTO;
import isis.projet.backend.entity.User;
import isis.projet.backend.security.jwt.JwtTokenProvider;
import isis.projet.backend.service.AuthService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthDTO.RegisterRequest registerRequest) {
        try {
            User user = new User();
            user.setPseudo(registerRequest.getPseudo());
            user.setEmail(registerRequest.getEmail());
            user.setPassword(registerRequest.getPassword());

            User registeredUser = authService.register(user);

            // Générer un token JWT pour l'utilisateur enregistré
            String jwt = tokenProvider.generateToken(registeredUser);

            AuthDTO.AuthResponse response = modelMapper.map(registeredUser, AuthDTO.AuthResponse.class);
            response.setToken(jwt);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorResponse);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthDTO.LoginRequest loginRequest) {
        try {
            // Authentifier l'utilisateur
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            // Mettre à jour le contexte de sécurité
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Récupérer l'utilisateur
            Optional<User> userOpt = authService.authenticate(loginRequest.getEmail(), loginRequest.getPassword());

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                String jwt = tokenProvider.generateToken(user);
                AuthDTO.AuthResponse response = modelMapper.map(user, AuthDTO.AuthResponse.class);
                response.setToken(jwt);

                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body("Email ou mot de passe incorrect");
            }
        } catch (AuthenticationException e) {
            return ResponseEntity.badRequest().body("Email ou mot de passe incorrect");
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody AuthDTO.TokenRefreshRequest request) {
        String token = request.getToken();
        if (!tokenProvider.validateToken(token)) {
            return ResponseEntity.badRequest().body("Token invalide ou expiré");
        }
        String email = tokenProvider.getUsernameFromToken(token);
        Optional<User> userOpt = authService.getUserRepository().findByEmail(email);

        if (userOpt.isPresent()) {
            String newToken = tokenProvider.generateToken(userOpt.get());
            return ResponseEntity.ok(new AuthDTO.TokenRefreshResponse(newToken));
        } else {
            return ResponseEntity.badRequest().body("Utilisateur introuvable");
        }
    }
}