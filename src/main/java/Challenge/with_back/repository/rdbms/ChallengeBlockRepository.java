package Challenge.with_back.repository.rdbms;

import Challenge.with_back.entity.rdbms.ChallengeBlock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeBlockRepository extends JpaRepository<ChallengeBlock, Long> {
}
