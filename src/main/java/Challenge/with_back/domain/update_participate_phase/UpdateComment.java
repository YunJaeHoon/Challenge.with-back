package Challenge.with_back.domain.update_participate_phase;

import Challenge.with_back.common.entity.rdbms.ParticipatePhase;
import Challenge.with_back.common.entity.rdbms.User;
import Challenge.with_back.common.repository.rdbms.ParticipatePhaseRepository;
import Challenge.with_back.common.exception.CustomException;
import Challenge.with_back.domain.challenge.util.ChallengeValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("UPDATE_COMMENT")
@RequiredArgsConstructor
public class UpdateComment implements UpdateParticipatePhaseStrategy
{
    private final ParticipatePhaseRepository participatePhaseRepository;
    private final ChallengeValidator challengeValidator;

    // 한마디 변경
    @Override
    public void updateParticipatePhase(User user, ParticipatePhase participatePhase, Object data) throws CustomException
    {
        // 한마디
        String comment = (String) data;

        // 페이즈 참여 정보가 해당 사용자 것인지 확인
        challengeValidator.checkParticipatePhaseOwnership(user, participatePhase);

        // 한마디 길이 체크
        challengeValidator.checkParticipatePhaseCommentLength(comment);

        // 한마디 수정
        participatePhase.updateComment(comment);

        // 챌린지 및 챌린지 참여 정보 마지막 활동 날짜 갱신
        challengeValidator.renewLastActiveDate(participatePhase);

        participatePhaseRepository.save(participatePhase);
    }
}
