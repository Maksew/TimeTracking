package isis.projet.backend.service;

import isis.projet.backend.entity.UserGroup;
import isis.projet.backend.entity.UserGroupId;
import isis.projet.backend.repository.UserGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserGroupService {

    @Autowired
    private UserGroupRepository userGroupRepository;

    /**
     * Vérifie si un utilisateur a un rôle spécifique dans un groupe
     * @param userId ID de l'utilisateur
     * @param groupId ID du groupe
     * @param role Rôle à vérifier (ex: "OWNER", "MEMBER")
     * @return true si l'utilisateur a le rôle spécifié, false sinon
     */
    public boolean hasUserRole(Integer userId, Integer groupId, String role) {
        // Utiliser les méthodes existantes
        Optional<UserGroup> userGroup = userGroupRepository.findById(new UserGroupId(userId, groupId));
        return userGroup.isPresent() && role.equals(userGroup.get().getRole());
    }

    /**
     * Vérifie si un utilisateur est propriétaire d'un groupe
     * @param userId ID de l'utilisateur
     * @param groupId ID du groupe
     * @return true si l'utilisateur est propriétaire, false sinon
     */
    public boolean isGroupOwner(Integer userId, Integer groupId) {
        return hasUserRole(userId, groupId, "OWNER");
    }

    /**
     * Vérifie si un utilisateur est membre d'un groupe (quel que soit son rôle)
     * @param userId ID de l'utilisateur
     * @param groupId ID du groupe
     * @return true si l'utilisateur est membre du groupe, false sinon
     */
    public boolean isGroupMember(Integer userId, Integer groupId) {
        Optional<UserGroup> userGroup = userGroupRepository.findById(new UserGroupId(userId, groupId));
        return userGroup.isPresent();
    }

    /**
     * Récupère le rôle d'un utilisateur dans un groupe
     * @param userId ID de l'utilisateur
     * @param groupId ID du groupe
     * @return Un Optional contenant le rôle si trouvé, vide sinon
     */
    public Optional<String> getUserRole(Integer userId, Integer groupId) {
        Optional<UserGroup> userGroup = userGroupRepository.findById(new UserGroupId(userId, groupId));
        return userGroup.map(UserGroup::getRole);
    }
}