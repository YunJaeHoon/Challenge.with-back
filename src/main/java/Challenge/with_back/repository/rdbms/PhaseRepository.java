package Challenge.with_back.repository.rdbms;

import Challenge.with_back.entity.rdbms.Challenge;
import Challenge.with_back.entity.rdbms.Phase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PhaseRepository extends JpaRepository<Phase, Long>
{
    // 챌린지와 번호로 특정 페이즈 찾기
    Optional<Phase> findByChallengeAndNumber(Challenge challenge, int number);
}
