package Challenge.with_back.domain.update_participate_phase.service;

import Challenge.with_back.common.entity.rdbms.EvidencePhoto;
import Challenge.with_back.common.entity.rdbms.ParticipatePhase;
import Challenge.with_back.common.entity.rdbms.Phase;
import Challenge.with_back.common.entity.rdbms.User;
import Challenge.with_back.common.enums.UpdateParticipatePhaseType;
import Challenge.with_back.common.repository.rdbms.*;
import Challenge.with_back.common.exception.CustomException;
import Challenge.with_back.common.exception.CustomExceptionCode;
import Challenge.with_back.domain.challenge.dto.EvidencePhotoDto;
import Challenge.with_back.domain.update_participate_phase.UpdateParticipatePhaseMessage;
import Challenge.with_back.domain.challenge.util.ChallengeUtil;
import Challenge.with_back.domain.evidence_photo.S3EvidencePhoto;
import Challenge.with_back.domain.evidence_photo.S3EvidencePhotoManager;
import Challenge.with_back.domain.update_participate_phase.UpdateParticipatePhaseStrategy;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateParticipatePhaseService
{
    private final UserRepository userRepository;
    private final ParticipatePhaseRepository participatePhaseRepository;
    private final EvidencePhotoRepository evidencePhotoRepository;

    private final ChallengeUtil challengeUtil;

    private final S3EvidencePhotoManager s3EvidencePhotoManager;

    private final RabbitTemplate rabbitTemplate;
    private final Map<String, UpdateParticipatePhaseStrategy> updateParticipatePhaseStrategyMap;

    private static final Logger log = LoggerFactory.getLogger(UpdateParticipatePhaseService.class);

    @Value("${RABBITMQ_UPDATE_PARTICIPATE_PHASE_EXCHANGE_NAME}")
    private String updateParticipatePhaseExchangeName;

    @Value("${RABBITMQ_UPDATE_PARTICIPATE_PHASE_ROUTING_KEY}")
    private String updateParticipatePhaseRoutingKey;

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
        if(evidencePhotoRepository.countAllByParticipatePhaseId(participatePhaseId) + images.size() > maxEvidencePhotoCount)
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

        // 챌린지 및 챌린지 참여 정보 마지막 활동 날짜 갱신
        challengeUtil.renewLastActiveDate(participatePhase);
        participatePhaseRepository.save(participatePhase);

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

        // 증거사진 삭제
        evidencePhotoRepository.delete(evidencePhoto);

        // 챌린지 및 챌린지 참여 정보 마지막 활동 날짜 갱신
        challengeUtil.renewLastActiveDate(participatePhase);
        participatePhaseRepository.save(participatePhase);

        // S3에서 삭제
        s3EvidencePhotoManager.delete(evidencePhoto.getFilename());
    }

    // 페이즈 참여 정보 한마디 수정 요청 메시지를 RabbitMQ의 큐로 발행
    public void sendUpdateParticipatePhaseComment(User user, Long participatePhaseId, String comment)
    {
        UpdateParticipatePhaseMessage message = UpdateParticipatePhaseMessage.builder()
                .type(UpdateParticipatePhaseType.UPDATE_COMMENT.name())
                .userId(user.getId())
                .participatePhaseId(participatePhaseId)
                .data(comment)
                .build();

        rabbitTemplate.convertAndSend(updateParticipatePhaseExchangeName, updateParticipatePhaseRoutingKey, message);
    }

    // 페이즈 참여 정보 현재 달성 개수 변경 요청 메시지를 RabbitMQ의 큐로 발행
    public void sendUpdateParticipatePhaseCurrentCount(User user, Long participatePhaseId, int value)
    {
        UpdateParticipatePhaseMessage message = UpdateParticipatePhaseMessage.builder()
                .type(UpdateParticipatePhaseType.UPDATE_CURRENT_COUNT.name())
                .userId(user.getId())
                .participatePhaseId(participatePhaseId)
                .data(value)
                .build();

        rabbitTemplate.convertAndSend(updateParticipatePhaseExchangeName, updateParticipatePhaseRoutingKey, message);
    }

    // 페이즈 참여 정보 면제 여부 토글 요청 메시지를 RabbitMQ의 큐로 발행
    public void sendToggleIsExempt(User user, Long participatePhaseId)
    {
        UpdateParticipatePhaseMessage message = UpdateParticipatePhaseMessage.builder()
                .type(UpdateParticipatePhaseType.TOGGLE_IS_EXEMPT.name())
                .userId(user.getId())
                .participatePhaseId(participatePhaseId)
                .data(null)
                .build();

        rabbitTemplate.convertAndSend(updateParticipatePhaseExchangeName, updateParticipatePhaseRoutingKey, message);
    }

    // RabbitMQ의 페이즈 참여 정보 변경 메시지 수신(구독) 서비스
    @RabbitListener(queues = "${RABBITMQ_UPDATE_PARTICIPATE_PHASE_QUEUE_NAME}")
    @Transactional
    public void updateParticipatePhase(UpdateParticipatePhaseMessage message)
    {
        // 사용자 정보
        User user = userRepository.findById(message.getUserId())
                .orElseThrow(() -> new CustomException(CustomExceptionCode.USER_NOT_FOUND, message.getUserId()));

        // 페이즈 참여 정보
        ParticipatePhase participatePhase = participatePhaseRepository.findById(message.getParticipatePhaseId())
                .orElseThrow(() -> new CustomException(CustomExceptionCode.PARTICIPATE_PHASE_NOT_FOUND, message.getParticipatePhaseId()));

        // 페이즈 참여 정보 변경 전략 확인
        UpdateParticipatePhaseStrategy updateParticipatePhaseStrategy = updateParticipatePhaseStrategyMap.get(message.getType());

        // 존재하지 않는 페이즈 참여 정보 변경 종류라면 예외 처리
        if(updateParticipatePhaseStrategy == null)
            throw new CustomException(CustomExceptionCode.INVALID_UPDATE_PARTICIPATE_PHASE_TYPE, message.getType());

        // 페이즈 참여 정보 변경
        try {
            updateParticipatePhaseStrategy.updateParticipatePhase(user, participatePhase, message.getData());
        } catch (CustomException e) {
            log.error("{}: {}\n{}", e.getErrorCode().name(), e.getErrorCode().getMessage(), message.toString());
            throw e;
        }
    }
}
