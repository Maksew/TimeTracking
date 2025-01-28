
@Entity
public class Participation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String role;
    private Float pourcentage;

    @ManyToOne
    @JoinColumn(name = "personne_matricule")
    private Personne personne;

    @ManyToOne
    @JoinColumn(name = "projet_code")
    private Projet projet;
}
