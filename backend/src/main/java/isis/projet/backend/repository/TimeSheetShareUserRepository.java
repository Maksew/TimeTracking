package isis.projet.backend.repository;

import isis.projet.backend.entity.TimeSheetShareUser;
import isis.projet.backend.entity.TimeSheetShareUserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimeSheetShareUserRepository extends JpaRepository<TimeSheetShareUser, TimeSheetShareUserId> {
    List<TimeSheetShareUser> findByUserId(Integer userId);
    List<TimeSheetShareUser> findByTimeSheetId(Integer timeSheetId);
}