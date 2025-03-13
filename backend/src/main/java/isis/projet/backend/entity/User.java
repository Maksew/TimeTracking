package isis.projet.backend.entity;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    private List<UserGroup> userGroups = new ArrayList<>();

    // Relation inverse avec TIME_SHEET
    @OneToMany(mappedBy = "user")
    private List<TimeSheet> timeSheets = new ArrayList<>();

    // Relation inverse avec TIME_SHEET_SHARE_USER
    @OneToMany(mappedBy = "user")
    private List<TimeSheetShareUser> sharedTimeSheets = new ArrayList<>();

    // Relation inverse avec INVITATION (comme expéditeur)
    @OneToMany(mappedBy = "sender")
    private List<Invitation> sentInvitations = new ArrayList<>();

    // Relation inverse avec INVITATION (comme destinataire)
    @OneToMany(mappedBy = "recipient")
    private List<Invitation> receivedInvitations = new ArrayList<>();
}