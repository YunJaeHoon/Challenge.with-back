package Challenge.with_back.common.repository.rdbms;

import Challenge.with_back.common.entity.rdbms.InviteChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InviteChallengeRepository extends JpaRepository<InviteChallenge, Long> {
}
