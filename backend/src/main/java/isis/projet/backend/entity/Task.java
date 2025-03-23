package isis.projet.backend.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Table(name = "TASK")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    private Integer id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "repetition", length = 50)
    private String repetition;

    // Relation inverse avec TIME_SHEET_TASK (pas dans le SQL mais utile pour JPA)
    @OneToMany(mappedBy = "task")
    @JsonManagedReference("task-timesheettasks")
    private List<TimeSheetTask> timeSheetTasks = new ArrayList<>();
}