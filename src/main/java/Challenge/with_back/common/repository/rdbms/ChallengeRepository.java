package Challenge.with_back.common.repository.rdbms;

import Challenge.with_back.common.entity.rdbms.Challenge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, Long>
{
    // 공개 챌린지 페이지 조회
    Page<Challenge> findPageByPublicTrue(Pageable pageable);
}
