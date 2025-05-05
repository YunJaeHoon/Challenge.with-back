package Challenge.with_back.common.repository.rdbms;

import Challenge.with_back.common.entity.rdbms.ParticipatePhase;
import Challenge.with_back.common.entity.rdbms.Phase;
import Challenge.with_back.common.entity.rdbms.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParticipatePhaseRepository extends JpaRepository<ParticipatePhase, Long>
{
    Optional<ParticipatePhase> findByPhaseAndUser(Phase phase, User user);
}
