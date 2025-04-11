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

import java.util.*;
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
        if (group == null) return;

        try {
            // Utiliser la requête JPQL personnalisée pour charger le groupe avec tous ses membres en une seule opération
            Optional<Group> fullGroupOpt = groupRepository.findByIdWithAllMembers(group.getId());

            if (fullGroupOpt.isEmpty()) return;

            Group fullGroup = fullGroupOpt.get();

            // Remplacer la liste userGroups du groupe par celle du groupe complet
            group.setUserGroups(fullGroup.getUserGroups());

            // Vérifier si le nombre de membres correspond à celui attendu
            long expectedCount = groupRepository.countMembersByGroupId(group.getId());
            int actualCount = group.getUserGroups() != null ? group.getUserGroups().size() : 0;

            if (expectedCount != actualCount) {
                System.out.println("Attention: Décalage dans le nombre de membres pour le groupe " + group.getName() +
                        ". Attendu: " + expectedCount + ", Actuel: " + actualCount);
            }

            // Si userGroups est vide ou null, on a un problème
            if (group.getUserGroups() == null || group.getUserGroups().isEmpty()) {
                System.out.println("Aucun membre trouvé pour le groupe " + group.getName() +
                        ". Tentative de récupération alternative...");

                // Récupération alternative des UserGroup
                List<UserGroup> allUserGroups = userGroupRepository.findByGroupId(group.getId());
                group.setUserGroups(allUserGroups);
            }

            // Maintenant qu'on a les UserGroup, charger les détails des utilisateurs si nécessaire
            if (group.getUserGroups() != null) {
                for (UserGroup userGroup : group.getUserGroups()) {
                    if (userGroup.getUser() == null) {
                        Optional<User> userOpt = userRepository.findById(userGroup.getUserId());
                        userOpt.ifPresent(userGroup::setUser);
                    }
                }
            }

            System.out.println("Groupe " + group.getName() + " chargé avec " +
                    (group.getUserGroups() != null ? group.getUserGroups().size() : 0) + " membres");

        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des membres du groupe: " + e.getMessage());
            e.printStackTrace();
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