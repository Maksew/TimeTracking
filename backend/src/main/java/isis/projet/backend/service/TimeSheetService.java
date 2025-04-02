package isis.projet.backend.service;

import isis.projet.backend.entity.*;
import isis.projet.backend.repository.TimeSheetRepository;
import isis.projet.backend.repository.TimeSheetShareGroupRepository;
import isis.projet.backend.repository.TimeSheetShareUserRepository;
import isis.projet.backend.repository.TimeSheetTaskRepository;
import isis.projet.backend.repository.TaskRepository;
import isis.projet.backend.repository.UserRepository;
import isis.projet.backend.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TimeSheetService {

    @Autowired
    private TimeSheetRepository timeSheetRepository;

    @Autowired
    private TimeSheetTaskRepository timeSheetTaskRepository;

    @Autowired
    private TimeSheetShareUserRepository timeSheetShareUserRepository;

    @Autowired
    private TimeSheetShareGroupRepository timeSheetShareGroupRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    /**
     * Récupère toutes les feuilles de temps d'un utilisateur
     * @param userId ID de l'utilisateur
     * @return Liste des feuilles de temps
     */
    public List<TimeSheet> getAllTimeSheetsByUserId(Integer userId) {
        return timeSheetRepository.findByUserId(userId);
    }

    /**
     * Récupère une feuille de temps par son ID
     * @param id ID de la feuille de temps
     * @return Feuille de temps
     */
    public Optional<TimeSheet> getTimeSheetById(Integer id) {
        return timeSheetRepository.findById(id);
    }

    /**
     * Crée une nouvelle feuille de temps
     * @param timeSheet Feuille de temps à créer
     * @param user Utilisateur
     * @return Feuille de temps créée
     */
    public TimeSheet createTimeSheet(TimeSheet timeSheet, User user) {
        timeSheet.setUser(user);
        if (timeSheet.getEntryDate() == null) {
            timeSheet.setEntryDate(LocalDate.now());
        }
        return timeSheetRepository.save(timeSheet);
    }

    /**
     * Met à jour une feuille de temps
     * @param timeSheet Feuille de temps à mettre à jour
     * @return Feuille de temps mise à jour
     */
    public TimeSheet updateTimeSheet(TimeSheet updatedTimeSheet) {
        // Vérifier si l'ID est présent et si la feuille de temps existe dans la base de données
        if (updatedTimeSheet.getId() == null || !timeSheetRepository.existsById(updatedTimeSheet.getId())) {
            throw new RuntimeException("Feuille de temps introuvable");
        }

        // Récupérer la feuille de temps existante
        Optional<TimeSheet> optionalExisting = timeSheetRepository.findById(updatedTimeSheet.getId());
        if (!optionalExisting.isPresent()) {
            throw new RuntimeException("Feuille de temps introuvable");
        }
        TimeSheet existingTimeSheet = optionalExisting.get();

        // Mettre à jour les champs autorisés tout en préservant l'association avec l'utilisateur
        existingTimeSheet.setEntryDate(updatedTimeSheet.getEntryDate());
        existingTimeSheet.setIcon(updatedTimeSheet.getIcon());
        // Ajoutez ici d'autres mises à jour de champs si nécessaire

        // Sauvegarder et retourner la feuille de temps mise à jour
        return timeSheetRepository.save(existingTimeSheet);
    }


    /**
     * Supprime une feuille de temps
     * @param id ID de la feuille de temps
     */
    public void deleteTimeSheet(Integer id) {
        timeSheetRepository.deleteById(id);
    }

    /**
     * Ajoute une tâche à une feuille de temps
     * @param timeSheetId ID de la feuille de temps
     * @param taskId ID de la tâche
     * @param duration Durée en minutes
     * @return Tâche ajoutée à la feuille de temps
     */
    public TimeSheetTask addTaskToTimeSheet(Integer timeSheetId, Integer taskId, Integer duration) {
        // Retrieve the timesheet from the database
        TimeSheet timeSheet = timeSheetRepository.findById(timeSheetId)
                .orElseThrow(() -> new RuntimeException("TimeSheet introuvable"));

        // Retrieve the task from the database
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));


        // Create the TimeSheetTask entity by setting the composite key and relationships
        TimeSheetTask timeSheetTask = TimeSheetTask.builder()
                .timeSheetId(timeSheet.getId())
                .taskId(task.getId())
                .duration(duration)
                .timeSheet(timeSheet)
                .task(task)
                .build();

        // Save the association entity
        return timeSheetTaskRepository.save(timeSheetTask);
    }


    /**
     * Récupère les feuilles de temps par date
     * @param date Date
     * @return Liste des feuilles de temps
     */
    public List<TimeSheet> getTimeSheetsByDate(LocalDate date) {
        return timeSheetRepository.findByEntryDate(date);
    }

    /**
     * Récupère les feuilles de temps partagées avec un utilisateur
     * @param userId ID de l'utilisateur
     * @return Liste des feuilles de temps partagées
     */
    public List<TimeSheet> getTimeSheetSharedWithUser(Integer userId) {
        return timeSheetRepository.findSharedWithUser(userId);
    }

    /**
     * Récupère les feuilles de temps partagées avec un groupe
     * @param groupId ID du groupe
     * @return Liste des feuilles de temps partagées
     */
    public List<TimeSheet> getTimeSheetSharedWithGroup(Integer groupId) {
        return timeSheetRepository.findSharedWithGroup(groupId);
    }

    /**
     * Partage une feuille de temps avec un utilisateur
     * @param timeSheetId ID de la feuille de temps
     * @param userId ID de l'utilisateur
     * @param accessLevel Niveau d'accès (READ, WRITE)
     */
    public void shareTimeSheetWithUser(Integer timeSheetId, Integer userId, String accessLevel) {
        // Retrieve the timesheet from the database
        TimeSheet timeSheet = timeSheetRepository.findById(timeSheetId)
                .orElseThrow(() -> new RuntimeException("TimeSheet introuvable"));

        // Retrieve the user from the database
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // Create the composite key for the share
        TimeSheetShareUserId shareId = new TimeSheetShareUserId(timeSheet.getId(), user.getId());

        if (timeSheetShareUserRepository.existsById(shareId)) {
            // Update the access level if the share already exists
            TimeSheetShareUser existingShare = timeSheetShareUserRepository.findById(shareId).get();
            existingShare.setAccessLevel(accessLevel);
            timeSheetShareUserRepository.save(existingShare);
        } else {
            // Create a new share record with proper references
            TimeSheetShareUser shareUser = new TimeSheetShareUser();
            shareUser.setTimeSheetId(timeSheet.getId());
            shareUser.setUserId(user.getId());
            shareUser.setAccessLevel(accessLevel);

            // Optionally set the associated entities as well
            shareUser.setTimeSheet(timeSheet);
            shareUser.setUser(user);

            timeSheetShareUserRepository.save(shareUser);
        }
    }

    /**
     * Partage une feuille de temps avec un groupe
     * @param timeSheetId ID de la feuille de temps
     * @param groupId ID du groupe
     * @param accessLevel Niveau d'accès (READ, WRITE)
     */
    public void shareTimeSheetWithGroup(Integer timeSheetId, Integer groupId, String accessLevel) {
        // Retrieve the timesheet from the database
        TimeSheet timeSheet = timeSheetRepository.findById(timeSheetId)
                .orElseThrow(() -> new RuntimeException("TimeSheet introuvable"));

        // Retrieve the group from the database
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Groupe introuvable"));

        // Create the composite key for the share record
        TimeSheetShareGroupId id = new TimeSheetShareGroupId(timeSheet.getId(), group.getId());

        if (timeSheetShareGroupRepository.existsById(id)) {
            // Update the access level if the share record already exists
            TimeSheetShareGroup existingShare = timeSheetShareGroupRepository.findById(id).get();
            existingShare.setAccessLevel(accessLevel);
            timeSheetShareGroupRepository.save(existingShare);
        } else {
            // Create a new share record with proper references
            TimeSheetShareGroup shareGroup = new TimeSheetShareGroup();
            shareGroup.setTimeSheetId(timeSheet.getId());
            shareGroup.setGroupId(group.getId());
            shareGroup.setAccessLevel(accessLevel);

            // Optionally set the associated entities to maintain consistency
            shareGroup.setTimeSheet(timeSheet);
            shareGroup.setGroup(group);

            timeSheetShareGroupRepository.save(shareGroup);
        }
    }

    /**
     * Exporte les feuilles de temps en CSV
     * @param userId ID de l'utilisateur
     * @param startDate Date de début (optionnelle)
     * @param endDate Date de fin (optionnelle)
     * @return Données CSV
     */
    public byte[] exportTimeSheetsToCsv(Integer userId, LocalDate startDate, LocalDate endDate) {
        // Récupérer les feuilles de temps de l'utilisateur
        List<TimeSheet> timeSheets = timeSheetRepository.findByUserId(userId);

        // Filtrer par date si nécessaire
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

        // Construire le contenu CSV
        StringBuilder csvContent = new StringBuilder();

        // En-tête
        csvContent.append("ID,Date,Icon,UserID,TaskID,TaskName,Duration\n");

        // Données
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (TimeSheet timeSheet : timeSheets) {
            List<TimeSheetTask> tasks = timeSheetTaskRepository.findByTimeSheetId(timeSheet.getId());

            for (TimeSheetTask task : tasks) {
                csvContent.append(timeSheet.getId()).append(",");
                csvContent.append(timeSheet.getEntryDate().format(dateFormatter)).append(",");
                csvContent.append(timeSheet.getIcon() != null ? timeSheet.getIcon() : "").append(",");
                csvContent.append(timeSheet.getUser().getId()).append(",");
                csvContent.append(task.getTaskId()).append(",");
                csvContent.append(task.getTask() != null ? task.getTask().getName().replace(",", ";") : "Unknown").append(",");
                csvContent.append(task.getDuration()).append("\n");
            }
        }

        return csvContent.toString().getBytes(StandardCharsets.UTF_8);
    }
}