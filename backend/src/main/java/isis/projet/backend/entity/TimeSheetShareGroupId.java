package isis.projet.backend.entity;

import lombok.*;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeSheetShareGroupId implements Serializable {
    private Integer timeSheetId;
    private Integer groupId;
}