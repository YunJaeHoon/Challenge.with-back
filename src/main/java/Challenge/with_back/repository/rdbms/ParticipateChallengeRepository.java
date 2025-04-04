package Challenge.with_back.repository.rdbms;

import Challenge.with_back.entity.rdbms.ParticipateChallenge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipateChallengeRepository extends JpaRepository<ParticipateChallenge, Long> {
}
