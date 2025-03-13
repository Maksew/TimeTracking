package isis.projet.backend.controller;

import isis.projet.backend.entity.Group;
import isis.projet.backend.entity.User;
import isis.projet.backend.service.GroupService;
import isis.projet.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    @Autowired
    private GroupService groupService;

    @Autowired
    private UserService userService;

    @GetMapping
    public List<Group> getAllGroups() {
        return groupService.getAllGroups();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Group> getGroupById(@PathVariable Integer id) {
        Optional<Group> group = groupService.getGroupById(id);
        return group.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public List<Group> getGroupsByUserId(@PathVariable Integer userId) {
        return groupService.getGroupsByUserId(userId);
    }

    @PostMapping
    public ResponseEntity<?> createGroup(@RequestBody Group group, @RequestParam Integer ownerId) {
        try {
            Optional<User> ownerOpt = userService.getUserById(ownerId);
            if (ownerOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Propriétaire introuvable");
            }

            Group createdGroup = groupService.createGroup(group, ownerOpt.get());
            return ResponseEntity.ok(createdGroup);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateGroup(@PathVariable Integer id, @RequestBody Group group) {
        try {
            if (!id.equals(group.getId())) {
                return ResponseEntity.badRequest().body("ID du groupe incohérent");
            }

            Group updatedGroup = groupService.updateGroup(group);
            return ResponseEntity.ok(updatedGroup);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGroup(@PathVariable Integer id) {
        try {
            groupService.deleteGroup(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/join")
    public ResponseEntity<?> joinGroup(@RequestParam String invitCode, @RequestParam Integer userId) {
        try {
            Optional<User> userOpt = userService.getUserById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Utilisateur introuvable");
            }

            Group group = groupService.joinGroup(invitCode, userOpt.get());
            return ResponseEntity.ok(group);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}