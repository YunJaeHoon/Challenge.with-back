package Challenge.with_back.domain.challenge.service;

import Challenge.with_back.common.entity.rdbms.*;
import Challenge.with_back.common.enums.UpdateParticipatePhaseType;
import Challenge.with_back.common.repository.rdbms.*;
import Challenge.with_back.domain.challenge.dto.EvidencePhotoDto;
import Challenge.with_back.domain.challenge.dto.UpdateParticipatePhaseMessage;
import Challenge.with_back.domain.update_participate_phase.UpdateParticipatePhaseStrategy;
import Challenge.with_back.domain.evidence_photo.S3EvidencePhoto;
import Challenge.with_back.domain.evidence_photo.S3EvidencePhotoManager;
import Challenge.with_back.common.enums.ChallengeRole;
import Challenge.with_back.common.enums.ChallengeUnit;
import Challenge.with_back.common.response.exception.CustomException;
import Challenge.with_back.common.response.exception.CustomExceptionCode;
import Challenge.with_back.domain.account.util.AccountUtil;
import Challenge.with_back.domain.challenge.dto.CreateChallengeDto;
import Challenge.with_back.domain.challenge.dto.GetMyChallengeDto;
import Challenge.with_back.domain.challenge.util.ChallengeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ChallengeService
{
    private final UserRepository userRepository;
    private final ChallengeRepository challengeRepository;
    private final ParticipateChallengeRepository participateChallengeRepository;
    private final ParticipatePhaseRepository participatePhaseRepository;
    private final EvidencePhotoRepository evidencePhotoRepository;

    private final AccountUtil accountUtil;
    private final ChallengeUtil challengeUtil;

    // 챌린지 생성
    @Transactional
    public void createChallenge(CreateChallengeDto createChallengeDto, User user)
    {
        // 챌린지 색상 코드, 단위 추출
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

        // 이미 사용자가 최대 개수로 챌린지를 참여하고 있는지 확인
        if(accountUtil.isParticipatingInMaxChallenges(user))
            throw new CustomException(CustomExceptionCode.TOO_MANY_PARTICIPATE_CHALLENGE, null);

        // 참가자 리스트
        List<User> participantList = new ArrayList<>();
        participantList.add(user);

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

            participantList.add(inviteUser);
        });

        // 참가자들의 인원수가 챌린지 최대 참여자 인원수보다 많으면 예외 처리
        if(participantList.size() > maxParticipantCount)
            throw new CustomException(CustomExceptionCode.FULL_CHALLENGE, createChallengeDto.getInviteUserIdList().size() + 1);

        // 챌린지 생성
        Challenge challenge = Challenge.builder()
                .superAdmin(user)
                .icon(createChallengeDto.getIcon())
                .colorTheme(colorTheme)
                .name(createChallengeDto.getName())
                .description(createChallengeDto.getDescription())
                .goalCount(createChallengeDto.getGoalCount())
                .unit(unit)
                .isPublic(createChallengeDto.getIsPublic())
                .maxParticipantCount(maxParticipantCount)
                .countPhase(0)
                .lastActiveDate(LocalDate.now())
                .isFinished(false)
                .build();

        // 챌린지 참여 정보 리스트
        List<ParticipateChallenge> participateChallengeList = new ArrayList<>();

        // 챌린지 참여 정보 생성
        participantList.forEach(participant -> {
            ParticipateChallenge participateChallenge = ParticipateChallenge.builder()
                    .user(participant)
                    .challenge(challenge)
                    .determination("")
                    .challengeRole(Objects.equals(participant, user) ? ChallengeRole.SUPER_ADMIN : ChallengeRole.USER)
                    .countSuccess(0)
                    .countExemption(0)
                    .isPublic(true)
                    .lastActiveDate(LocalDate.now())
                    .build();

            participateChallengeList.add(participateChallenge);
        });

        userRepository.saveAll(participantList);
        challengeRepository.save(challenge);
        participateChallengeRepository.saveAll(participateChallengeList);

        // 페이즈 10개 생성
        challengeUtil.createPhases(challenge, 10);
    }

    // 현재 진행 중인 내 챌린지 조회
    public GetMyChallengeDto getMyChallenges(User user)
    {
        // 참여 중인 챌린지 개수
        int countChallenge = participateChallengeRepository.countAllOngoing(user);

        // 챌린지 개수 상한값
        int maxChallengeCount = accountUtil.getMaxChallengeCount(user);

        // 모든 챌린지 참여 정보를 생성 날짜 내림차순으로 조회
        List<ParticipateChallenge> participateChallengeList = participateChallengeRepository.findAllOngoing(user);

        // 챌린지 참여 정보 리스트를 dto 리스트로 변경
        // 현재 진행 중인 챌린지만 필터링
        List<GetMyChallengeDto.ChallengeDto> challengeDtoList = participateChallengeList.stream()
                .map(participateChallenge -> {

                    // 챌린지
                    Challenge challenge = participateChallenge.getChallenge();

                    // 현재 페이즈
                    Phase phase = challengeUtil.getCurrentPhase(challenge);

                    // 현재 페이즈 참여 정보
                    ParticipatePhase participatePhase = participatePhaseRepository.findByPhaseAndUser(phase, user)
                            .orElseThrow(() -> new CustomException(CustomExceptionCode.PARTICIPATE_PHASE_NOT_FOUND, null));

                    // 증거사진 리스트
                    List<EvidencePhoto> evidencePhotoList = evidencePhotoRepository.findAllByParticipatePhase(participatePhase);

                    // 증거사진 리스트를 증거사진 dto 리스트로 변경
                    List<GetMyChallengeDto.EvidencePhotoDto> evidencePhotos = evidencePhotoList.stream()
                            .map(evidencePhoto -> {
                                return GetMyChallengeDto.EvidencePhotoDto.builder()
                                        .evidencePhotoId(evidencePhoto.getId())
                                        .url(evidencePhoto.getPhotoUrl())
                                        .build();
                            })
                            .toList();

                    // 증거사진 최대 개수
                    long maxEvidencePhotoCount = ChronoUnit.DAYS.between(phase.getStartDate(), phase.getEndDate()) + 1;

                    return GetMyChallengeDto.ChallengeDto.builder()
                            .challengeId(challenge.getId())
                            .superAdminId(challenge.getSuperAdmin().getId())
                            .icon(challenge.getIcon())
                            .colorTheme(challenge.getColorTheme())
                            .challengeName(challenge.getName())
                            .challengeDescription(challenge.getDescription())
                            .maxParticipantCount(challenge.getMaxParticipantCount())
                            .goalCount(challenge.getGoalCount())
                            .unit(challenge.getUnit().name())
                            .challengeStartDate(challenge.getCreatedAt().toLocalDate())
                            .participateCurrentPhaseId(participatePhase.getId())
                            .currentPhaseNumber(phase.getNumber())
                            .currentPhaseStartDate(phase.getStartDate())
                            .currentPhaseEndDate(phase.getEndDate())
                            .currentPhaseName(phase.getName())
                            .currentPhaseDescription(phase.getDescription())
                            .completeCount(participatePhase.getCurrentCount())
                            .isExempt(participatePhase.isExempt())
                            .comment(participatePhase.getComment())
                            .maxEvidencePhotoCount(maxEvidencePhotoCount)
                            .evidencePhotos(evidencePhotos)
                            .build();
                })
                .toList();

        return GetMyChallengeDto.builder()
                .countChallenge(countChallenge)
                .maxChallengeCount(maxChallengeCount)
                .challenges(challengeDtoList)
                .build();
    }
}
