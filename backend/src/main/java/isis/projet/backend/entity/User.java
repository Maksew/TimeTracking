package isis.projet.backend.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import isis.projet.backend.security.converter.AttributeEncryptor;
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
    @Convert(converter = AttributeEncryptor.class)  // Field will be automatically encrypted/decrypted
    private String pseudo;

    @Column(name = "email", unique = true, nullable = false)
    @Convert(converter = AttributeEncryptor.class)  // Field will be automatically encrypted/decrypted
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "role", nullable = false)
    private String role;

    // Relation inverse avec USER_GROUP (pas dans le SQL mais utile pour JPA)
    @OneToMany(mappedBy = "user")
    @JsonManagedReference("user-usergroups")
    private List<UserGroup> userGroups = new ArrayList<>();

    // ... other relationships and fields remain the same
    @OneToMany(mappedBy = "user")
    @JsonManagedReference("user-timesheets")
    private List<TimeSheet> timeSheets = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    @JsonManagedReference("user-sharedtimesheets")
    private List<TimeSheetShareUser> sharedTimeSheets = new ArrayList<>();

    @OneToMany(mappedBy = "sender")
    @JsonManagedReference("user-sentinvitations")
    private List<Invitation> sentInvitations = new ArrayList<>();

    @OneToMany(mappedBy = "recipient")
    @JsonManagedReference("user-receivedinvitations")
    private List<Invitation> receivedInvitations = new ArrayList<>();
}
