package Challenge.with_back.domain.update_participate_phase;

import Challenge.with_back.common.entity.ParticipatePhase;
import Challenge.with_back.common.entity.User;

public interface UpdateParticipatePhaseStrategy
{
    void updateParticipatePhase(User user, ParticipatePhase participatePhase, Object data);
}
