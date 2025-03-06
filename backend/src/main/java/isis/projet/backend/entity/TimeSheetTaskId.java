package isis.projet.backend.entity;

import lombok.*;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeSheetTaskId implements Serializable {
    private Integer taskId;
    private Integer timeSheetId;
}