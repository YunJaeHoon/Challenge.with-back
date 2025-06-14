package Challenge.with_back.domain.update_participate_phase;

import Challenge.with_back.common.entity.rdbms.Challenge;
import Challenge.with_back.common.entity.rdbms.ParticipateChallenge;
import Challenge.with_back.common.entity.rdbms.ParticipatePhase;
import Challenge.with_back.common.entity.rdbms.User;
import Challenge.with_back.common.repository.rdbms.ParticipateChallengeRepository;
import Challenge.with_back.common.repository.rdbms.ParticipatePhaseRepository;
import Challenge.with_back.common.exception.CustomException;
import Challenge.with_back.common.exception.CustomExceptionCode;
import Challenge.with_back.domain.challenge.util.ChallengeValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("TOGGLE_IS_EXEMPT")
@RequiredArgsConstructor
public class ToggleIsExempt implements UpdateParticipatePhaseStrategy
{
    private final ParticipateChallengeRepository participateChallengeRepository;

    // 면제 여부 토글
    @Override
    @Transactional
    public void updateParticipatePhase(User user, ParticipatePhase participatePhase, Object data) throws CustomException
    {
        // 페이즈 참여 정보가 해당 사용자 것인지 확인
        if(!participatePhase.getUser().getId().equals(user.getId()))
            throw new CustomException(CustomExceptionCode.PARTICIPATE_PHASE_NOT_OWNED, null);

        // 챌린지
        Challenge challenge = participatePhase.getPhase().getChallenge();

        // 챌린지 참여 정보
        ParticipateChallenge participateChallenge = participateChallengeRepository.findByUserAndChallenge(user, challenge)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.PARTICIPATE_CHALLENGE_NOT_FOUND, challenge.getId()));

        // 기존 면제 여부 값에 따라 챌린지 참여 정보의 면제 개수 갱신
        if(participatePhase.isExempt())
            participateChallenge.decreaseCountExemption();
        else
            participateChallenge.increaseCountExemption();

        // 면제 여부 토글
        participatePhase.toggleIsExempt();
    }
}
