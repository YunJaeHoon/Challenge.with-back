package Challenge.with_back.common.repository.rdbms;

import Challenge.with_back.common.entity.rdbms.Challenge;
import Challenge.with_back.common.entity.rdbms.ParticipateChallenge;
import Challenge.with_back.common.entity.rdbms.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ParticipateChallengeRepository extends JpaRepository<ParticipateChallenge, Long>
{
    // 특정 사용자와 챌린지로 특정 챌린지 참여 정보 조회
    Optional<ParticipateChallenge> findByUserAndChallenge(User user, Challenge challenge);

    // 특정 사용자로 현재 진행 중인 챌린지 참여 정보를 생성 날짜 내림차순으로 조회
    @Query("""
        SELECT pc
        FROM ParticipateChallenge pc
        WHERE pc.user = :user AND pc.challenge.isFinished = false
        ORDER BY pc.createdAt DESC
    """)
    List<ParticipateChallenge> findAllOngoing(User user);

    // 특정 사용자로 현재 진행 중인 챌린지 참여 정보 개수 세기
    @Query("""
        SELECT count(pc)
        FROM ParticipateChallenge pc
        WHERE pc.user = :user AND pc.challenge.isFinished = false
    """)
    int countAllOngoing(User user);

    // 특정 챌린지로 모든 챌린지 참여 정보를 생성 날짜 내림차순으로 조회
    List<ParticipateChallenge> findAllByChallengeOrderByCreatedAtDesc(Challenge challenge);

    // 특정 챌린지로 모든 챌린지 참여 정보의 개수 세기
    int countAllByChallenge(Challenge challenge);
}
