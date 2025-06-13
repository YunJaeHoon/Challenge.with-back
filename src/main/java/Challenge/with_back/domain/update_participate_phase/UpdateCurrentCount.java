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

@Component("UPDATE_CURRENT_COUNT")
@RequiredArgsConstructor
public class UpdateCurrentCount implements UpdateParticipatePhaseStrategy
{
    private final ParticipateChallengeRepository participateChallengeRepository;
    private final ParticipatePhaseRepository participatePhaseRepository;
    private final ChallengeValidator challengeValidator;

    // 현재 달성 개수 변경
    @Override
    public void updateParticipatePhase(User user, ParticipatePhase participatePhase, Object data) throws CustomException
    {
        // 달성 개수
        int value = (int) data;

        // 페이즈 참여 정보가 해당 사용자 것인지 확인
        if(!participatePhase.getUser().getId().equals(user.getId()))
            throw new CustomException(CustomExceptionCode.PARTICIPATE_PHASE_NOT_OWNED, null);

        // 챌린지
        Challenge challenge = participatePhase.getPhase().getChallenge();

        // 현재 달성 개수가 범위를 벗어나면 예외 처리
        challengeValidator.checkCurrentCount(value, challenge);

        // 기존 달성 개수
        int originalValue = participatePhase.getCurrentCount();

        // 현재 달성 개수 변경
        participatePhase.updateCurrentCount(value);

        // 기존에 목표 개수를 달성하지 못했지만 새롭게 목표 개수에 달성했다면, 챌린지 참여 정보에서 성공 개수 1 증가
        // 기존에 목표 개수를 달성했지만 새롭게 목표 개수에 달성하지 못했다면, 챌린지 참여 정보에서 성공 개수 1 감소
        if(originalValue < challenge.getGoalCount() && value == challenge.getGoalCount())
        {
            // 챌린지 참여 정보
            ParticipateChallenge participateChallenge = participateChallengeRepository.findByUserAndChallenge(user, challenge)
                    .orElseThrow(() -> new CustomException(CustomExceptionCode.PARTICIPATE_CHALLENGE_NOT_FOUND, challenge.getId()));

            participateChallenge.increaseCountSuccess();
            participateChallengeRepository.save(participateChallenge);
        }
        else if(originalValue == challenge.getGoalCount() && value < challenge.getGoalCount())
        {
            // 챌린지 참여 정보
            ParticipateChallenge participateChallenge = participateChallengeRepository.findByUserAndChallenge(user, challenge)
                    .orElseThrow(() -> new CustomException(CustomExceptionCode.PARTICIPATE_CHALLENGE_NOT_FOUND, challenge.getId()));

            participateChallenge.decreaseCountSuccess();
            participateChallengeRepository.save(participateChallenge);
        }

        // 변경 사항 저장
        participatePhaseRepository.save(participatePhase);
    }
}
