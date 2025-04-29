package Challenge.with_back.repository.rdbms;

import Challenge.with_back.entity.rdbms.Challenge;
import Challenge.with_back.entity.rdbms.ParticipateChallenge;
import Challenge.with_back.entity.rdbms.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParticipateChallengeRepository extends JpaRepository<ParticipateChallenge, Long>
{
    // 특정 사용자와 챌린지로 특정 챌린지 참여 정보 조회
    Optional<ParticipateChallenge> findByUserAndChallenge(User user, Challenge challenge);

    // 특정 사용자로 모든 챌린지 참여 정보를 생성 날짜 내림차순으로 조회
    List<ParticipateChallenge> findAllByUserOrderByCreatedAtDesc(User user);

    // 특정 챌린지로 모든 챌린지 참여 정보를 생성 날짜 내림차순으로 조회
    List<ParticipateChallenge> findAllByChallengeOrderByCreatedAtDesc(Challenge challenge);
}
