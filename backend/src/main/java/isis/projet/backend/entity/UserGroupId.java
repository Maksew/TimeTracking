package isis.projet.backend.entity;

import lombok.*;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserGroupId implements Serializable {
    private Integer userId;
    private Integer groupId;
}