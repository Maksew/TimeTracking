package isis.projet.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTOs pour les statistiques
 */
public class StatisticsDTO {

    /**
     * Statistiques globales pour un utilisateur
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserStatsSummary {
        private Integer userId;
        private String userName;
        private Integer totalTimeSheets;
        private Integer totalTasks;
        private Integer completedTasks;
        private Integer totalTimeInMinutes;
        private Double completionRate; // Pourcentage de tâches complétées
    }

    /**
     * Statistiques pour une catégorie de tâche spécifique
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryStats {
        private String category;
        private Integer totalTasks;
        private Integer completedTasks;
        private Integer totalTimeInMinutes;
        private Double completionRate;
        private List<TaskStats> tasks;
    }

    /**
     * Statistiques pour une tâche spécifique
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TaskStats {
        private Integer taskId;
        private String taskName;
        private String icon;
        private Integer totalTimeInMinutes;
        private Boolean completed;
        private Double percentageOfTotal; // Pourcentage du temps total
    }

    /**
     * Statistiques pour une période (jour, semaine, mois)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PeriodStats {
        private String period; // "day", "week", "month", etc.
        private Map<String, Integer> timeDistribution; // Clé: date, Valeur: minutes
        private Integer totalTimeInMinutes;
        private Integer totalTasks;
        private Integer completedTasks;
    }

    /**
     * Réponse contenant toutes les statistiques
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StatisticsResponse {
        private UserStatsSummary summary;
        private List<CategoryStats> categories;
        private PeriodStats dailyStats;
        private PeriodStats weeklyStats;
        private PeriodStats monthlyStats;
    }
}