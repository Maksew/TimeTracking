package isis.projet.backend.service;

import isis.projet.backend.dto.StatisticsDTO;
import isis.projet.backend.entity.*;
import isis.projet.backend.repository.TaskRepository;
import isis.projet.backend.repository.TimeSheetRepository;
import isis.projet.backend.repository.TimeSheetTaskRepository;
import isis.projet.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    @Autowired
    private TimeSheetRepository timeSheetRepository;

    @Autowired
    private TimeSheetTaskRepository timeSheetTaskRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Obtient les statistiques globales pour un utilisateur
     * @param userId ID de l'utilisateur
     * @return Statistiques globales
     */
    public StatisticsDTO.StatisticsResponse getUserStatistics(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        List<TimeSheet> timeSheets = timeSheetRepository.findByUserId(userId);

        if (timeSheets.isEmpty()) {
            return createEmptyStatistics(user);
        }

        StatisticsDTO.UserStatsSummary summary = calculateUserSummary(user, timeSheets);
        List<StatisticsDTO.CategoryStats> categories = calculateCategoryStats(timeSheets);
        StatisticsDTO.PeriodStats dailyStats = calculatePeriodStats(timeSheets, "day");
        StatisticsDTO.PeriodStats weeklyStats = calculatePeriodStats(timeSheets, "week");
        StatisticsDTO.PeriodStats monthlyStats = calculatePeriodStats(timeSheets, "month");

        return StatisticsDTO.StatisticsResponse.builder()
                .summary(summary)
                .categories(categories)
                .dailyStats(dailyStats)
                .weeklyStats(weeklyStats)
                .monthlyStats(monthlyStats)
                .build();
    }

    /**
     * Obtient les statistiques pour un groupe
     * @param groupId ID du groupe
     * @return Statistiques du groupe
     */
    public Map<Integer, StatisticsDTO.UserStatsSummary> getGroupStatistics(Integer groupId) {
        List<UserGroup> userGroups = new ArrayList<>();

        Map<Integer, StatisticsDTO.UserStatsSummary> userStats = new HashMap<>();

        for (UserGroup userGroup : userGroups) {
            User user = userGroup.getUser();
            List<TimeSheet> timeSheets = timeSheetRepository.findByUserId(user.getId());

            if (!timeSheets.isEmpty()) {
                userStats.put(user.getId(), calculateUserSummary(user, timeSheets));
            }
        }

        return userStats;
    }

    /**
     * Obtient les statistiques pour une période spécifique
     * @param userId ID de l'utilisateur
     * @param startDate Date de début
     * @param endDate Date de fin
     * @return Statistiques pour la période
     */
    public StatisticsDTO.StatisticsResponse getStatisticsByPeriod(Integer userId, LocalDate startDate, LocalDate endDate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        List<TimeSheet> allTimeSheets = timeSheetRepository.findByUserId(userId);

        List<TimeSheet> timeSheets = allTimeSheets.stream()
                .filter(ts -> !ts.getEntryDate().isBefore(startDate) && !ts.getEntryDate().isAfter(endDate))
                .collect(Collectors.toList());

        if (timeSheets.isEmpty()) {
            return createEmptyStatistics(user);
        }

        StatisticsDTO.UserStatsSummary summary = calculateUserSummary(user, timeSheets);
        List<StatisticsDTO.CategoryStats> categories = calculateCategoryStats(timeSheets);
        StatisticsDTO.PeriodStats periodStats = calculateCustomPeriodStats(timeSheets, startDate, endDate);

        return StatisticsDTO.StatisticsResponse.builder()
                .summary(summary)
                .categories(categories)
                .dailyStats(periodStats)
                .build();
    }

    /**
     * Calcule un résumé des statistiques pour un utilisateur
     * @param user Utilisateur
     * @param timeSheets Liste des feuilles de temps
     * @return Résumé des statistiques
     */
    private StatisticsDTO.UserStatsSummary calculateUserSummary(User user, List<TimeSheet> timeSheets) {
        int totalTasks = 0;
        int completedTasks = 0;
        int totalTimeInMinutes = 0;

        for (TimeSheet timeSheet : timeSheets) {
            List<TimeSheetTask> timeSheetTasks = timeSheetTaskRepository.findByTimeSheetId(timeSheet.getId());

            totalTasks += timeSheetTasks.size();

            for (TimeSheetTask tst : timeSheetTasks) {
                totalTimeInMinutes += tst.getDuration();
                if (tst.getDuration() > 0) {
                    completedTasks++;
                }
            }
        }

        double completionRate = totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0;

        return StatisticsDTO.UserStatsSummary.builder()
                .userId(user.getId())
                .userName(user.getPseudo())
                .totalTimeSheets(timeSheets.size())
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .totalTimeInMinutes(totalTimeInMinutes)
                .completionRate(completionRate)
                .build();
    }

    /**
     * Calcule les statistiques par catégorie
     * @param timeSheets Liste des feuilles de temps
     * @return Liste des statistiques par catégorie
     */
    private List<StatisticsDTO.CategoryStats> calculateCategoryStats(List<TimeSheet> timeSheets) {
        Map<String, List<Task>> tasksByCategory = new HashMap<>();
        Map<Integer, Integer> taskTimeMap = new HashMap<>();

        for (TimeSheet timeSheet : timeSheets) {
            List<TimeSheetTask> timeSheetTasks = timeSheetTaskRepository.findByTimeSheetId(timeSheet.getId());

            for (TimeSheetTask tst : timeSheetTasks) {
                Task task = taskRepository.findById(tst.getTaskId()).orElse(null);
                if (task != null) {
                    String category = task.getRepetition() != null ? task.getRepetition() : "NONE";

                    if (!tasksByCategory.containsKey(category)) {
                        tasksByCategory.put(category, new ArrayList<>());
                    }

                    if (!tasksByCategory.get(category).contains(task)) {
                        tasksByCategory.get(category).add(task);
                    }

                    taskTimeMap.put(task.getId(), taskTimeMap.getOrDefault(task.getId(), 0) + tst.getDuration());
                }
            }
        }

        List<StatisticsDTO.CategoryStats> result = new ArrayList<>();
        int totalTime = taskTimeMap.values().stream().mapToInt(Integer::intValue).sum();

        for (Map.Entry<String, List<Task>> entry : tasksByCategory.entrySet()) {
            String category = entry.getKey();
            List<Task> tasks = entry.getValue();

            int categoryTotalTime = 0;
            List<StatisticsDTO.TaskStats> taskStatsList = new ArrayList<>();

            for (Task task : tasks) {
                int taskTime = taskTimeMap.getOrDefault(task.getId(), 0);
                categoryTotalTime += taskTime;

                StatisticsDTO.TaskStats taskStats = StatisticsDTO.TaskStats.builder()
                        .taskId(task.getId())
                        .taskName(task.getName())
                        .icon("mdi-checkbox-marked-circle-outline")
                        .totalTimeInMinutes(taskTime)
                        .completed(taskTime > 0)
                        .percentageOfTotal(totalTime > 0 ? (double) taskTime / totalTime * 100 : 0)
                        .build();

                taskStatsList.add(taskStats);
            }

            taskStatsList.sort((a, b) -> b.getTotalTimeInMinutes().compareTo(a.getTotalTimeInMinutes()));

            StatisticsDTO.CategoryStats categoryStats = StatisticsDTO.CategoryStats.builder()
                    .category(category)
                    .totalTasks(tasks.size())
                    .completedTasks((int) taskStatsList.stream().filter(StatisticsDTO.TaskStats::getCompleted).count())
                    .totalTimeInMinutes(categoryTotalTime)
                    .completionRate(tasks.size() > 0 ? (double) taskStatsList.stream().filter(StatisticsDTO.TaskStats::getCompleted).count() / tasks.size() * 100 : 0)
                    .tasks(taskStatsList)
                    .build();

            result.add(categoryStats);
        }

        result.sort((a, b) -> b.getTotalTimeInMinutes().compareTo(a.getTotalTimeInMinutes()));

        return result;
    }

    /**
     * Calcule les statistiques pour une période spécifique
     * @param timeSheets Liste des feuilles de temps
     * @param periodType Type de période ("day", "week", "month")
     * @return Statistiques pour la période
     */
    private StatisticsDTO.PeriodStats calculatePeriodStats(List<TimeSheet> timeSheets, String periodType) {
        DateTimeFormatter formatter;
        Map<String, Integer> timeDistribution = new HashMap<>();

        switch (periodType) {
            case "day":
                formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                break;
            case "week":
                formatter = DateTimeFormatter.ofPattern("yyyy-'W'ww");
                break;
            case "month":
                formatter = DateTimeFormatter.ofPattern("yyyy-MM");
                break;
            default:
                formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        }

        int totalTimeInMinutes = 0;
        int totalTasks = 0;
        int completedTasks = 0;

        for (TimeSheet timeSheet : timeSheets) {
            String periodKey;

            if ("week".equals(periodType)) {
                WeekFields weekFields = WeekFields.of(Locale.getDefault());
                int weekNumber = timeSheet.getEntryDate().get(weekFields.weekOfWeekBasedYear());
                periodKey = timeSheet.getEntryDate().getYear() + "-W" + String.format("%02d", weekNumber);
            } else if ("month".equals(periodType)) {
                periodKey = timeSheet.getEntryDate().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            } else {
                periodKey = timeSheet.getEntryDate().format(formatter);
            }

            List<TimeSheetTask> timeSheetTasks = timeSheetTaskRepository.findByTimeSheetId(timeSheet.getId());
            totalTasks += timeSheetTasks.size();

            for (TimeSheetTask tst : timeSheetTasks) {
                int duration = tst.getDuration();
                totalTimeInMinutes += duration;

                if (duration > 0) {
                    completedTasks++;
                }

                timeDistribution.put(periodKey, timeDistribution.getOrDefault(periodKey, 0) + duration);
            }
        }

        return StatisticsDTO.PeriodStats.builder()
                .period(periodType)
                .timeDistribution(timeDistribution)
                .totalTimeInMinutes(totalTimeInMinutes)
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .build();
    }

    /**
     * Calcule les statistiques pour une période personnalisée
     * @param timeSheets Liste des feuilles de temps
     * @param startDate Date de début
     * @param endDate Date de fin
     * @return Statistiques pour la période
     */
    private StatisticsDTO.PeriodStats calculateCustomPeriodStats(List<TimeSheet> timeSheets, LocalDate startDate, LocalDate endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Map<String, Integer> timeDistribution = new HashMap<>();

        int totalTimeInMinutes = 0;
        int totalTasks = 0;
        int completedTasks = 0;

        for (TimeSheet timeSheet : timeSheets) {
            if (!timeSheet.getEntryDate().isBefore(startDate) && !timeSheet.getEntryDate().isAfter(endDate)) {
                String periodKey = timeSheet.getEntryDate().format(formatter);

                List<TimeSheetTask> timeSheetTasks = timeSheetTaskRepository.findByTimeSheetId(timeSheet.getId());
                totalTasks += timeSheetTasks.size();

                for (TimeSheetTask tst : timeSheetTasks) {
                    int duration = tst.getDuration();
                    totalTimeInMinutes += duration;

                    if (duration > 0) {
                        completedTasks++;
                    }

                    timeDistribution.put(periodKey, timeDistribution.getOrDefault(periodKey, 0) + duration);
                }
            }
        }

        return StatisticsDTO.PeriodStats.builder()
                .period("custom")
                .timeDistribution(timeDistribution)
                .totalTimeInMinutes(totalTimeInMinutes)
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .build();
    }

    /**
     * Crée un objet statistique vide pour un utilisateur sans données
     * @param user Utilisateur
     * @return Statistiques vides
     */
    private StatisticsDTO.StatisticsResponse createEmptyStatistics(User user) {
        StatisticsDTO.UserStatsSummary summary = StatisticsDTO.UserStatsSummary.builder()
                .userId(user.getId())
                .userName(user.getPseudo())
                .totalTimeSheets(0)
                .totalTasks(0)
                .completedTasks(0)
                .totalTimeInMinutes(0)
                .completionRate(0.0)
                .build();

        StatisticsDTO.PeriodStats emptyPeriodStats = StatisticsDTO.PeriodStats.builder()
                .period("empty")
                .timeDistribution(new HashMap<>())
                .totalTimeInMinutes(0)
                .totalTasks(0)
                .completedTasks(0)
                .build();

        return StatisticsDTO.StatisticsResponse.builder()
                .summary(summary)
                .categories(new ArrayList<>())
                .dailyStats(emptyPeriodStats)
                .weeklyStats(emptyPeriodStats)
                .monthlyStats(emptyPeriodStats)
                .build();
    }
}