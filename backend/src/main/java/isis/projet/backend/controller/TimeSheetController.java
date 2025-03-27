package isis.projet.backend.controller;

import isis.projet.backend.entity.TimeSheet;
import isis.projet.backend.entity.TimeSheetTask;
import isis.projet.backend.entity.User;
import isis.projet.backend.security.jwt.JwtUserDetails;
import isis.projet.backend.service.TimeSheetService;
import isis.projet.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/timesheets")
public class TimeSheetController {

    @Autowired
    private TimeSheetService timeSheetService;

    @Autowired
    private UserService userService;

    /**
     * Récupère toutes les feuilles de temps de l'utilisateur connecté
     * @param authentication Informations d'authentification
     * @return Liste des feuilles de temps
     */
    @GetMapping
    public List<TimeSheet> getCurrentUserTimeSheets(
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        JwtUserDetails userDetails = (JwtUserDetails) authentication.getPrincipal();
        List<TimeSheet> timeSheets = timeSheetService.getAllTimeSheetsByUserId(userDetails.getId());

        // Filtrer les feuilles de temps par date si les dates sont spécifiées
        if (startDate != null && endDate != null) {
            timeSheets = timeSheets.stream()
                    .filter(ts -> !ts.getEntryDate().isBefore(startDate) && !ts.getEntryDate().isAfter(endDate))
                    .collect(Collectors.toList());
        } else if (startDate != null) {
            timeSheets = timeSheets.stream()
                    .filter(ts -> !ts.getEntryDate().isBefore(startDate))
                    .collect(Collectors.toList());
        } else if (endDate != null) {
            timeSheets = timeSheets.stream()
                    .filter(ts -> !ts.getEntryDate().isAfter(endDate))
                    .collect(Collectors.toList());
        }

        return timeSheets;
    }

    /**
     * Récupère une feuille de temps par son ID
     * @param id ID de la feuille de temps
     * @return Feuille de temps
     */
    @GetMapping("/{id}")
    public ResponseEntity<TimeSheet> getTimeSheetById(@PathVariable Integer id) {
        Optional<TimeSheet> timeSheet = timeSheetService.getTimeSheetById(id);
        return timeSheet.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Récupère les feuilles de temps par date
     * @param date Date de la feuille de temps
     * @return Liste des feuilles de temps
     */
    @GetMapping("/byDate")
    public List<TimeSheet> getTimeSheetsByDate(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return timeSheetService.getTimeSheetsByDate(date);
    }

    /**
     * Récupère les feuilles de temps partagées avec un utilisateur
     * @param authentication Informations d'authentification
     * @return Liste des feuilles de temps partagées
     */
    @GetMapping("/shared")
    public List<TimeSheet> getSharedTimeSheets(Authentication authentication) {
        JwtUserDetails userDetails = (JwtUserDetails) authentication.getPrincipal();
        return timeSheetService.getTimeSheetSharedWithUser(userDetails.getId());
    }

    /**
     * Récupère les feuilles de temps partagées avec un groupe
     * @param groupId ID du groupe
     * @return Liste des feuilles de temps partagées
     */
    @GetMapping("/group/{groupId}")
    public List<TimeSheet> getGroupTimeSheets(@PathVariable Integer groupId) {
        return timeSheetService.getTimeSheetSharedWithGroup(groupId);
    }

    /**
     * Crée une nouvelle feuille de temps
     * @param timeSheet Feuille de temps à créer
     * @param authentication Informations d'authentification
     * @return Feuille de temps créée
     */
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

    /**
     * Met à jour une feuille de temps
     * @param id ID de la feuille de temps
     * @param timeSheet Feuille de temps à mettre à jour
     * @return Feuille de temps mise à jour
     */
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

    /**
     * Supprime une feuille de temps
     * @param id ID de la feuille de temps
     * @return Statut de l'opération
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTimeSheet(@PathVariable Integer id) {
        try {
            timeSheetService.deleteTimeSheet(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Ajoute une tâche à une feuille de temps
     * @param timeSheetId ID de la feuille de temps
     * @param taskData Données de la tâche
     * @return Tâche ajoutée
     */
    @PostMapping("/{timeSheetId}/tasks")
    public ResponseEntity<?> addTaskToTimeSheet(
            @PathVariable Integer timeSheetId,
            @RequestBody Map<String, Object> taskData) {
        try {
            Integer taskId = (Integer) taskData.get("taskId");
            Integer duration = (Integer) taskData.get("duration");

            TimeSheetTask timeSheetTask = timeSheetService.addTaskToTimeSheet(timeSheetId, taskId, duration);
            return ResponseEntity.ok(timeSheetTask);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Partage une feuille de temps avec un utilisateur
     * @param timeSheetId ID de la feuille de temps
     * @param userId ID de l'utilisateur
     * @param accessLevel Niveau d'accès
     * @return Statut de l'opération
     */
    @PostMapping("/{timeSheetId}/share/user/{userId}")
    public ResponseEntity<?> shareTimeSheetWithUser(
            @PathVariable Integer timeSheetId,
            @PathVariable Integer userId,
            @RequestParam String accessLevel) {
        try {
            timeSheetService.shareTimeSheetWithUser(timeSheetId, userId, accessLevel);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Partage une feuille de temps avec un groupe
     * @param timeSheetId ID de la feuille de temps
     * @param groupId ID du groupe
     * @param accessLevel Niveau d'accès
     * @return Statut de l'opération
     */
    @PostMapping("/{timeSheetId}/share/group/{groupId}")
    public ResponseEntity<?> shareTimeSheetWithGroup(
            @PathVariable Integer timeSheetId,
            @PathVariable Integer groupId,
            @RequestParam String accessLevel) {
        try {
            timeSheetService.shareTimeSheetWithGroup(timeSheetId, groupId, accessLevel);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Exporte les feuilles de temps en CSV
     * @param authentication Informations d'authentification
     * @param startDate Date de début optionnelle
     * @param endDate Date de fin optionnelle
     * @return Fichier CSV
     */
    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportTimeSheetsToCsv(
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        JwtUserDetails userDetails = (JwtUserDetails) authentication.getPrincipal();

        byte[] csvData = timeSheetService.exportTimeSheetsToCsv(userDetails.getId(), startDate, endDate);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("filename", "timesheets.csv");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<>(csvData, headers, HttpStatus.OK);
    }
}