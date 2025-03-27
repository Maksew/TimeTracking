package isis.projet.backend.repository;

import isis.projet.backend.entity.TimeSheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TimeSheetRepository extends JpaRepository<TimeSheet, Integer> {
    List<TimeSheet> findByUserId(Integer userId);

    List<TimeSheet> findByEntryDate(LocalDate entryDate);

    List<TimeSheet> findByEntryDateBetween(LocalDate startDate, LocalDate endDate);

    List<TimeSheet> findByUserIdAndEntryDateBetween(Integer userId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT ts FROM TimeSheet ts JOIN ts.sharedWithUsers swu WHERE swu.userId = :userId")
    List<TimeSheet> findSharedWithUser(@Param("userId") Integer userId);

    @Query("SELECT ts FROM TimeSheet ts JOIN ts.sharedWithGroups swg WHERE swg.groupId = :groupId")
    List<TimeSheet> findSharedWithGroup(@Param("groupId") Integer groupId);

    @Query("SELECT COUNT(DISTINCT ts.id) FROM TimeSheet ts WHERE ts.user.id = :userId")
    long countByUserId(@Param("userId") Integer userId);

    @Query("SELECT SUM(tst.duration) FROM TimeSheet ts JOIN ts.timeSheetTasks tst WHERE ts.user.id = :userId")
    Integer sumDurationByUserId(@Param("userId") Integer userId);

    @Query("SELECT SUM(tst.duration) FROM TimeSheet ts JOIN ts.timeSheetTasks tst WHERE ts.user.id = :userId AND ts.entryDate BETWEEN :startDate AND :endDate")
    Integer sumDurationByUserIdAndDateRange(@Param("userId") Integer userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}