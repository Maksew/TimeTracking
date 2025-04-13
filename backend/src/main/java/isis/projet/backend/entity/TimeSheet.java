package isis.projet.backend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.*;

@Entity
@Table(name = "TIME_SHEET")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSheet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "time_sheet_id")
    private Integer id;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(name = "icon", length = 50)
    private String icon;

    @Column(name = "title", length = 100)
    private String title;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "start_time")
    private String startTime;

    @Column(name = "end_time")
    private String endTime;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference("user-timesheets")
    private User user;

    // Relation inverse avec TIME_SHEET_TASK (pas dans le SQL mais utile pour JPA)
    @OneToMany(mappedBy = "timeSheet")
    @JsonManagedReference("timesheet-tasks")
    private List<TimeSheetTask> timeSheetTasks = new ArrayList<>();

    // Relation inverse avec TIME_SHEET_SHARE_USER
    @OneToMany(mappedBy = "timeSheet")
    @JsonManagedReference("timesheet-sharedusers")
    private List<TimeSheetShareUser> sharedWithUsers = new ArrayList<>();

    // Relation inverse avec TIME_SHEET_SHARE_GROUP
    @OneToMany(mappedBy = "timeSheet")
    @JsonManagedReference("timesheet-sharedgroups")
    private List<TimeSheetShareGroup> sharedWithGroups = new ArrayList<>();
}