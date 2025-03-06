package isis.projet.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "TIME_SHEET_SHARE_USER")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(TimeSheetShareUserId.class)
public class TimeSheetShareUser {
    @Id
    @Column(name = "time_sheet_id")
    private Integer timeSheetId;

    @Id
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "access_level", nullable = false, length = 50)
    private String accessLevel;

    @ManyToOne
    @JoinColumn(name = "time_sheet_id", insertable = false, updatable = false)
    private TimeSheet timeSheet;

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
}