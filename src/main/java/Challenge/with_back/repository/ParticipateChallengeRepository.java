package Challenge.with_back.repository;

import Challenge.with_back.entity.ParticipateChallenge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipateChallengeRepository extends JpaRepository<ParticipateChallenge, Long> {
}
