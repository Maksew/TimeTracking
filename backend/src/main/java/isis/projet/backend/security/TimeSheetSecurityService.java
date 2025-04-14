package isis.projet.backend.security;

import isis.projet.backend.entity.TimeSheet;
import isis.projet.backend.repository.TimeSheetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component("timesheetSecurityService")
public class TimeSheetSecurityService {

    @Autowired
    private TimeSheetRepository timeSheetRepository;

    /**
     * Returns true if the current user (by email) is the owner of the timesheet.
     */
    public boolean isOwner(Integer timesheetId, UserDetails currentUser) {
        TimeSheet timesheet = timeSheetRepository.findById(timesheetId).orElse(null);
        if (timesheet == null) return false;
        return currentUser.getUsername().equals(timesheet.getUser().getEmail());
    }
}
