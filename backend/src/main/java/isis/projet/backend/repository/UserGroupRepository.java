package isis.projet.backend.repository;

import isis.projet.backend.entity.UserGroup;
import isis.projet.backend.entity.UserGroupId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserGroupRepository extends JpaRepository<UserGroup, UserGroupId> {
    List<UserGroup> findByUserId(Integer userId);
    List<UserGroup> findByGroupId(Integer groupId);
    List<UserGroup> findByUserIdAndRole(Integer userId, String role);
}