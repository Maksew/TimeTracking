package isis.projet.backend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "INVITATION")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invitation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invitation_id")
    private Integer id;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference("user-sentinvitations")
    private User sender;

    @ManyToOne
    @JoinColumn(name = "user_id_1", nullable = false)
    @JsonBackReference("user-receivedinvitations")
    private User recipient;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    @JsonBackReference("group-invitations")
    private Group group;
}