package isis.projet.backend.controller;

import isis.projet.backend.entity.*;
import isis.projet.backend.repository.TimeSheetTaskRepository;
import isis.projet.backend.repository.UserGroupRepository;
import isis.projet.backend.security.jwt.JwtUserDetails;
import isis.projet.backend.service.TimeSheetService;
import isis.projet.backend.service.UserGroupService;
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

    @Autowired
    private TimeSheetTaskRepository timeSheetTaskRepository;

    @Autowired
    private UserGroupService userGroupService;

    /**
     * Vérifie si l'utilisateur a le droit de modifier une feuille de temps
     * @param timeSheetId ID de la feuille de temps
     * @param authentication Informations d'authentification de l'utilisateur
     * @return true si l'utilisateur peut modifier la feuille, false sinon
     */
    private boolean canUserEditTimeSheet(Integer timeSheetId, Authentication authentication) {
        JwtUserDetails userDetails = (JwtUserDetails) authentication.getPrincipal();
        Integer userId = userDetails.getId();

        // Récupérer la feuille de temps
        Optional<TimeSheet> timeSheetOpt = timeSheetService.getTimeSheetById(timeSheetId);
        if (timeSheetOpt.isEmpty()) {
            return false;
        }

        TimeSheet timeSheet = timeSheetOpt.get();

        // Cas 1: L'utilisateur est le propriétaire de la feuille
        if (timeSheet.getUser() != null && timeSheet.getUser().getId().equals(userId)) {
            return true;
        }

        // Cas 2: La feuille est partagée avec un groupe dont l'utilisateur est propriétaire
        if (timeSheet.getSharedWithGroups() != null && !timeSheet.getSharedWithGroups().isEmpty()) {
            for (TimeSheetShareGroup shareGroup : timeSheet.getSharedWithGroups()) {
                Integer groupId = shareGroup.getGroupId();

                // Utiliser le service pour vérifier si l'utilisateur est propriétaire du groupe
                if (userGroupService.isGroupOwner(userId, groupId)) {
                    return true;
                }
            }
        }

        // Aucune condition n'est remplie, l'utilisateur ne peut pas modifier
        return false;
    }

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

            // Vérifier le partage de groupe si spécifié dans la requête
            if (timeSheet.getSharedWithGroups() != null && !timeSheet.getSharedWithGroups().isEmpty()) {
                for (TimeSheetShareGroup shareGroup : timeSheet.getSharedWithGroups()) {
                    // Vérifier si l'utilisateur est propriétaire du groupe
                    if (!userGroupService.isGroupOwner(userDetails.getId(), shareGroup.getGroupId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body("Vous devez être propriétaire du groupe pour y partager une feuille de temps");
                    }
                }
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
     * @param authentication Informations d'authentification
     * @return Feuille de temps mise à jour
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTimeSheet(@PathVariable Integer id, @RequestBody TimeSheet timeSheet, Authentication authentication) {
        try {
            if (!id.equals(timeSheet.getId())) {
                return ResponseEntity.badRequest().body("ID de la feuille de temps incohérent");
            }

            // Vérifier les permissions
            if (!canUserEditTimeSheet(id, authentication)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Vous n'avez pas la permission de modifier cette feuille de temps");
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
     * @param authentication Informations d'authentification
     * @return Statut de l'opération
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTimeSheet(@PathVariable Integer id, Authentication authentication) {
        try {
            // Vérifier les permissions
            if (!canUserEditTimeSheet(id, authentication)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Vous n'avez pas la permission de supprimer cette feuille de temps");
            }

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
     * @param authentication Informations d'authentification
     * @return Tâche ajoutée
     */
    @PostMapping("/{timeSheetId}/tasks")
    public ResponseEntity<?> addTaskToTimeSheet(
            @PathVariable Integer timeSheetId,
            @RequestBody Map<String, Object> taskData,
            Authentication authentication) {
        try {
            // Vérifier les permissions
            if (!canUserEditTimeSheet(timeSheetId, authentication)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Vous n'avez pas la permission de modifier cette feuille de temps");
            }

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
     * @param authentication Informations d'authentification
     * @return Statut de l'opération
     */
    @PostMapping("/{timeSheetId}/share/user/{userId}")
    public ResponseEntity<?> shareTimeSheetWithUser(
            @PathVariable Integer timeSheetId,
            @PathVariable Integer userId,
            @RequestParam String accessLevel,
            Authentication authentication) {
        try {
            // Vérifier les permissions
            if (!canUserEditTimeSheet(timeSheetId, authentication)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Vous n'avez pas la permission de partager cette feuille de temps");
            }

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
     * @param authentication Informations d'authentification
     * @return Statut de l'opération
     */
    // Dans TimeSheetController.java
    @PostMapping("/{timeSheetId}/share/group/{groupId}")
    public ResponseEntity<?> shareTimeSheetWithGroup(
            @PathVariable Integer timeSheetId,
            @PathVariable Integer groupId,
            @RequestParam String accessLevel,
            Authentication authentication) {
        try {
            JwtUserDetails userDetails = (JwtUserDetails) authentication.getPrincipal();
            Integer userId = userDetails.getId();

            // Récupérer la feuille de temps
            Optional<TimeSheet> timeSheetOpt = timeSheetService.getTimeSheetById(timeSheetId);
            if (timeSheetOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            TimeSheet timeSheet = timeSheetOpt.get();

            // Vérifier si l'utilisateur est le propriétaire de la feuille de temps
            boolean isOwner = timeSheet.getUser() != null && timeSheet.getUser().getId().equals(userId);

            // Vérifier si l'utilisateur est propriétaire du groupe
            boolean isGroupOwner = userGroupService.isGroupOwner(userId, groupId);

            // Autoriser le partage uniquement si l'utilisateur est le propriétaire de la feuille
            // ET qu'il est aussi propriétaire du groupe
            if (isOwner && isGroupOwner) {
                timeSheetService.shareTimeSheetWithGroup(timeSheetId, groupId, accessLevel);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Vous n'avez pas la permission de partager cette feuille de temps avec ce groupe. Seul le propriétaire du groupe peut partager des feuilles avec le groupe.");
            }
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

    /**
     * Met à jour la durée d'une tâche dans une feuille de temps
     * @param timeSheetId ID de la feuille de temps
     * @param taskId ID de la tâche
     * @param taskData Données de la tâche à mettre à jour
     * @param authentication Informations d'authentification
     * @return Statut de l'opération
     */
    @PutMapping("/{timeSheetId}/tasks/{taskId}")
    public ResponseEntity<?> updateTaskDuration(
            @PathVariable Integer timeSheetId,
            @PathVariable Integer taskId,
            @RequestBody Map<String, Integer> taskData,
            Authentication authentication) {
        try {
            // Vérifier les permissions
            if (!canUserEditTimeSheet(timeSheetId, authentication)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Vous n'avez pas la permission de modifier cette feuille de temps");
            }

            Integer durationInSeconds = taskData.get("duration");
            if (durationInSeconds == null) {
                return ResponseEntity.badRequest().body("La durée est requise");
            }

            TimeSheetTask updatedTask = timeSheetService.updateTaskDuration(timeSheetId, taskId, durationInSeconds);

            return ResponseEntity.ok(updatedTask);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Met à jour l'état "complété" d'une tâche dans une feuille de temps
     * @param timeSheetId ID de la feuille de temps
     * @param taskId ID de la tâche
     * @param completionData Données avec l'état "complété"
     * @param authentication Informations d'authentification
     * @return Statut de l'opération
     */
    @PutMapping("/{timeSheetId}/tasks/{taskId}/complete")
    public ResponseEntity<?> updateTaskCompletionState(
            @PathVariable Integer timeSheetId,
            @PathVariable Integer taskId,
            @RequestBody Map<String, Boolean> completionData,
            Authentication authentication) {
        try {
            // Vérifier les permissions
            if (!canUserEditTimeSheet(timeSheetId, authentication)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Vous n'avez pas la permission de modifier cette feuille de temps");
            }

            Boolean isCompleted = completionData.get("completed");
            if (isCompleted == null) {
                return ResponseEntity.badRequest().body("L'état de complétion est requis");
            }

            TimeSheetTask updatedTask = timeSheetService.updateTaskCompletionState(timeSheetId, taskId, isCompleted);

            return ResponseEntity.ok(updatedTask);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Supprime une tâche d'une feuille de temps
     * @param timeSheetId ID de la feuille de temps
     * @param taskId ID de la tâche
     * @param authentication Informations d'authentification
     * @return Statut de l'opération
     */
    @DeleteMapping("/{timeSheetId}/tasks/{taskId}")
    public ResponseEntity<?> removeTaskFromTimeSheet(
            @PathVariable Integer timeSheetId,
            @PathVariable Integer taskId,
            Authentication authentication) {
        try {
            // Vérifier les permissions
            if (!canUserEditTimeSheet(timeSheetId, authentication)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Vous n'avez pas la permission de modifier cette feuille de temps");
            }

            // Appeler le service pour supprimer la tâche de la feuille de temps
            timeSheetService.removeTaskFromTimeSheet(timeSheetId, taskId);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}