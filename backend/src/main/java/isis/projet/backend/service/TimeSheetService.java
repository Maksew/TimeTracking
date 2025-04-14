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

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;


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

        // Gestion des périodes de validité
        if (timeSheet.getStartDate() == null) {
            timeSheet.setStartDate(LocalDate.now());
        }
        if (timeSheet.getEndDate() == null) {
            timeSheet.setEndDate(LocalDate.now().plusWeeks(2)); // Par défaut 2 semaines
        }

        return timeSheetRepository.save(timeSheet);
    }

    /**
     * Met à jour une feuille de temps
     * @param updatedTimeSheet Feuille de temps à mettre à jour
     * @return Feuille de temps mise à jour
     */
    public TimeSheet updateTimeSheet(TimeSheet updatedTimeSheet) {
        if (updatedTimeSheet.getId() == null || !timeSheetRepository.existsById(updatedTimeSheet.getId())) {
            throw new RuntimeException("Feuille de temps introuvable");
        }

        Optional<TimeSheet> optionalExisting = timeSheetRepository.findById(updatedTimeSheet.getId());
        if (!optionalExisting.isPresent()) {
            throw new RuntimeException("Feuille de temps introuvable");
        }
        TimeSheet existingTimeSheet = optionalExisting.get();

        // Mettre à jour les champs autorisés tout en préservant l'association avec l'utilisateur
        existingTimeSheet.setEntryDate(updatedTimeSheet.getEntryDate());
        existingTimeSheet.setIcon(updatedTimeSheet.getIcon());
        existingTimeSheet.setTitle(updatedTimeSheet.getTitle());

        // Mettre à jour les champs de période de validité
        existingTimeSheet.setStartDate(updatedTimeSheet.getStartDate());
        existingTimeSheet.setEndDate(updatedTimeSheet.getEndDate());
        existingTimeSheet.setStartTime(updatedTimeSheet.getStartTime());
        existingTimeSheet.setEndTime(updatedTimeSheet.getEndTime());

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
        TimeSheet timeSheet = timeSheetRepository.findById(timeSheetId)
                .orElseThrow(() -> new RuntimeException("TimeSheet introuvable"));

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        TimeSheetTask timeSheetTask = new TimeSheetTask();
        timeSheetTask.setTimeSheetId(timeSheet.getId());
        timeSheetTask.setTaskId(task.getId());
        timeSheetTask.setDuration(duration);
        timeSheetTask.setCompleted(false);
        timeSheetTask.setTimeSheet(timeSheet);
        timeSheetTask.setTask(task);

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
        TimeSheet timeSheet = timeSheetRepository.findById(timeSheetId)
                .orElseThrow(() -> new RuntimeException("TimeSheet introuvable"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        TimeSheetShareUserId shareId = new TimeSheetShareUserId(timeSheet.getId(), user.getId());

        if (timeSheetShareUserRepository.existsById(shareId)) {
            TimeSheetShareUser existingShare = timeSheetShareUserRepository.findById(shareId).get();
            existingShare.setAccessLevel(accessLevel);
            timeSheetShareUserRepository.save(existingShare);
        } else {
            TimeSheetShareUser shareUser = new TimeSheetShareUser();
            shareUser.setTimeSheetId(timeSheet.getId());
            shareUser.setUserId(user.getId());
            shareUser.setAccessLevel(accessLevel);

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
        TimeSheet timeSheet = timeSheetRepository.findById(timeSheetId)
                .orElseThrow(() -> new RuntimeException("TimeSheet introuvable"));

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Groupe introuvable"));

        TimeSheetShareGroupId id = new TimeSheetShareGroupId(timeSheet.getId(), group.getId());

        if (timeSheetShareGroupRepository.existsById(id)) {
            TimeSheetShareGroup existingShare = timeSheetShareGroupRepository.findById(id).get();
            existingShare.setAccessLevel(accessLevel);
            timeSheetShareGroupRepository.save(existingShare);
        } else {
            TimeSheetShareGroup shareGroup = new TimeSheetShareGroup();
            shareGroup.setTimeSheetId(timeSheet.getId());
            shareGroup.setGroupId(group.getId());
            shareGroup.setAccessLevel(accessLevel);

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
        List<TimeSheet> timeSheets = timeSheetRepository.findByUserId(userId);

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

        StringBuilder csvContent = new StringBuilder();

        // En-tête
        csvContent.append("ID,Date,Icon,UserID,TaskID,TaskName,Duration,StartDate,EndDate\n");

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
                csvContent.append(task.getDuration()).append(",");
                csvContent.append(timeSheet.getStartDate() != null ? timeSheet.getStartDate().format(dateFormatter) : "").append(",");
                csvContent.append(timeSheet.getEndDate() != null ? timeSheet.getEndDate().format(dateFormatter) : "").append("\n");
            }
        }

        return csvContent.toString().getBytes(StandardCharsets.UTF_8);
    }


    /**
     * Exports the user's timesheets as a PDF file.
     * @param userId ID de l'utilisateur
     * @param startDate Date de début (optionnelle)
     * @param endDate Date de fin (optionnelle)
     * @return PDF en tant que tableau d'octets
     */
    public byte[] exportTimeSheetsToPdf(Integer userId, LocalDate startDate, LocalDate endDate) {
        List<TimeSheet> sheets = timeSheetRepository.findByUserId(userId);
        if (startDate != null && endDate != null) {
            sheets = sheets.stream()
                    .filter(ts -> !ts.getEntryDate().isBefore(startDate) && !ts.getEntryDate().isAfter(endDate))
                    .collect(Collectors.toList());
        } else if (startDate != null) {
            sheets = sheets.stream()
                    .filter(ts -> !ts.getEntryDate().isBefore(startDate))
                    .collect(Collectors.toList());
        } else if (endDate != null) {
            sheets = sheets.stream()
                    .filter(ts -> !ts.getEntryDate().isAfter(endDate))
                    .collect(Collectors.toList());
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.WHITE);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);

            BaseColor primaryColor = new BaseColor(26, 35, 126);

            Paragraph title = new Paragraph("Export des feuilles de temps", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            PdfPTable titleTable = new PdfPTable(1);
            titleTable.setWidthPercentage(100);
            PdfPCell titleCell = new PdfPCell(title);
            titleCell.setBackgroundColor(primaryColor);
            titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            titleCell.setBorder(PdfPCell.NO_BORDER);
            titleTable.addCell(titleCell);
            document.add(titleTable);
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            // Add styled header cells for remaining columns: Date, TaskName, Duration, StartDate, EndDate
            addStyledHeaderCell(table, "Date", headerFont, primaryColor);
            addStyledHeaderCell(table, "TaskName", headerFont, primaryColor);
            addStyledHeaderCell(table, "Duration", headerFont, primaryColor);
            addStyledHeaderCell(table, "StartDate", headerFont, primaryColor);
            addStyledHeaderCell(table, "EndDate", headerFont, primaryColor);

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            for (TimeSheet ts : sheets) {
                List<TimeSheetTask> tasks = timeSheetTaskRepository.findByTimeSheetId(ts.getId());
                for (TimeSheetTask task : tasks) {
                    table.addCell(createCell(ts.getEntryDate().format(dateFormatter), cellFont));
                    String taskName = task.getTask() != null ? task.getTask().getName() : "Unknown";
                    table.addCell(createCell(taskName.replace(",", ";"), cellFont));
                    table.addCell(createCell(task.getDuration() != null ? task.getDuration().toString() : "", cellFont));
                    table.addCell(createCell(ts.getStartDate() != null ? ts.getStartDate().format(dateFormatter) : "", cellFont));
                    table.addCell(createCell(ts.getEndDate() != null ? ts.getEndDate().format(dateFormatter) : "", cellFont));
                }
            }

            document.add(table);
            document.close();
        } catch (DocumentException de) {
            throw new RuntimeException("Erreur lors de la génération du PDF: " + de.getMessage());
        }
        return baos.toByteArray();
    }



    /**
     * Adds a styled header cell to a table.
     */
    private void addStyledHeaderCell(PdfPTable table, String text, Font font, BaseColor backgroundColor) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, font));
        cell.setBackgroundColor(backgroundColor);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    /**
     * Creates a table cell with given text and font.
     */
    private PdfPCell createCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, font));
        cell.setPadding(4f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }


    /**
     * Met à jour la durée d'une tâche dans une feuille de temps
     * @param timeSheetId ID de la feuille de temps
     * @param taskId ID de la tâche
     * @param durationInSeconds Nouvelle durée en minutes
     * @return Tâche mise à jour
     */
    public TimeSheetTask updateTaskDuration(Integer timeSheetId, Integer taskId, Integer durationInSeconds) {
        TimeSheetTaskId id = new TimeSheetTaskId(taskId, timeSheetId);

        TimeSheetTask timeSheetTask = timeSheetTaskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Association tâche-feuille introuvable"));

        timeSheetTask.setDuration(durationInSeconds);
        return timeSheetTaskRepository.save(timeSheetTask);
    }

    /**
     * Met à jour l'état "complété" d'une tâche dans une feuille de temps
     * @param timeSheetId ID de la feuille de temps
     * @param taskId ID de la tâche
     * @param completed Nouvel état "complété"
     * @return Tâche mise à jour
     */
    public TimeSheetTask updateTaskCompletionState(Integer timeSheetId, Integer taskId, Boolean completed) {
        TimeSheetTaskId id = new TimeSheetTaskId(taskId, timeSheetId);

        TimeSheetTask timeSheetTask = timeSheetTaskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Association tâche-feuille introuvable"));

        // Mettre à jour l'état "complété"
        timeSheetTask.setCompleted(completed);

        return timeSheetTaskRepository.save(timeSheetTask);
    }

    /**
     * Supprime une tâche d'une feuille de temps
     * @param timeSheetId ID de la feuille de temps
     * @param taskId ID de la tâche
     */
    public void removeTaskFromTimeSheet(Integer timeSheetId, Integer taskId) {
        // Créer l'ID composite
        TimeSheetTaskId id = new TimeSheetTaskId(taskId, timeSheetId);

        // Vérifier si la relation existe
        if (timeSheetTaskRepository.existsById(id)) {
            // Supprimer la relation
            timeSheetTaskRepository.deleteById(id);
        } else {
            throw new RuntimeException("Association tâche-feuille de temps introuvable");
        }
    }
}