package ssii.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;

@DataJpaTest
public class PersonneRepositoryTest {
    @Autowired
    private PersonneRepository clientDao;


    
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


    @Test
    void daoExiste() {
        assertNotNull(clientDao);
    }
}




