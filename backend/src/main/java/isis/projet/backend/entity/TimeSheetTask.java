package isis.projet.backend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "TIME_SHEET_TASK")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(TimeSheetTaskId.class)
public class TimeSheetTask {
    @Id
    @Column(name = "task_id")
    private Integer taskId;

    @Id
    @Column(name = "time_sheet_id")
    private Integer timeSheetId;

    @Column(name = "duration", nullable = false)
    private Integer duration;

    @ManyToOne
    @JoinColumn(name = "task_id", insertable = false, updatable = false)
    @JsonBackReference("task-timesheettasks")
    private Task task;

    @ManyToOne
    @JoinColumn(name = "time_sheet_id", insertable = false, updatable = false)
    @JsonBackReference("timesheet-tasks")
    private TimeSheet timeSheet;
}