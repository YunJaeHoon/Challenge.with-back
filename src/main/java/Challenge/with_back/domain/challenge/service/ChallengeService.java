package Challenge.with_back.domain.challenge.service;

import Challenge.with_back.common.entity.rdbms.*;
import Challenge.with_back.common.enums.UpdateParticipatePhaseType;
import Challenge.with_back.common.repository.rdbms.*;
import Challenge.with_back.domain.challenge.dto.EvidencePhotoDto;
import Challenge.with_back.domain.challenge.dto.UpdateParticipatePhaseMessage;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChallengeService
{
    private final UserRepository userRepository;
    private final ChallengeRepository challengeRepository;
    private final ParticipateChallengeRepository participateChallengeRepository;
    private final ParticipatePhaseRepository participatePhaseRepository;
    private final EvidencePhotoRepository evidencePhotoRepository;

    private final AccountUtil accountUtil;
    private final ChallengeUtil challengeUtil;

    private final S3EvidencePhotoManager s3EvidencePhotoManager;

    private final RabbitTemplate rabbitTemplate;

    @Value("${RABBITMQ_EXCHANGE_NAME}")
    private String exchangeName;

    @Value("${RABBITMQ_ROUTING_KEY}")
    private String routingKey;

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
                .countCurrentParticipant(0)
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

            // 챌린지 참여자 인원수 1명 증가
            challenge.increaseCountCurrentParticipant();

            // 사용자 참여 챌린지 개수 1개 증가
            participant.increaseCountParticipateChallenge();
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
        int countChallenge = user.getCountParticipateChallenge();

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

    // 증거사진 등록
    @Transactional
    public List<EvidencePhotoDto> uploadEvidencePhotos(User user, Long participatePhaseId, List<MultipartFile> images)
    {
        // 페이즈 참여 정보
        ParticipatePhase participatePhase = participatePhaseRepository.findById(participatePhaseId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.PARTICIPATE_PHASE_NOT_FOUND, participatePhaseId));

        // 페이즈 참여 정보가 해당 사용자 것인지 확인
        challengeUtil.checkParticipatePhaseOwnership(user, participatePhase);

        // 페이즈
        Phase phase = participatePhase.getPhase();

        // 증거사진 최대 개수
        long maxEvidencePhotoCount = ChronoUnit.DAYS.between(phase.getStartDate(), phase.getEndDate()) + 1;

        // 증거사진 최대 개수를 초과한다면 예외처리
        if(participatePhase.getCountEvidencePhoto() + images.size() > maxEvidencePhotoCount)
            throw new CustomException(CustomExceptionCode.TOO_MANY_EVIDENCE_PHOTO, maxEvidencePhotoCount);

        // 증거사진 dto 리스트
        List<EvidencePhotoDto> evidencePhotoDtoList = new ArrayList<>();

        images.forEach(image -> {
            // UUID 생성
            String uuid = UUID.randomUUID().toString();

            // 증거사진 엔티티 생성
            EvidencePhoto evidencePhoto = EvidencePhoto.builder()
                    .participatePhase(participatePhase)
                    .filename(uuid)
                    .build();

            // S3 업로드
            S3EvidencePhoto s3EvidencePhoto = s3EvidencePhotoManager.upload(image, uuid);

            // 증거사진 URL 등록
            evidencePhoto.setPhotoUrl(s3EvidencePhoto.getPhotoUrl());
            evidencePhoto.setFilename(s3EvidencePhoto.getFilename());

            // 증거사진 엔티티 저장
            evidencePhotoRepository.save(evidencePhoto);

            // 증거사진 dto 리스트에 추가
            evidencePhotoDtoList.add(EvidencePhotoDto.builder()
                    .evidencePhotoId(evidencePhoto.getId())
                    .url(evidencePhoto.getPhotoUrl())
                    .build());
        });

        // 증거사진 개수 갱신
        participatePhase.increaseCountEvidencePhoto(images.size());
        participatePhaseRepository.save(participatePhase);

        // 챌린지 및 챌린지 참여 정보 마지막 활동 날짜 갱신
        challengeUtil.renewLastActiveDate(participatePhase);

        return evidencePhotoDtoList;
    }

    // 증거사진 삭제
    @Transactional
    public void deleteEvidencePhoto(User user, Long evidencePhotoId)
    {
        // 증거사진
        EvidencePhoto evidencePhoto = evidencePhotoRepository.findById(evidencePhotoId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.EVIDENCE_PHOTO_NOT_FOUND, evidencePhotoId));

        // 페이즈 참여 정보
        ParticipatePhase participatePhase = evidencePhoto.getParticipatePhase();

        // 페이즈 참여 정보가 해당 사용자 것인지 확인
        challengeUtil.checkParticipatePhaseOwnership(user, participatePhase);

        // 증거사진 개수 갱신
        participatePhase.decreaseCountEvidencePhoto();
        participatePhaseRepository.save(participatePhase);

        // 증거사진 삭제
        evidencePhotoRepository.delete(evidencePhoto);

        // 챌린지 및 챌린지 참여 정보 마지막 활동 날짜 갱신
        challengeUtil.renewLastActiveDate(participatePhase);

        // S3에서 삭제
        s3EvidencePhotoManager.delete(evidencePhoto.getFilename());
    }

    // 페이즈 참여 정보 한마디 수정 요청 메시지를 RabbitMQ의 큐로 발행
    public void sendUpdateParticipatePhaseComment(User user, Long participatePhaseId, String comment)
    {
        System.out.println("한마디 수정 메시지 전송");

        UpdateParticipatePhaseMessage message = UpdateParticipatePhaseMessage.builder()
                .type(UpdateParticipatePhaseType.UPDATE_COMMENT)
                .userId(user.getId())
                .participatePhaseId(participatePhaseId)
                .data(comment)
                .build();

        rabbitTemplate.convertAndSend(exchangeName, routingKey, message);
    }

    // 페이즈 참여 정보 현재 달성 개수 변경 요청 메시지를 RabbitMQ의 큐로 발행
    public void sendUpdateParticipatePhaseCurrentCount(User user, Long participatePhaseId, int value)
    {
        System.out.println("현재 달성 개수 변경 메시지 전송");

        UpdateParticipatePhaseMessage message = UpdateParticipatePhaseMessage.builder()
                .type(UpdateParticipatePhaseType.UPDATE_CURRENT_COUNT)
                .userId(user.getId())
                .participatePhaseId(participatePhaseId)
                .data(value)
                .build();

        rabbitTemplate.convertAndSend(exchangeName, routingKey, message);
    }

    // 페이즈 참여 정보 면제 여부 토글 요청 메시지를 RabbitMQ의 큐로 발행
    public void sendToggleIsExempt(User user, Long participatePhaseId)
    {
        UpdateParticipatePhaseMessage message = UpdateParticipatePhaseMessage.builder()
                .type(UpdateParticipatePhaseType.TOGGLE_IS_EXEMPT)
                .userId(user.getId())
                .participatePhaseId(participatePhaseId)
                .data(null)
                .build();

        rabbitTemplate.convertAndSend(exchangeName, routingKey, message);
    }

    // RabbitMQ의 페이즈 참여 정보 변경 메시지 수신(구독) 서비스
    @RabbitListener(queues = "${RABBITMQ_QUEUE_NAME}")
    @Transactional
    public void updateParticipatePhaseInfo(UpdateParticipatePhaseMessage message)
    {
        // 사용자 정보
        User user = userRepository.findById(message.getUserId())
                .orElseThrow(() -> new CustomException(CustomExceptionCode.USER_NOT_FOUND, message.getUserId()));

        try {
            if (message.getType().equals(UpdateParticipatePhaseType.UPDATE_COMMENT)) {
                updateParticipatePhaseComment(user, message.getParticipatePhaseId(), (String) message.getData());
            } else if (message.getType().equals(UpdateParticipatePhaseType.UPDATE_CURRENT_COUNT)) {
                updateParticipatePhaseCurrentCount(user, message.getParticipatePhaseId(), (int) message.getData());
            } else if (message.getType().equals(UpdateParticipatePhaseType.TOGGLE_IS_EXEMPT)) {
                toggleIsExempt(user, message.getParticipatePhaseId());
            } else {
                throw new CustomException(CustomExceptionCode.INVALID_UPDATE_PARTICIPATE_PHASE_TYPE, message.getType().name());
            }
        } catch (CustomException e) {
            log.error(e.getErrorCode().getMessage());
        }
    }

    // 페이즈 참여 정보 한마디 수정
    public void updateParticipatePhaseComment(User user, Long participatePhaseId, String comment) throws CustomException
    {
        // 페이즈 참여 정보
        ParticipatePhase participatePhase = participatePhaseRepository.findById(participatePhaseId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.PARTICIPATE_PHASE_NOT_FOUND, participatePhaseId));

        // 페이즈 참여 정보가 해당 사용자 것인지 확인
        challengeUtil.checkParticipatePhaseOwnership(user, participatePhase);

        // 한마디 길이 체크
        challengeUtil.checkParticipatePhaseCommentLength(comment);

        // 한마디 수정
        participatePhase.updateComment(comment);
        participatePhaseRepository.save(participatePhase);

        // 챌린지 및 챌린지 참여 정보 마지막 활동 날짜 갱신
        challengeUtil.renewLastActiveDate(participatePhase);
    }

    // 페이즈 참여 정보 현재 달성 개수 변경
    @Async("participatePhaseThreadPool")
    @Transactional
    public void updateParticipatePhaseCurrentCount(User user, Long participatePhaseId, int value) throws CustomException
    {
        // 페이즈 참여 정보
        ParticipatePhase participatePhase = participatePhaseRepository.findById(participatePhaseId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.PARTICIPATE_PHASE_NOT_FOUND, participatePhaseId));

        // 페이즈 참여 정보가 해당 사용자 것인지 확인
        challengeUtil.checkParticipatePhaseOwnership(user, participatePhase);

        // 챌린지
        Challenge challenge = participatePhase.getPhase().getChallenge();

        // 현재 달성 개수가 범위를 벗어나면 예외 처리
        challengeUtil.checkCurrentCount(value, challenge);

        // 기존 달성 개수
        int originalValue = participatePhase.getCurrentCount();

        // 현재 달성 개수 변경
        participatePhase.updateCurrentCount(value);
        participatePhaseRepository.save(participatePhase);

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

        // 챌린지 및 챌린지 참여 정보 마지막 활동 날짜 갱신
        challengeUtil.renewLastActiveDate(participatePhase);
    }

    // 페이즈 참여 정보 면제 여부 토글
    @Async("participatePhaseThreadPool")
    @Transactional
    public void toggleIsExempt(User user, Long participatePhaseId) throws CustomException
    {
        // 페이즈 참여 정보
        ParticipatePhase participatePhase = participatePhaseRepository.findById(participatePhaseId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.PARTICIPATE_PHASE_NOT_FOUND, participatePhaseId));

        // 페이즈 참여 정보가 해당 사용자 것인지 확인
        challengeUtil.checkParticipatePhaseOwnership(user, participatePhase);

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

        // 챌린지 및 챌린지 참여 정보 마지막 활동 날짜 갱신
        challengeUtil.renewLastActiveDate(participatePhase);
    }
}
