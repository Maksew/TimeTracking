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

    @Query("SELECT DISTINCT g FROM Group g JOIN g.userGroups ug WHERE ug.user.id = :userId")
    List<Group> findByUserId(@Param("userId") Integer userId);

    @Query("SELECT COUNT(ug) FROM UserGroup ug WHERE ug.groupId = :groupId")
    long countMembersByGroupId(@Param("groupId") Integer groupId);

    @Query("SELECT g FROM Group g LEFT JOIN FETCH g.userGroups WHERE g.id = :groupId")
    Optional<Group> findByIdWithAllMembers(@Param("groupId") Integer groupId);
}