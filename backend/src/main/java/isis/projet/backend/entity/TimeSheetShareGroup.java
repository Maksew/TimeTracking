package isis.projet.backend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "TIME_SHEET_SHARE_GROUP")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(TimeSheetShareGroupId.class)
public class TimeSheetShareGroup {
    @Id
    @Column(name = "time_sheet_id")
    private Integer timeSheetId;

    @Id
    @Column(name = "group_id")
    private Integer groupId;

    @Column(name = "access_level", nullable = false, length = 50)
    private String accessLevel;

    @ManyToOne
    @JoinColumn(name = "time_sheet_id", insertable = false, updatable = false)
    @JsonBackReference("timesheet-sharedgroups")
    private TimeSheet timeSheet;

    @ManyToOne
    @JoinColumn(name = "group_id", insertable = false, updatable = false)
    @JsonBackReference("group-sharedtimesheets")
    private Group group;
}