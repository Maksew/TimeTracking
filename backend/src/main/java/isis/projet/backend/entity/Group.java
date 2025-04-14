package isis.projet.backend.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Table(name = "\"GROUP\"") // On utilise des guillemets doubles pour échapper le mot réservé SQL
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "invit_code")
    private String invitCode;

    // Relation inverse avec USER_GROUP
    @OneToMany(mappedBy = "group")
    @JsonManagedReference("group-usergroups")
    private List<UserGroup> userGroups = new ArrayList<>();

    // Relation inverse avec TIME_SHEET_SHARE_GROUP
    @OneToMany(mappedBy = "group")
    @JsonManagedReference("group-sharedtimesheets")
    private List<TimeSheetShareGroup> sharedTimeSheets = new ArrayList<>();

    // Relation inverse avec INVITATION
    @OneToMany(mappedBy = "group")
    @JsonManagedReference("group-invitations")
    private List<Invitation> invitations = new ArrayList<>();
}