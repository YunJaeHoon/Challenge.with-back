package Challenge.with_back.domain.challenge.update_participate_phase;

import Challenge.with_back.common.entity.rdbms.ParticipatePhase;
import Challenge.with_back.common.entity.rdbms.User;

public interface UpdateParticipatePhaseStrategy
{
    void updateParticipatePhase(User user, ParticipatePhase participatePhase, Object data);
}
