package isis.projet.backend.repository;

import isis.projet.backend.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Integer> {
    Optional<Group> findByInvitCode(String invitCode);

    @Query(value = "SELECT g FROM Group g JOIN g.userGroups ug WHERE ug.user.id = ?1")
    List<Group> findByUserId(Integer userId);
}