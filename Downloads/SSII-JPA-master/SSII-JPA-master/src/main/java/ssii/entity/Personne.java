package ssii.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor

public class Personne {
    @Id
    private Integer matricule;
    private String nom;
    private String prenom;
    private String poste;

    @OneToMany(mappedBy = "personne")
    private Set<Participation> participations;
}
