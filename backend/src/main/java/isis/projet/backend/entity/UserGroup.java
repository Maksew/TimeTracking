package isis.projet.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "USER_GROUP")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(UserGroupId.class)
public class UserGroup {
    @Id
    @Column(name = "user_id")
    private Integer userId;

    @Id
    @Column(name = "group_id")
    private Integer groupId;

    @Column(name = "role", nullable = false, length = 50)
    private String role;

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "group_id", insertable = false, updatable = false)
    private Group group;
}