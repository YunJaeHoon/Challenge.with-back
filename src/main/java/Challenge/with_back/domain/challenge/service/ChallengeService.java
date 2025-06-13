package Challenge.with_back.domain.challenge.service;

import Challenge.with_back.common.entity.rdbms.*;
import Challenge.with_back.common.enums.ChallengeColorTheme;
import Challenge.with_back.common.repository.rdbms.*;
import Challenge.with_back.common.enums.ChallengeRole;
import Challenge.with_back.common.enums.ChallengeUnit;
import Challenge.with_back.common.exception.CustomException;
import Challenge.with_back.common.exception.CustomExceptionCode;
import Challenge.with_back.domain.challenge.dto.CreateChallengeDto;
import Challenge.with_back.domain.challenge.dto.GetMyChallengeDto;
import Challenge.with_back.domain.challenge.util.ChallengeValidator;
import Challenge.with_back.domain.evidence_photo.S3EvidencePhotoManager;
import Challenge.with_back.domain.update_participate_phase.UpdateParticipatePhaseStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ChallengeService
{
    private final UserRepository userRepository;
    private final ChallengeRepository challengeRepository;
    private final PhaseRepository phaseRepository;
    private final ParticipateChallengeRepository participateChallengeRepository;
    private final ParticipatePhaseRepository participatePhaseRepository;
    private final EvidencePhotoRepository evidencePhotoRepository;
    private final FriendRepository friendRepository;
    private final InviteChallengeRepository inviteChallengeRepository;

    private final ChallengeValidator challengeValidator;

    private final S3EvidencePhotoManager s3EvidencePhotoManager;

    /// 서비스

    // 챌린지 생성
    @Transactional
    public void createChallenge(CreateChallengeDto createChallengeDto, User user)
    {
        /// 형식 체크
        /// 1. 챌린지 색상 코드 및 단위 추출
        /// 2. 챌린지 이름 및 설명 길이 체크
        /// 3. 챌린지 목표 개수 크기 체크
        /// 4. 이미 사용자가 최대 개수로 챌린지를 참여하고 있는지 확인
        /// 5. 본인과 초대한 사용자들의 인원수가 챌린지 최대 참여자 인원수를 초과했는지 확인

        /// 1

        // 챌린지 색상 코드, 단위 추출
        ChallengeColorTheme colorTheme = challengeValidator.getColor(createChallengeDto.getColorTheme());
        ChallengeUnit unit = challengeValidator.getUnit(createChallengeDto.getUnit());

        /// 2

        // 챌린지 이름 길이 체크
        challengeValidator.checkNameLength(createChallengeDto.getName());

        // 챌린지 설명 길이 체크
        if(createChallengeDto.getDescription() != null)
            challengeValidator.checkDescriptionLength(createChallengeDto.getDescription());

        /// 3

        // 챌린지 목표 개수 크기 체크
        challengeValidator.checkGoalCount(createChallengeDto.getGoalCount());

        /// 4

        // 이미 사용자가 최대 개수로 챌린지를 참여하고 있는지 확인
        if(isParticipatingInMaxChallenges(user))
            throw new CustomException(CustomExceptionCode.TOO_MANY_PARTICIPATE_CHALLENGE, null);

        /// 5

        // 챌린지 최대 참여자 인원수
        int maxParticipantCount = createChallengeDto.getIsAlone() ? 1 :
                user.isPremium() ? 100 : 5;

        // 초대한 사용자 리스트
        List<User> inviteUserList = new ArrayList<>();

        createChallengeDto.getInviteUserIdList().forEach(inviteUserId -> {

            // 초대한 사용자 정보
            Optional<User> InviteUserOptional = userRepository.findById(inviteUserId);

            // 존재하지 않는 사용자면 그냥 넘어감
            if(InviteUserOptional.isEmpty()) {
                return;
            }

            User inviteUser = InviteUserOptional.get();

            // 초대한 사용자가 최대 개수로 챌린지를 참여하고 있다면 그냥 넘어감
            if(isParticipatingInMaxChallenges(inviteUser)) {
                return;
            }

            // 본인과 초대한 사용자가 친구가 아니라면 그냥 넘어감
            if(friendRepository.findByUser1IdAndUser2Id(user.getId(), inviteUser.getId()).isEmpty()) {
                return;
            }

            inviteUserList.add(inviteUser);

        });

        // 참가자들의 인원수가 챌린지 최대 참여자 인원수보다 많으면 예외 처리
        if(inviteUserList.size() + 1 > maxParticipantCount)
            throw new CustomException(CustomExceptionCode.FULL_CHALLENGE, createChallengeDto.getInviteUserIdList().size() + 1);

        /// 챌린지 생성

        // 챌린지 생성
        Challenge challenge = Challenge.builder()
                .superAdmin(user)
                .icon(createChallengeDto.getIcon())
                .colorTheme(colorTheme)
                .name(createChallengeDto.getName().trim())
                .description(createChallengeDto.getDescription().trim())
                .goalCount(createChallengeDto.getGoalCount())
                .unit(unit)
                .isPublic(createChallengeDto.getIsPublic())
                .maxParticipantCount(maxParticipantCount)
                .countPhase(0)
                .lastActiveDate(LocalDate.now())
                .isFinished(false)
                .build();

        // 챌린지 저장
        challengeRepository.save(challenge);

        /// 챌린지 참여 데이터 생성

        // 챌린지 참여 데이터 생성
        ParticipateChallenge participateChallenge = ParticipateChallenge.builder()
                .user(user)
                .challenge(challenge)
                .determination("")
                .challengeRole(ChallengeRole.SUPER_ADMIN)
                .countSuccess(0)
                .countExemption(0)
                .isPublic(true)
                .lastActiveDate(LocalDate.now())
                .build();

        // 챌린지 참여 데이터 저장
        participateChallengeRepository.save(participateChallenge);

        /// 페이즈 생성

        // 페이즈 10개 생성
        createPhases(challenge, 10);

        /// 챌린지 초대 데이터 생성

        // 챌린지 초대 데이터 생성
        List<InviteChallenge> inviteChallengeList = inviteUserList.stream().map(inviteUser -> {
            return InviteChallenge.builder()
                    .sender(user)
                    .receiver(inviteUser)
                    .challenge(challenge)
                    .build();
        }).toList();

        // 챌린지 초대 데이터 저장
        inviteChallengeRepository.saveAll(inviteChallengeList);

        // TODO: 초대한 사용자들에게 챌린지 초대 알림 및 이메일 전송
    }

    // 챌린지 가입
    @Transactional
    public void joinChallenge(Challenge challenge, User user, ChallengeRole role)
    {
        // 공개 챌린지인지 확인
        if(!challenge.isPublic())
            throw new CustomException(CustomExceptionCode.PRIVATE_CHALLENGE, null);

        // 이미 챌린지에 참여자가 가득 찼는지 확인
        if(challenge.getMaxParticipantCount() == participateChallengeRepository.countAllByChallenge(challenge))
            throw new CustomException(CustomExceptionCode.FULL_CHALLENGE, null);

        // 이미 사용자가 최대 개수로 챌린지를 참여하고 있는지 확인
        if(isParticipatingInMaxChallenges(user))
            throw new CustomException(CustomExceptionCode.TOO_MANY_PARTICIPATE_CHALLENGE, null);

        // 이미 사용자가 해당 챌린지에 가입했는지 확인
        if(participateChallengeRepository.findByUserAndChallenge(user, challenge).isPresent())
            throw new CustomException(CustomExceptionCode.ALREADY_PARTICIPATING_CHALLENGE, null);

        // 챌린지 참여 정보 생성
        ParticipateChallenge participateChallenge = ParticipateChallenge.builder()
                .user(user)
                .challenge(challenge)
                .determination("")
                .challengeRole(role)
                .countSuccess(0)
                .countExemption(0)
                .isPublic(true)
                .lastActiveDate(LocalDate.now())
                .build();

        // 챌린지 참여 정보 저장
        participateChallengeRepository.save(participateChallenge);

        // TODO: 현재 페이즈 이후의 모든 페이즈에 대한 참여 정보를 생성해야 함.

        // 현재 페이즈 조회
        Phase phase = getCurrentPhase(challenge);

        // 페이즈 참여 정보 생성
        ParticipatePhase participatePhase = ParticipatePhase.builder()
                .user(user)
                .phase(phase)
                .currentCount(0)
                .isExempt(false)
                .comment("")
                .build();

        // 페이즈 참여 정보 저장
        participatePhaseRepository.save(participatePhase);
    }

    // 현재 진행 중인 내 챌린지 조회
    public GetMyChallengeDto getMyChallenges(User user)
    {
        // 참여 가능한 챌린지 최대 개수
        int maxChallengeCount = user.getMaxChallengeCount();

        // 모든 챌린지 참여 정보를 생성 날짜 내림차순으로 조회
        List<ParticipateChallenge> participateChallengeList = participateChallengeRepository.findAllOngoing(user);

        // 챌린지 참여 정보 리스트를 dto 리스트로 변경
        // 현재 진행 중인 챌린지만 필터링
        List<GetMyChallengeDto.ChallengeDto> challengeDtoList = participateChallengeList.stream()
                .map(participateChallenge -> {

                    // 챌린지
                    Challenge challenge = participateChallenge.getChallenge();

                    // 현재 페이즈
                    Phase phase = getCurrentPhase(challenge);

                    // 현재 페이즈 참여 정보
                    ParticipatePhase participatePhase = participatePhaseRepository.findByPhaseIdAndUserId(phase.getId(), user.getId())
                            .orElseThrow(() -> new CustomException(CustomExceptionCode.PARTICIPATE_PHASE_NOT_FOUND, null));

                    // 증거사진 리스트
                    List<EvidencePhoto> evidencePhotoList = evidencePhotoRepository.findAllByParticipatePhaseId(participatePhase.getId());

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
                            .colorTheme(challenge.getColorTheme().name())
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
                .countChallenge(participateChallengeList.size())
                .maxChallengeCount(maxChallengeCount)
                .challenges(challengeDtoList)
                .build();
    }

    // 챌린지 삭제
    public void deleteChallenge(Long challengeId)
    {
        /// 증거사진을 S3에서 삭제

        // 챌린지 데이터 조회
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.CHALLENGE_NOT_FOUND, null));

        // 페이즈 리스트 조회
        List<Phase> phaseList = phaseRepository.findAllByChallengeId(challenge.getId());

        phaseList.forEach(phase -> {

            // 페이즈 참가 데이터 리스트 조회
            List<ParticipatePhase> participatePhaseList = participatePhaseRepository.findAllByPhaseId(phase.getId());

            participatePhaseList.forEach(participatePhase -> {

                // 증거사진 데이터 리스트 조회
                List<EvidencePhoto> evidencePhotoList = evidencePhotoRepository.findAllByParticipatePhaseId(participatePhase.getId());

                // S3에서 증거사진 삭제
                evidencePhotoList.forEach(evidencePhoto -> {
                    s3EvidencePhotoManager.delete(evidencePhoto.getFilename());
                });

            });

        });

        /// 챌린지 삭제

        // 챌린지 데이터 삭제
        challengeRepository.delete(challenge);
    }

    /// 공통 로직

    // 사용자가 참여 중인 챌린지 개수가 최대인지 확인
    public boolean isParticipatingInMaxChallenges(User user)
    {
        return participateChallengeRepository.countAllOngoing(user) >= user.getMaxChallengeCount();
    }

    // 현재 페이즈 조회
    public Phase getCurrentPhase(Challenge challenge)
    {
        return phaseRepository.findByChallengeIdAndNumber(challenge.getId(), challenge.calcCurrentPhaseNumber())
                .orElseThrow(() -> new CustomException(CustomExceptionCode.PHASE_NOT_FOUND, null));
    }

    public void createPhases(Challenge challenge, int count)
    {
        /// 생성할 페이즈 개수만큼 다음을 수행
        /// 1. 챌린지의 페이즈 개수 1 증가
        /// 2. 페이즈 생성
        /// 3. 페이즈 참여 정보 생성

        // 챌린지 참여 정보 리스트
        List<ParticipateChallenge> participateChallengeList = participateChallengeRepository.findAllByChallengeOrderByCreatedAtDesc(challenge);

        // 페이즈 리스트
        List<Phase> phaseList = new ArrayList<>();

        // 페이즈 참여 정보 리스트
        List<ParticipatePhase> participatePhaseList = new ArrayList<>();

        // 생성할 페이즈 개수만큼 반복
        for(int i = 0; i < count; i++)
        {
            // 챌린지의 페이즈 개수 증가
            challenge.increaseCountPhase();

            // 페이즈 시작 날짜 및 종료 날짜 계산
            LocalDate startDate = challenge.calcPhaseStartDate(challenge.getCountPhase());
            LocalDate endDate = challenge.getUnit().calcPhaseEndDate(startDate);

            // 페이즈 생성
            Phase phase = Phase.builder()
                    .challenge(challenge)
                    .name(challenge.getCountPhase() + "번째 페이즈")
                    .description("")
                    .number(challenge.getCountPhase())
                    .startDate(startDate)
                    .endDate(endDate)
                    .build();

            phaseList.add(phase);

            // 페이즈 참여 정보 생성
            participateChallengeList.forEach(participateChallenge -> {
                ParticipatePhase participatePhase = ParticipatePhase.builder()
                        .user(participateChallenge.getUser())
                        .phase(phase)
                        .currentCount(0)
                        .isExempt(false)
                        .comment("")
                        .build();

                participatePhaseList.add(participatePhase);
            });
        }

        challengeRepository.save(challenge);
        phaseRepository.saveAll(phaseList);
        participatePhaseRepository.saveAll(participatePhaseList);
    }
}
