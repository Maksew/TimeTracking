package isis.projet.backend.controller;

import isis.projet.backend.dto.StatisticsDTO;
import isis.projet.backend.entity.TimeSheet;
import isis.projet.backend.security.jwt.JwtUserDetails;
import isis.projet.backend.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    /**
     * Récupère les statistiques pour l'utilisateur authentifié
     * @param authentication Informations d'authentification
     * @return Statistiques de l'utilisateur
     */
    @GetMapping("/user")
    public ResponseEntity<StatisticsDTO.StatisticsResponse> getCurrentUserStatistics(Authentication authentication) {
        JwtUserDetails userDetails = (JwtUserDetails) authentication.getPrincipal();
        StatisticsDTO.StatisticsResponse statistics = statisticsService.getUserStatistics(userDetails.getId());
        return ResponseEntity.ok(statistics);
    }

    /**
     * Récupère les statistiques pour un utilisateur spécifique
     * @param userId ID de l'utilisateur
     * @return Statistiques de l'utilisateur
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<StatisticsDTO.StatisticsResponse> getUserStatistics(@PathVariable Integer userId) {
        StatisticsDTO.StatisticsResponse statistics = statisticsService.getUserStatistics(userId);
        return ResponseEntity.ok(statistics);
    }

    /**
     * Récupère les statistiques pour un groupe
     * @param groupId ID du groupe
     * @return Statistiques du groupe
     */
    @GetMapping("/group/{groupId}")
    public ResponseEntity<Map<Integer, StatisticsDTO.UserStatsSummary>> getGroupStatistics(@PathVariable Integer groupId) {
        Map<Integer, StatisticsDTO.UserStatsSummary> statistics = statisticsService.getGroupStatistics(groupId);
        return ResponseEntity.ok(statistics);
    }

    /**
     * Récupère les statistiques pour une période spécifique
     * @param authentication Informations d'authentification
     * @param startDate Date de début
     * @param endDate Date de fin
     * @return Statistiques pour la période
     */
    @GetMapping("/period")
    public ResponseEntity<StatisticsDTO.StatisticsResponse> getPeriodStatistics(
            Authentication authentication,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        JwtUserDetails userDetails = (JwtUserDetails) authentication.getPrincipal();
        StatisticsDTO.StatisticsResponse statistics =
                statisticsService.getStatisticsByPeriod(userDetails.getId(), startDate, endDate);

        return ResponseEntity.ok(statistics);
    }

    /**
     * Récupère les statistiques pour une période spécifique pour un utilisateur spécifique
     * @param userId ID de l'utilisateur
     * @param startDate Date de début
     * @param endDate Date de fin
     * @return Statistiques pour la période
     */
    @GetMapping("/user/{userId}/period")
    public ResponseEntity<StatisticsDTO.StatisticsResponse> getUserPeriodStatistics(
            @PathVariable Integer userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        StatisticsDTO.StatisticsResponse statistics =
                statisticsService.getStatisticsByPeriod(userId, startDate, endDate);

        return ResponseEntity.ok(statistics);
    }
}