package isis.projet.backend.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Table(name = "\"USER\"")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_generator")
    @SequenceGenerator(name = "user_generator", sequenceName = "user_id_seq", allocationSize = 1)
    @Column(name = "user_id")
    private Integer id;

    @Column(name = "pseudo", nullable = false)
    private String pseudo;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "role", nullable = false)
    private String role;

    // Relation inverse avec USER_GROUP (pas dans le SQL mais utile pour JPA)
    @OneToMany(mappedBy = "user")
    @JsonManagedReference("user-usergroups")
    private List<UserGroup> userGroups = new ArrayList<>();

    // Relation inverse avec TIME_SHEET
    @OneToMany(mappedBy = "user")
    @JsonManagedReference("user-timesheets")
    private List<TimeSheet> timeSheets = new ArrayList<>();

    // Relation inverse avec TIME_SHEET_SHARE_USER
    @OneToMany(mappedBy = "user")
    @JsonManagedReference("user-sharedtimesheets")
    private List<TimeSheetShareUser> sharedTimeSheets = new ArrayList<>();

    // Relation inverse avec INVITATION (comme expéditeur)
    @OneToMany(mappedBy = "sender")
    @JsonManagedReference("user-sentinvitations")
    private List<Invitation> sentInvitations = new ArrayList<>();

    @OneToMany(mappedBy = "recipient")
    @JsonManagedReference("user-receivedinvitations")
    private List<Invitation> receivedInvitations = new ArrayList<>();
}