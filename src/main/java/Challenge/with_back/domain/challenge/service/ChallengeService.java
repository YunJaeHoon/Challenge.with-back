package Challenge.with_back.domain.challenge.service;

import Challenge.with_back.common.enums.ChallengeRole;
import Challenge.with_back.common.enums.ChallengeUnit;
import Challenge.with_back.common.response.exception.CustomException;
import Challenge.with_back.common.response.exception.CustomExceptionCode;
import Challenge.with_back.domain.account.util.AccountUtil;
import Challenge.with_back.domain.challenge.dto.CreateChallengeDto;
import Challenge.with_back.domain.challenge.util.ChallengeUtil;
import Challenge.with_back.entity.rdbms.*;
import Challenge.with_back.repository.rdbms.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChallengeService
{
    private final UserRepository userRepository;
    private final ChallengeRepository challengeRepository;
    private final ParticipateChallengeRepository participateChallengeRepository;
    private final PhaseRepository phaseRepository;
    private final ParticipatePhaseRepository participatePhaseRepository;

    private final AccountUtil accountUtil;
    private final ChallengeUtil challengeUtil;

    // 챌린지 생성
    @Transactional
    public void createChallenge(CreateChallengeDto createChallengeDto, User user)
    {
        // 챌린지 아이콘 URL, 색상 코드, 단위 추출
        String iconUrl = challengeUtil.getIconUrl(createChallengeDto.getIcon());
        String colorTheme = challengeUtil.getColor(createChallengeDto.getColorTheme());
        ChallengeUnit unit = challengeUtil.getUnit(createChallengeDto.getUnit());

        // 챌린지 이름 및 설명 길이 체크
        challengeUtil.checkNameLength(createChallengeDto.getName());
        challengeUtil.checkDescriptionLength(createChallengeDto.getDescription());

        // 챌린지 목표 개수 크기 체크
        challengeUtil.checkGoalCount(createChallengeDto.getGoalCount());

        // 챌린지 최대 참여자 인원수
        int maxParticipantCount = createChallengeDto.getIsAlone() ? 1 :
                accountUtil.isPremium(user) ? 100 : 5;

        // 본인과 초대한 사람들의 인원수가 챌린지 최대 참여자 인원수보다 많으면 예외 처리
        if(createChallengeDto.getInviteUserIdList().size() + 1 > maxParticipantCount)
            throw new CustomException(CustomExceptionCode.FULL_CHALLENGE, createChallengeDto.getInviteUserIdList().size() + 1);

        // 챌린지 생성
        Challenge challenge = Challenge.builder()
                .superAdmin(user)
                .iconUrl(iconUrl)
                .colorTheme(colorTheme)
                .name(createChallengeDto.getName())
                .description(createChallengeDto.getDescription())
                .goalCount(createChallengeDto.getGoalCount())
                .unit(unit)
                .isPublic(createChallengeDto.getIsPublic())
                .maxParticipantCount(maxParticipantCount)
                .countCurrentParticipant(0)
                .countPhase(0)
                .lastActiveDate(LocalDate.now())
                .build();

        // 챌린지 저장
        challengeRepository.save(challenge);

        // 페이즈 생성 및 저장
        challengeUtil.createPhase(challenge);

        // 본인을 챌린지에 가입시키기
        challengeUtil.joinChallenge(challenge, user, ChallengeRole.SUPER_ADMIN);

        // 초대한 사용자 리스트
        List<User> inviteUserList = new ArrayList<>();

        createChallengeDto.getInviteUserIdList().forEach(inviteUserId -> {

            // 사용자 정보 확인
            Optional<User> InviteUserOptional = userRepository.findById(inviteUserId);

            // 존재하지 않는 사용자면 그냥 넘어감
            if(InviteUserOptional.isEmpty())
                return;

            User inviteUser = InviteUserOptional.get();

            // 최대 개수로 챌린지를 참여하고 있다면 그냥 넘어감
            if(accountUtil.isParticipatingInMaxChallenges(inviteUser))
                return;

            inviteUserList.add(inviteUser);
        });

        // 초대한 사용자들을 챌린지에 가입시키기
        inviteUserList.forEach(inviteUser -> {
            challengeUtil.joinChallenge(challenge, inviteUser, ChallengeRole.USER);
        });
    }
}
