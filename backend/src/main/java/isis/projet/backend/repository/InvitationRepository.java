package isis.projet.backend.repository;

import isis.projet.backend.entity.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Integer> {
    List<Invitation> findBySenderId(Integer senderId);

    List<Invitation> findByRecipientId(Integer recipientId);

    List<Invitation> findByGroupId(Integer groupId);

    List<Invitation> findByStatus(String status);
}