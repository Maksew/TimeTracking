package isis.projet.backend.controller;

import isis.projet.backend.entity.TimeSheet;
import isis.projet.backend.entity.User;
import isis.projet.backend.security.jwt.JwtUserDetails;
import isis.projet.backend.service.TimeSheetService;
import isis.projet.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/timesheets")
public class TimeSheetController {

    @Autowired
    private TimeSheetService timeSheetService;

    @Autowired
    private UserService userService;

    @GetMapping
    public List<TimeSheet> getCurrentUserTimeSheets(Authentication authentication) {
        JwtUserDetails userDetails = (JwtUserDetails) authentication.getPrincipal();
        return timeSheetService.getAllTimeSheetsByUserId(userDetails.getId());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TimeSheet> getTimeSheetById(@PathVariable Integer id) {
        Optional<TimeSheet> timeSheet = timeSheetService.getTimeSheetById(id);
        return timeSheet.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createTimeSheet(@RequestBody TimeSheet timeSheet, Authentication authentication) {
        try {
            JwtUserDetails userDetails = (JwtUserDetails) authentication.getPrincipal();
            Optional<User> userOpt = userService.getUserById(userDetails.getId());

            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Utilisateur introuvable");
            }

            TimeSheet createdTimeSheet = timeSheetService.createTimeSheet(timeSheet, userOpt.get());
            return ResponseEntity.ok(createdTimeSheet);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTimeSheet(@PathVariable Integer id, @RequestBody TimeSheet timeSheet) {
        try {
            if (!id.equals(timeSheet.getId())) {
                return ResponseEntity.badRequest().body("ID de la feuille de temps incohérent");
            }

            TimeSheet updatedTimeSheet = timeSheetService.updateTimeSheet(timeSheet);
            return ResponseEntity.ok(updatedTimeSheet);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTimeSheet(@PathVariable Integer id) {
        try {
            timeSheetService.deleteTimeSheet(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}