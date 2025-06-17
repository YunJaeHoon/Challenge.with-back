package Challenge.with_back.common.repository.rdbms;

import Challenge.with_back.common.entity.rdbms.ChallengeBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChallengeBlockRepository extends JpaRepository<ChallengeBlock, Long>
{
    Optional<ChallengeBlock> findByUserIdAndChallengeId(Long user, Long challengeId);
}
