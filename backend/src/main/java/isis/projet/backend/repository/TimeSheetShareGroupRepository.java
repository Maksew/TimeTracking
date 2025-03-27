package isis.projet.backend.repository;

import isis.projet.backend.entity.TimeSheetShareGroup;
import isis.projet.backend.entity.TimeSheetShareGroupId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimeSheetShareGroupRepository extends JpaRepository<TimeSheetShareGroup, TimeSheetShareGroupId> {
    List<TimeSheetShareGroup> findByGroupId(Integer groupId);
    List<TimeSheetShareGroup> findByTimeSheetId(Integer timeSheetId);
}