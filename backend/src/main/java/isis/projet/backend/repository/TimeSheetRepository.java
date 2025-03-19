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

    @Query("SELECT ts FROM TimeSheet ts JOIN ts.sharedWithUsers swu WHERE swu.userId = :userId")
    List<TimeSheet> findSharedWithUser(@Param("userId") Integer userId);

    @Query("SELECT ts FROM TimeSheet ts JOIN ts.sharedWithGroups swg WHERE swg.groupId = :groupId")
    List<TimeSheet> findSharedWithGroup(@Param("groupId") Integer groupId);
}