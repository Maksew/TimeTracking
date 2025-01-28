
@Service
public class ParticipationService {
    @Autowired private ParticipationRepository participationRepository;
    @Autowired private PersonneRepository personneRepository;
    @Autowired private ProjetRepository projetRepository;

    public Participation ajouterParticipation(Integer matricule, Integer code, String role, Float pourcentage) {
        Personne personne = personneRepository.findById(matricule).orElseThrow();
        Projet projet = projetRepository.findById(code).orElseThrow();

        if (participationRepository.existsByPersonneAndProjet(personne, projet)) {
            throw new IllegalStateException("Cette personne participe déjà à ce projet.");
        }
        
        if (participationRepository.countByPersonneAndProjetFinIsNull(personne) >= 3) {
            throw new IllegalStateException("Une personne ne peut pas participer à plus de 3 projets en même temps.");
        }
        
        Float totalOccupation = participationRepository.totalOccupation(personne);
        if (totalOccupation + pourcentage > 100) {
            throw new IllegalStateException("Une personne ne peut pas être occupée à plus de 100%.");
        }
        
        Participation participation = new Participation();
        participation.setPersonne(personne);
        participation.setProjet(projet);
        participation.setRole(role);
        participation.setPourcentage(pourcentage);
        
        return participationRepository.save(participation);
    }
}

@SpringBootTest
@RunWith(SpringRunner.class)
public class ParticipationServiceTest {
    @Autowired private ParticipationService participationService;
    @Autowired private PersonneRepository personneRepository;
    @Autowired private ProjetRepository projetRepository;
    @Autowired private ParticipationRepository participationRepository;

    @BeforeEach
    void setup() {
        participationRepository.deleteAll();
        personneRepository.save(new Personne(1, "Doe", "John", "Dev"));
        projetRepository.save(new Projet(1, "Projet A", LocalDate.now(), null));
    }

    @Test
    void testAjoutParticipation() {
        assertDoesNotThrow(() -> participationService.ajouterParticipation(1, 1, "Dev", 50f));
    }
}
