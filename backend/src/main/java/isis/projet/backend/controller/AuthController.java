package isis.projet.backend.controller;

import isis.projet.backend.dto.AuthDTO;
import isis.projet.backend.entity.User;
import isis.projet.backend.service.AuthService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private ModelMapper modelMapper;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthDTO.RegisterRequest registerRequest) {
        try {
            User user = new User();
            user.setPseudo(registerRequest.getPseudo());
            user.setEmail(registerRequest.getEmail());
            user.setPassword(registerRequest.getPassword());

            User registeredUser = authService.register(user);

            AuthDTO.AuthResponse response = modelMapper.map(registeredUser, AuthDTO.AuthResponse.class);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthDTO.LoginRequest loginRequest) {
        Optional<User> userOpt = authService.authenticate(loginRequest.getEmail(), loginRequest.getPassword());

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            AuthDTO.AuthResponse response = modelMapper.map(user, AuthDTO.AuthResponse.class);
            // here add jwt later if needed
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body("Email ou mot de passe incorrect");
        }
    }
}