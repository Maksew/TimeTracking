package isis.projet.backend.service;

import isis.projet.backend.entity.Group;
import isis.projet.backend.entity.User;
import isis.projet.backend.entity.UserGroup;
import isis.projet.backend.entity.UserGroupId;
import isis.projet.backend.repository.GroupRepository;
import isis.projet.backend.repository.UserGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class GroupService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserGroupRepository userGroupRepository;

    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }

    public Optional<Group> getGroupById(Integer id) {
        return groupRepository.findById(id);
    }

    public List<Group> getGroupsByUserId(Integer userId) {
        return groupRepository.findByUserId(userId);
    }

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
    public void deleteGroup(Integer groupId) {
        // Retrieve associated UserGroup entries
        List<UserGroup> associations = userGroupRepository.findByGroupId(groupId);
        // Delete associations explicitly
        userGroupRepository.deleteAll(associations);
        // Now delete the group
        groupRepository.deleteById(groupId);
    }

    public Group joinGroup(String invitCode, User user) {
        Optional<Group> groupOpt = groupRepository.findByInvitCode(invitCode);

        if (groupOpt.isEmpty()) {
            throw new RuntimeException("Code d'invitation invalide");
        }

        Group group = groupOpt.get();

        // Vérifier si l'utilisateur est déjà membre
        UserGroupId userGroupId = new UserGroupId(user.getId(), group.getId());
        if (userGroupRepository.existsById(userGroupId)) {
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