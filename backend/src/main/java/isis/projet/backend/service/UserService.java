package isis.projet.backend.service;

import isis.projet.backend.entity.User;
import isis.projet.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Integer id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User updateUser(User user) {
        // Retrieve the existing user from the database
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // Update allowed fields
        existingUser.setPseudo(user.getPseudo());
        existingUser.setEmail(user.getEmail());
        // Check if password is provided and if it's different (i.e. not already encoded)
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            // It's a good idea to check if the password is already encoded, but usually you expect plain text here
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        // Update role or other fields if necessary
        existingUser.setRole(user.getRole());

        return userRepository.save(existingUser);
    }

    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }
}