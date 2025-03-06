package isis.projet.backend.entity;

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

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Relation inverse avec TIME_SHEET_TASK (pas dans le SQL mais utile pour JPA)
    @OneToMany(mappedBy = "timeSheet")
    private List<TimeSheetTask> timeSheetTasks = new ArrayList<>();

    // Relation inverse avec TIME_SHEET_SHARE_USER
    @OneToMany(mappedBy = "timeSheet")
    private List<TimeSheetShareUser> sharedWithUsers = new ArrayList<>();

    // Relation inverse avec TIME_SHEET_SHARE_GROUP
    @OneToMany(mappedBy = "timeSheet")
    private List<TimeSheetShareGroup> sharedWithGroups = new ArrayList<>();
}