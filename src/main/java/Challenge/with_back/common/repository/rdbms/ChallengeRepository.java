package Challenge.with_back.common.repository.rdbms;

import Challenge.with_back.common.entity.rdbms.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
}
