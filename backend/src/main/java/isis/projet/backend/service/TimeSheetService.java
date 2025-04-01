package isis.projet.backend.service;

import isis.projet.backend.entity.*;
import isis.projet.backend.repository.TimeSheetRepository;
import isis.projet.backend.repository.TimeSheetShareGroupRepository;
import isis.projet.backend.repository.TimeSheetShareUserRepository;
import isis.projet.backend.repository.TimeSheetTaskRepository;
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
        TimeSheetTask timeSheetTask = new TimeSheetTask();
        timeSheetTask.setTimeSheetId(timeSheetId);
        timeSheetTask.setTaskId(taskId);
        timeSheetTask.setDuration(duration);
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
        // Vérifier si le partage existe déjà
        TimeSheetShareUserId id = new TimeSheetShareUserId(timeSheetId, userId);
        if (timeSheetShareUserRepository.existsById(id)) {
            // Mettre à jour le niveau d'accès
            TimeSheetShareUser existingShare = timeSheetShareUserRepository.findById(id).get();
            existingShare.setAccessLevel(accessLevel);
            timeSheetShareUserRepository.save(existingShare);
        } else {
            // Créer un nouveau partage
            TimeSheetShareUser shareUser = new TimeSheetShareUser();
            shareUser.setTimeSheetId(timeSheetId);
            shareUser.setUserId(userId);
            shareUser.setAccessLevel(accessLevel);
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
        // Vérifier si le partage existe déjà
        TimeSheetShareGroupId id = new TimeSheetShareGroupId(timeSheetId, groupId);
        if (timeSheetShareGroupRepository.existsById(id)) {
            // Mettre à jour le niveau d'accès
            TimeSheetShareGroup existingShare = timeSheetShareGroupRepository.findById(id).get();
            existingShare.setAccessLevel(accessLevel);
            timeSheetShareGroupRepository.save(existingShare);
        } else {
            // Créer un nouveau partage
            TimeSheetShareGroup shareGroup = new TimeSheetShareGroup();
            shareGroup.setTimeSheetId(timeSheetId);
            shareGroup.setGroupId(groupId);
            shareGroup.setAccessLevel(accessLevel);
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