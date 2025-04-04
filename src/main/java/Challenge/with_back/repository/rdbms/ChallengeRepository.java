package Challenge.with_back.repository.rdbms;

import Challenge.with_back.entity.rdbms.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
}
