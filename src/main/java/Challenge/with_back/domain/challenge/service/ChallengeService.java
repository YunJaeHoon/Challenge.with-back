package Challenge.with_back.domain.challenge.service;

import Challenge.with_back.common.entity.rdbms.*;
import Challenge.with_back.common.enums.AccountRole;
import Challenge.with_back.common.enums.ChallengeColorTheme;
import Challenge.with_back.common.repository.rdbms.*;
import Challenge.with_back.common.enums.ChallengeRole;
import Challenge.with_back.common.enums.ChallengeUnit;
import Challenge.with_back.common.exception.CustomException;
import Challenge.with_back.common.exception.CustomExceptionCode;
import Challenge.with_back.domain.account.service.AccountService;
import Challenge.with_back.domain.challenge.dto.*;
import Challenge.with_back.domain.challenge.util.ChallengeValidator;
import Challenge.with_back.domain.evidence_photo.S3EvidencePhotoManager;
import Challenge.with_back.domain.notification.InviteChallengeNotificationFactory;
import Challenge.with_back.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChallengeService
{
    private final UserRepository userRepository;
    private final ChallengeRepository challengeRepository;
    private final ChallengeBlockRepository challengeBlockRepository;
    private final PhaseRepository phaseRepository;
    private final ParticipateChallengeRepository participateChallengeRepository;
    private final ParticipatePhaseRepository participatePhaseRepository;
    private final FriendRepository friendRepository;
    private final EvidencePhotoRepository evidencePhotoRepository;
    private final InviteChallengeRepository inviteChallengeRepository;

    private final AccountService accountService;
    private final NotificationService notificationService;

    private final ChallengeValidator challengeValidator;

    private final InviteChallengeNotificationFactory inviteChallengeNotificationFactory;

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
        if(accountService.isParticipatingInMaxChallenges(user))
            throw new CustomException(CustomExceptionCode.TOO_MANY_PARTICIPATE_CHALLENGE, null);

        /// 5

        // 챌린지 최대 참여자 인원수
        int maxParticipantCount = createChallengeDto.getIsAlone() ? 1 :
                user.isPremium() ? 100 : 5;

        // 초대한 사용자 리스트
        List<User> invitedUserList = userRepository.findAllById(createChallengeDto.getInviteUserIdList());

        // 필터링
        // 1. 초대한 사용자가 최대 개수로 챌린지를 참여하고 있는지 확인
        // 2. 초대한 사용자와 친구 사이인지 확인
        invitedUserList = invitedUserList.stream().filter(invitedUser -> {

            // 1. 초대한 사용자가 최대 개수로 챌린지를 참여하고 있는지 확인
            if(accountService.isParticipatingInMaxChallenges(invitedUser)) {
                return false;
            }

            // 2. 초대한 사용자와 친구 사이인지 확인
            return friendRepository.findByUser1IdAndUser2Id(user.getId(), invitedUser.getId()).isPresent();

        }).toList();

        // 참가자들의 인원수가 챌린지 최대 참여자 인원수보다 많으면 예외 처리
        if(invitedUserList.size() + 1 > maxParticipantCount)
            throw new CustomException(CustomExceptionCode.FULL_CHALLENGE, createChallengeDto.getInviteUserIdList().size() + 1);

        /// 챌린지 생성

        // 챌린지 생성
        Challenge challenge = Challenge.builder()
                .superAdmin(user)
                .icon(createChallengeDto.getIcon())
                .colorTheme(colorTheme)
                .name(createChallengeDto.getName().trim())
                .description(createChallengeDto.getDescription() == null ? "" : createChallengeDto.getDescription().trim())
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

        /// 챌린지 초대

        // 챌린지 초대 데이터 생성 및 챌린지 초대 알림 전송
        inviteChallenge(user, invitedUserList, challenge);
    }

    // 챌린지 가입
    @Transactional
    public void joinChallenge(User user, Long challengeId, boolean isInvited)
    {
        /// 챌린지 조회

        // 챌린지 조회
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.CHALLENGE_NOT_FOUND, challengeId));

        /// 예외 처리
        /// 1. 초대된 것이 아니라면, 공개 챌린지인지 확인
        /// 2. 이미 챌린지에 참여자가 가득 찼는지 확인
        /// 3. 이미 사용자가 최대 개수로 챌린지를 참여하고 있는지 확인
        /// 4. 이미 사용자가 해당 챌린지에 가입했는지 확인
        /// 5. 사용자가 챌린지로부터 차단되었는지 확인

        // 1. 초대된 것이 아니라면, 공개 챌린지인지 확인
        if(!isInvited && !challenge.isPublic()) {
            throw new CustomException(CustomExceptionCode.PRIVATE_CHALLENGE, null);
        }

        // 2. 이미 챌린지에 참여자가 가득 찼는지 확인
        if(challenge.getMaxParticipantCount() == participateChallengeRepository.countAllByChallengeId(challengeId)) {
            throw new CustomException(CustomExceptionCode.FULL_CHALLENGE, null);
        }

        // 3. 이미 사용자가 최대 개수로 챌린지를 참여하고 있는지 확인
        if(accountService.isParticipatingInMaxChallenges(user)) {
            throw new CustomException(CustomExceptionCode.TOO_MANY_PARTICIPATE_CHALLENGE, null);
        }

        // 4. 이미 사용자가 해당 챌린지에 가입했는지 확인
        if(participateChallengeRepository.findByUserIdAndChallengeId(user.getId(), challengeId).isPresent()) {
            throw new CustomException(CustomExceptionCode.ALREADY_PARTICIPATING_CHALLENGE, null);
        }

        // 5. 사용자가 챌린지로부터 차단되었는지 확인
        if(challengeBlockRepository.findByUserIdAndChallengeId(user.getId(), challengeId).isPresent()) {
            throw new CustomException(CustomExceptionCode.ALREADY_BLOCKED_FROM_CHALLENGE, null);
        }

        /// 챌린지 참여 데이터 생성

        // 챌린지 참여 데이터 생성
        ParticipateChallenge participateChallenge = ParticipateChallenge.builder()
                .user(user)
                .challenge(challenge)
                .determination("")
                .challengeRole(ChallengeRole.USER)
                .countSuccess(0)
                .countExemption(0)
                .isPublic(true)
                .lastActiveDate(LocalDate.now())
                .build();

        // 챌린지 참여 정보 저장
        participateChallengeRepository.save(participateChallenge);

        /// 현재 페이즈 이상의 모든 페이즈에 대한 참여 데이터 생성

        // 현재 페이즈 이상의 모든 페이즈 리스트 조회
        List<Phase> phaseList = getPhaseListFromCurrent(challenge);

        // 페이즈 참여 데이터 리스트
        List<ParticipatePhase> participatePhaseList = new ArrayList<>();

        // 각각에 대해 페이즈 참여 데이터 생성
        phaseList.forEach(phase -> {
            ParticipatePhase participatePhase = ParticipatePhase.builder()
                    .user(user)
                    .phase(phase)
                    .currentCount(0)
                    .isExempt(false)
                    .comment("")
                    .build();

            participatePhaseList.add(participatePhase);
        });

        // 모든 페이즈 참여 데이터 저장
        participatePhaseRepository.saveAll(participatePhaseList);
    }

    // 챌린지 초대
    @Transactional
    public void inviteChallenge(User sender, List<Long> userIdList, Long challengeId)
    {
        /// 챌린지 조회

        // 챌린지 조회
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.CHALLENGE_NOT_FOUND, challengeId));

        /// 예외 처리
        /// 1. 초대하는 사용자가 해당 챌린지에 참여하고 있는지 확인
        /// 2. 초대하는 사용자가 챌린지의 관리자 이상의 권한을 가지고 있는지 확인

        // 챌린지 참여 데이터 조회
        ParticipateChallenge participateChallenge = participateChallengeRepository.findByUserIdAndChallengeId(sender.getId(), challengeId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.PARTICIPATE_CHALLENGE_NOT_FOUND, null));

        // 초대하는 사용자가 챌린지의 관리자 이상의 권한을 가지고 있는지 확인
        if(participateChallenge.getChallengeRole() == ChallengeRole.USER) {
            throw new CustomException(CustomExceptionCode.LOW_CHALLENGE_ROLE, null);
        }

        /// 초대 사용자 리스트 조회

        // 초대 사용자 리스트
        List<User> invitedUserList = userRepository.findAllById(userIdList);

        // 필터링
        // 1. 초대한 사용자가 최대 개수로 챌린지를 참여하고 있는지 확인
        // 2. 초대한 사용자와 친구 사이인지 확인
        // 3. 이미 해당 챌린지에 참여하고 있는지 확인
        // 4. 해당 챌린지로부터 차단되었는지 확인
        invitedUserList = invitedUserList.stream().filter(invitedUser -> {

            // 1. 초대한 사용자가 최대 개수로 챌린지를 참여하고 있는지 확인
            if(accountService.isParticipatingInMaxChallenges(invitedUser)) {
                return false;
            }

            // 2. 초대한 사용자와 친구 사이인지 확인
            if(friendRepository.findByUser1IdAndUser2Id(sender.getId(), invitedUser.getId()).isEmpty()) {
                return false;
            }

            // 3. 이미 해당 챌린지에 참여하고 있는지 확인
            if(participateChallengeRepository.findByUserIdAndChallengeId(invitedUser.getId(), challengeId).isPresent()) {
                return false;
            }

            // 4. 해당 챌린지로부터 차단되었는지 확인
            return challengeBlockRepository.findByUserIdAndChallengeId(invitedUser.getId(), challengeId).isEmpty();

        }).toList();

        /// 챌린지에 초대 가능한 인원수가 초대할 사용자의 인원수를 수용할 수 있는지 확인

        // 챌린지에 초대 가능한 인원수가 초대할 사용자의 인원수를 수용할 수 있는지 확인
        if(invitedUserList.size() + participateChallengeRepository.countAllByChallengeId(challengeId) > challenge.getMaxParticipantCount()) {
            throw new CustomException(CustomExceptionCode.FULL_CHALLENGE, null);
        }

        /// 챌린지 초대 데이터 생성 및 챌린지 초대 알림 전송

        // 챌린지 초대 데이터 생성 및 챌린지 초대 알림 전송
        inviteChallenge(sender, invitedUserList, challenge);
    }

    // 챌린지 초대 수락 또는 거절
    @Transactional
    public void answerInviteChallenge(User receiver, Long inviteChallengeId, boolean isAccept)
    {
        /// 예외 처리
        /// 1. 챌린지 초대 데이터가 존재하지 않는 경우, 예외 처리
        /// 2. 초대를 보낸 사용자와 초대를 받은 사용자가 친구 사이가 아닌 경우, 예외 처리

        // 챌린지 초대 데이터 조회
        InviteChallenge inviteChallenge = inviteChallengeRepository.findById(inviteChallengeId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.INVITE_CHALLENGE_NOT_FOUND, inviteChallengeId));

        // 초대를 보낸 사용자와 초대를 받은 사용자가 친구 사이가 아닌 경우, 예외 처리
        if(friendRepository.findByUser1IdAndUser2Id(inviteChallenge.getSender().getId(), receiver.getId()).isEmpty()) {
            throw new CustomException(CustomExceptionCode.FRIEND_NOT_FOUND, null);
        }

        /// 챌린지 초대를 수락하는 경우, 챌린지 가입

        // 챌린지 초대를 수락하는 경우, 챌린지 가입
        if(isAccept) {
            joinChallenge(receiver, inviteChallenge.getChallenge().getId(), true);
        }

        /// 챌린지 초대 데이터 및 알림 삭제

        // 챌린지 초대 데이터 삭제
        inviteChallengeRepository.delete(inviteChallenge);

        // 챌린지 초대 알림 삭제
        notificationService.deleteNotificationEntity(inviteChallenge.getNotification());
    }

    // 현재 진행 중인 내 챌린지 조회
    @Transactional(readOnly = true)
    public GetMyChallengeDto getMyChallenges(User user)
    {
        // 참여 가능한 챌린지 최대 개수
        int maxChallengeCount = user.getMaxChallengeCount();

        // 모든 챌린지 참여 정보를 생성 날짜 내림차순으로 조회
        List<ParticipateChallenge> participateChallengeList = participateChallengeRepository.findAllOngoing(user.getId());

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
                    long maxEvidencePhotoCount = phase.countMaxEvidencePhoto();

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
    @Transactional
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

    // 챌린지 상세 조회
    @Transactional(readOnly = true)
    public ChallengeDetailDto getChallenge(User user, Long challengeId)
    {
        /// 챌린지 조회

        // 챌린지 조회
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.CHALLENGE_NOT_FOUND, challengeId));

        /// 챌린지 비공개 예외 처리

        // 챌린지 비공개 예외 처리
        checkChallengePublicity(challenge, user);

        /// 현재 페이즈 조회

        // 현재 페이즈 번호
        int currentPhaseNumber = challenge.calcCurrentPhaseNumber();

        // 현재 페이즈
        Phase currentPhase = phaseRepository.findByChallengeIdAndNumber(challengeId, currentPhaseNumber)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.PHASE_NOT_FOUND, currentPhaseNumber));

        /// 전체 챌린지 참여 정보 리스트

        // 챌린지 참여 데이터 리스트
        List<ParticipateChallenge> participateChallengeList = participateChallengeRepository.findAllByChallengeId(challengeId);

        // 정렬
        // challengeRole 기준 : SUPER_ADMIN > ADMIN > USER
        // 동일한 challengeRole 내에서는 createAt 오름차순
        participateChallengeList.sort(
                Comparator
                        .comparing((ParticipateChallenge participateChallenge) -> participateChallenge.getChallengeRole().ordinal())
                        .thenComparing(ParticipateChallenge::getCreatedAt)
        );

        /// 챌린지에 참여 중인 사용자인 경우, 현재 페이즈 현황 정보를 포함
        /// 그 외의 경우, 챌린지 로드맵 정보를 포함

        if(
                user != null &&
                participateChallengeRepository.findByUserIdAndChallengeId(user.getId(), challengeId).isPresent()
        )
        {
            /// 요청자의 챌린지 참여 정보 조회

            // 챌린지 참여 정보
            ParticipateChallenge participateChallenge = participateChallengeRepository.findByUserIdAndChallengeId(user.getId(), challengeId)
                    .orElseThrow(() -> new CustomException(CustomExceptionCode.PARTICIPATE_CHALLENGE_NOT_FOUND, null));

            /// 요청자의 현재 페이즈 참여 정보 조회

            // 현재 페이즈 참여 정보
            ParticipatePhase participatePhase = participatePhaseRepository.findByPhaseIdAndUserId(currentPhase.getId(), user.getId())
                    .orElseThrow(() -> new CustomException(CustomExceptionCode.PARTICIPATE_PHASE_NOT_FOUND, null));

            // 증거사진 리스트
            List<EvidencePhoto> evidencePhotoList = evidencePhotoRepository.findAllByParticipatePhaseId(participatePhase.getId());

            /// 현재 페이즈 현황 정보

            // 현재 페이즈 현황 정보
            PhaseStatusDto currentPhaseInfo = PhaseStatusDto.from(participateChallenge, participatePhase, evidencePhotoList);

            /// 챌린지 상세 정보 반환 (+현재 페이즈 현황 정보)
            
            // 챌린지 상세 정보 반환
            return ChallengeDetailDto.from(challenge, participateChallengeList, currentPhaseInfo, true);
        }
        else
        {
            /// (챌린지 참여 정보, 현재 페이즈 참여 정보) 리스트 생성

            // (챌린지 참여 정보, 현재 페이즈 참여 정보) 리스트 생성
            Map<ParticipateChallenge, ParticipatePhase> map = participateChallengeList.stream()
                    .collect(Collectors.toMap(
                            participateChallenge -> participateChallenge,
                            participateChallenge -> participatePhaseRepository.findByPhaseIdAndUserId(currentPhase.getId(), participateChallenge.getUser().getId())
                                    .orElseThrow(() -> new CustomException(CustomExceptionCode.PARTICIPATE_PHASE_NOT_FOUND, null))
                    ));

            /// 챌린지 로드맵 정보

            // 챌린지 로드맵 정보
            ChallengeRoadmapDto challengeRoadmapInfo = ChallengeRoadmapDto.from(currentPhase, map);

            /// 챌린지 상세 정보 반환 (+챌린지 로드맵 정보)

            // 챌린지 상세 정보 반환
            return ChallengeDetailDto.from(challenge, participateChallengeList, challengeRoadmapInfo, false);
        }
    }

    // 페이즈 현황 정보 조회
    @Transactional(readOnly = true)
    public PhaseStatusDto getPhaseStatus(User requester, Long challengeId, Long userId, Integer phaseNumber)
    {
        /// 챌린지 조회

        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.CHALLENGE_NOT_FOUND, challengeId));

        /// 챌린지 비공개 예외 처리

        checkChallengePublicity(challenge, requester);

        /// 페이즈 조회

        // 현재 페이즈 번호보다 요청한 페이즈의 번호가 큰지 확인
        if(phaseNumber > challenge.calcCurrentPhaseNumber()) {
            throw new CustomException(CustomExceptionCode.INVALID_PHASE_NUMBER, phaseNumber);
        }

        // 페이즈 조회
        Phase phase = phaseRepository.findByChallengeIdAndNumber(challengeId, phaseNumber)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.PHASE_NOT_FOUND, phaseNumber));

        /// 챌린지 참여 데이터 조회

        ParticipateChallenge participateChallenge = participateChallengeRepository.findByUserIdAndChallengeId(userId, challengeId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.PARTICIPATE_CHALLENGE_NOT_FOUND, null));

        /// 페이즈 참여 데이터 조회

        ParticipatePhase participatePhase = participatePhaseRepository.findByPhaseIdAndUserId(phase.getId(), userId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.PARTICIPATE_PHASE_NOT_FOUND, null));

        /// 증거사진 리스트 조회

        // 증거사진 리스트
        List<EvidencePhoto> evidencePhotoList = evidencePhotoRepository.findAllByParticipatePhaseId(participatePhase.getId());

        /// 페이즈 현황 정보 반환

        return PhaseStatusDto.from(participateChallenge, participatePhase, evidencePhotoList);
    }

    /// 공통 로직

    // 챌린지 초대 데이터 생성 및 챌린지 초대 알림 전송
    @Transactional
    public void inviteChallenge(User sender, List<User> userList, Challenge challenge)
    {
        /// 기존에 이미 초대를 받은 사용자는 챌린지 초대 데이터와 알림의 생성 날짜만 갱신

        // 새롭게 챌린지에 초대된 사용자 리스트
        List<User> newInvitedUserList = new ArrayList<>();

        // 기존에 이미 초대를 받은 사용자는 알림 생성 날짜만 갱신
        userList.forEach(user -> {

            // 챌린지 초대 데이터 조회
            Optional<InviteChallenge> inviteChallengeOptional = inviteChallengeRepository.findBySenderIdAndReceiverIdAndChallengeId(
                    sender.getId(),
                    user.getId(),
                    challenge.getId()
            );

            // 챌린지 초대 데이터가 존재하는 경우, 챌린지 초대 데이터와 알림의 생성 날짜만 갱신
            // 챌린지 초대 데이터가 존재하지 않는 경우, 새롭게 챌린지에 초대된 사용자 리스트에 추가
            if(inviteChallengeOptional.isPresent()) {

                // 챌린지 초대 데이터 생성 날짜 갱신
                inviteChallengeOptional.get().renew();

                // 알림 생성 날짜 갱신
                inviteChallengeOptional.get().getNotification().renew();

            } else {
                newInvitedUserList.add(user);
            }
        });

        // 챌린지 초대 데이터 및 알림 생성
        newInvitedUserList.forEach(user -> {

            // 챌린지 초대 데이터 생성
            InviteChallenge inviteChallenge = InviteChallenge.builder()
                    .sender(sender)
                    .receiver(user)
                    .challenge(challenge)
                    .build();

            inviteChallengeRepository.save(inviteChallenge);

            // 챌린지 초대 알림 생성
            Notification notification = inviteChallengeNotificationFactory.createNotification(user, inviteChallenge.getId());

            // 챌린지 초대 데이터의 알림 컬럼 갱신
            inviteChallenge.setNotification(notification);

        });
    }

    // 현재 페이즈 조회
    @Transactional(readOnly = true)
    public Phase getCurrentPhase(Challenge challenge)
    {
        return phaseRepository.findByChallengeIdAndNumber(challenge.getId(), challenge.calcCurrentPhaseNumber())
                .orElseThrow(() -> new CustomException(CustomExceptionCode.PHASE_NOT_FOUND, null));
    }

    // 현재 페이즈부터의 모든 페이즈 조회
    @Transactional(readOnly = true)
    public List<Phase> getPhaseListFromCurrent(Challenge challenge)
    {
        return phaseRepository.findAllFromNumber(challenge.getId(), challenge.calcCurrentPhaseNumber());
    }

    // 요청 개수만큼 페이즈 생성
    @Transactional
    public void createPhases(Challenge challenge, int count)
    {
        /// 생성할 페이즈 개수만큼 다음을 수행
        /// 1. 챌린지의 페이즈 개수 1 증가
        /// 2. 페이즈 생성
        /// 3. 페이즈 참여 정보 생성

        // 챌린지 참여 정보 리스트
        List<ParticipateChallenge> participateChallengeList = participateChallengeRepository.findAllByChallengeIdOrderByCreatedAtDesc(challenge.getId());

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

        phaseRepository.saveAll(phaseList);
        participatePhaseRepository.saveAll(participatePhaseList);
    }

    // 챌린지 비공개 예외 처리
    public void checkChallengePublicity(Challenge challenge, User user)
    {
        // 챌린지가 비공개인 경우
        if(!challenge.isPublic())
        {
            // 로그인하지 않은 사용자 예외 처리
            if(user == null) {
                throw new CustomException(CustomExceptionCode.PRIVATE_CHALLENGE, null);
            }
            else {

                // 로그인 한 사용자는 다음의 경우 조회 가능
                // 1. 관리자인 경우
                // 2. 해당 챌린지에 참여 중인 경우
                // 3. 해당 챌린지에 초대 받은 경우
                if(
                        user.getAccountRole() != AccountRole.ADMIN &&
                        participateChallengeRepository.findByUserIdAndChallengeId(user.getId(), challenge.getId()).isEmpty() &&
                        inviteChallengeRepository.findByReceiverIdAndChallengeId(user.getId(), challenge.getId()).isEmpty()
                ) {
                    throw new CustomException(CustomExceptionCode.PRIVATE_CHALLENGE, null);
                }

            }
        }
    }
}
