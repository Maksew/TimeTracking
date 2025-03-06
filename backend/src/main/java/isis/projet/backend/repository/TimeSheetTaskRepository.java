package isis.projet.backend.repository;

import isis.projet.backend.entity.TimeSheetTask;
import isis.projet.backend.entity.TimeSheetTaskId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimeSheetTaskRepository extends JpaRepository<TimeSheetTask, TimeSheetTaskId> {
    List<TimeSheetTask> findByTaskId(Integer taskId);

    List<TimeSheetTask> findByTimeSheetId(Integer timeSheetId);
}