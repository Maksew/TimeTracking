package isis.projet.backend.service;

import isis.projet.backend.entity.Group;
import isis.projet.backend.entity.User;
import isis.projet.backend.entity.UserGroup;
import isis.projet.backend.repository.GroupRepository;
import isis.projet.backend.repository.UserGroupRepository;
import isis.projet.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GroupService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserGroupRepository userGroupRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }

    public Optional<Group> getGroupById(Integer id) {
        return groupRepository.findById(id);
    }

    public List<Group> getGroupsByUserId(Integer userId) {
        return groupRepository.findByUserId(userId);
    }

    /**
     * Charge les détails complets des membres d'un groupe
     * Cette méthode s'assure que chaque UserGroup a un objet User complet avec toutes ses propriétés
     * @param group Le groupe dont on veut charger les membres
     */
    public void loadGroupMembersDetails(Group group) {
        if (group == null || group.getUserGroups() == null) return;

        // Récupérer tous les IDs d'utilisateurs dans le groupe en une seule liste
        List<Integer> userIds = group.getUserGroups().stream()
                .map(UserGroup::getUserId)
                .collect(Collectors.toList());

        // Récupérer tous les utilisateurs en une seule requête
        List<User> users = userRepository.findAllByIdIn(userIds);

        // Créer une map pour un accès rapide
        Map<Integer, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // Associer les objets User complets aux UserGroup
        for (UserGroup userGroup : group.getUserGroups()) {
            User user = userMap.get(userGroup.getUserId());
            if (user != null) {
                userGroup.setUser(user);
            }
        }

        // S'assurer que la liste UserGroups est initialisée et non vide
        if (group.getUserGroups().isEmpty()) {
            // Récupérer directement de la base de données si la liste est vide
            List<UserGroup> userGroups = userGroupRepository.findByGroupId(group.getId());
            for (UserGroup ug : userGroups) {
                User user = userMap.get(ug.getUserId());
                if (user != null) {
                    ug.setUser(user);
                }
            }
            group.setUserGroups(userGroups);
        }
    }

    @Transactional
    public Group createGroup(Group group, User owner) {
        // Générer un code d'invitation unique
        group.setInvitCode(generateInvitationCode());

        Group savedGroup = groupRepository.save(group);

        UserGroup userGroup = new UserGroup();
        userGroup.setUserId(owner.getId());
        userGroup.setGroupId(savedGroup.getId());
        userGroup.setRole("OWNER");
        userGroup.setUser(owner);
        userGroup.setGroup(savedGroup);

        userGroupRepository.save(userGroup);

        return savedGroup;
    }

    public Group updateGroup(Group group) {
        if (group.getId() == null || !groupRepository.existsById(group.getId())) {
            throw new RuntimeException("Groupe introuvable");
        }

        return groupRepository.save(group);
    }

    @Transactional
    public void deleteGroup(Integer groupId) {
        // Retrieve associated UserGroup entries
        List<UserGroup> associations = userGroupRepository.findByGroupId(groupId);
        // Delete associations explicitly
        userGroupRepository.deleteAll(associations);
        // Now delete the group
        groupRepository.deleteById(groupId);
    }

    @Transactional
    public Group joinGroup(String invitCode, User user) {
        Optional<Group> groupOpt = groupRepository.findByInvitCode(invitCode);

        if (groupOpt.isEmpty()) {
            throw new RuntimeException("Code d'invitation invalide");
        }

        Group group = groupOpt.get();

        // Vérifier si l'utilisateur est déjà membre
        boolean alreadyMember = userGroupRepository.findByGroupId(group.getId())
                .stream()
                .anyMatch(ug -> ug.getUserId().equals(user.getId()));

        if (alreadyMember) {
            throw new RuntimeException("Vous êtes déjà membre de ce groupe");
        }

        UserGroup userGroup = new UserGroup();
        userGroup.setUserId(user.getId());
        userGroup.setGroupId(group.getId());
        userGroup.setRole("MEMBER");
        userGroup.setUser(user);
        userGroup.setGroup(group);

        userGroupRepository.save(userGroup);

        return group;
    }

    private String generateInvitationCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}