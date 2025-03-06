package isis.projet.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Table(name = "GROUP") // "GROUP" est un mot reserv SQL, peut causer des problèmes
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

    // Relation inverse avec USER_GROUP (pas dans le SQL mais utile pour JPA)
    @OneToMany(mappedBy = "group")
    private List<UserGroup> userGroups = new ArrayList<>();

    // Relation inverse avec TIME_SHEET_SHARE_GROUP
    @OneToMany(mappedBy = "group")
    private List<TimeSheetShareGroup> sharedTimeSheets = new ArrayList<>();

    // Relation inverse avec INVITATION
    @OneToMany(mappedBy = "group")
    private List<Invitation> invitations = new ArrayList<>();
}