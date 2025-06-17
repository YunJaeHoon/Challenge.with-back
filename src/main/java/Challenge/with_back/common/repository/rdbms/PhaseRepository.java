package Challenge.with_back.common.repository.rdbms;

import Challenge.with_back.common.entity.rdbms.Phase;
import Challenge.with_back.common.entity.rdbms.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhaseRepository extends JpaRepository<Phase, Long>
{
    // 챌린지 ID로 페이즈 데이터 리스트 조회
    List<Phase> findAllByChallengeId(Long challengeId);

    // 챌린지 ID, 번호로 특정 페이즈 데이터 조회
    Optional<Phase> findByChallengeIdAndNumber(Long challengeId, int number);

    // 특정 번호 이상의 페이즈 리스트 조회
    @Query("SELECT p FROM Phase p WHERE p.challenge.id = :challengeId AND p.number >= :number")
    List<Phase> findAllFromNumber(Long challengeId, int number);
}
