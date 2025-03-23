package isis.projet.backend.service;

import isis.projet.backend.entity.TimeSheet;
import isis.projet.backend.entity.User;
import isis.projet.backend.repository.TimeSheetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TimeSheetService {

    @Autowired
    private TimeSheetRepository timeSheetRepository;

    public List<TimeSheet> getAllTimeSheetsByUserId(Integer userId) {
        return timeSheetRepository.findByUserId(userId);
    }

    public Optional<TimeSheet> getTimeSheetById(Integer id) {
        return timeSheetRepository.findById(id);
    }

    public TimeSheet createTimeSheet(TimeSheet timeSheet, User user) {
        timeSheet.setUser(user);
        if (timeSheet.getEntryDate() == null) {
            timeSheet.setEntryDate(LocalDate.now());
        }
        return timeSheetRepository.save(timeSheet);
    }

    public TimeSheet updateTimeSheet(TimeSheet timeSheet) {
        if (timeSheet.getId() == null || !timeSheetRepository.existsById(timeSheet.getId())) {
            throw new RuntimeException("Feuille de temps introuvable");
        }
        return timeSheetRepository.save(timeSheet);
    }

    public void deleteTimeSheet(Integer id) {
        timeSheetRepository.deleteById(id);
    }
    
}