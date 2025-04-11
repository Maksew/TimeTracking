package isis.projet.backend.repository;

import isis.projet.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByPseudo(String pseudo);
    Optional<User> findByEmail(String email);
    boolean existsByPseudo(String pseudo);
    boolean existsByEmail(String email);
    List<User> findAllByIdIn(Collection<Integer> ids);
}