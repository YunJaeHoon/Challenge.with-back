package Challenge.with_back.common.repository.rdbms;

import Challenge.with_back.common.entity.rdbms.ChallengeBlock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeBlockRepository extends JpaRepository<ChallengeBlock, Long> {
}
