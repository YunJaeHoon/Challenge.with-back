package Challenge.with_back.domain.challenge.service;

import Challenge.with_back.domain.evidence_photo.S3EvidencePhoto;
import Challenge.with_back.domain.evidence_photo.S3EvidencePhotoManager;
import Challenge.with_back.enums.ChallengeRole;
import Challenge.with_back.enums.ChallengeUnit;
import Challenge.with_back.response.exception.CustomException;
import Challenge.with_back.response.exception.CustomExceptionCode;
import Challenge.with_back.domain.account.util.AccountUtil;
import Challenge.with_back.domain.challenge.dto.CreateChallengeDto;
import Challenge.with_back.domain.challenge.dto.GetMyChallengeDto;
import Challenge.with_back.domain.challenge.util.ChallengeUtil;
import Challenge.with_back.entity.rdbms.*;
import Challenge.with_back.repository.rdbms.*;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class ChallengeService
{
    private final UserRepository userRepository;
    private final ChallengeRepository challengeRepository;
    private final ParticipateChallengeRepository participateChallengeRepository;
    private final PhaseRepository phaseRepository;
    private final ParticipatePhaseRepository participatePhaseRepository;
    private final EvidencePhotoRepository evidencePhotoRepository;

    private final AccountUtil accountUtil;
    private final ChallengeUtil challengeUtil;

    private final S3EvidencePhotoManager s3EvidencePhotoManager;

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

    // 내 챌린지 조회
    public GetMyChallengeDto getMyChallenges(User user)
    {
        // 참여 중인 챌린지 개수
        int countChallenge = user.getCountParticipateChallenge();

        // 챌린지 개수 상한값
        int maxChallengeCount = accountUtil.getMaxChallengeCount(user);

        // 모든 챌린지 참여 정보를 생성 날짜 내림차순으로 조회
        List<ParticipateChallenge> participateChallengeList = participateChallengeRepository.findAllByUserOrderByCreatedAtDesc(user);

        // 챌린지 참여 정보 리스트를 dto 리스트로 변경
        List<GetMyChallengeDto.ChallengeDto> challengeDtoList = participateChallengeList.stream()
                .map(participateChallenge -> {

                    // 챌린지
                    Challenge challenge = participateChallenge.getChallenge();

                    // 현재 페이즈
                    Phase phase = challengeUtil.getLastPhase(challenge);

                    // 현재 페이즈 참여 정보
                    ParticipatePhase participatePhase = participatePhaseRepository.findByPhaseAndUser(phase, user)
                            .orElseThrow(() -> new CustomException(CustomExceptionCode.PARTICIPATE_PHASE_NOT_FOUND, null));

                    // 증거사진 리스트
                    List<EvidencePhoto> evidencePhotoList = evidencePhotoRepository.findAllByParticipatePhase(participatePhase);

                    // 증거사진 리스트를 증거사진 URL 리스트로 변경
                    List<String> evidencePhotoUrlList = evidencePhotoList.stream()
                            .map(EvidencePhoto::getPhotoUrl)
                            .toList();

                    return GetMyChallengeDto.ChallengeDto.builder()
                            .challengeId(challenge.getId())
                            .iconUrl(challenge.getIconUrl())
                            .challengeName(challenge.getName())
                            .challengeDescription(challenge.getDescription())
                            .maxParticipantCount(challenge.getMaxParticipantCount())
                            .goalCount(challenge.getGoalCount())
                            .unit(challenge.getUnit().name())
                            .challengeStartDate(challenge.getCreatedAt().toLocalDate())
                            .countPhase(challenge.getCountPhase())
                            .participateCurrentPhaseId(participatePhase.getId())
                            .currentPhaseStartDate(phase.getStartDate())
                            .currentPhaseEndDate(phase.getEndDate())
                            .currentPhaseName(phase.getName())
                            .completeCount(participatePhase.getCurrentCount())
                            .isExempt(participatePhase.isExempt())
                            .comment(participatePhase.getComment())
                            .countEvidencePhoto(participatePhase.getCountEvidencePhoto())
                            .evidencePhotoUrls(evidencePhotoUrlList)
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
    public void uploadEvidencePhotos(User user, Long participatePhaseId, List<MultipartFile> images)
    {
        // 페이즈 참여 정보
        ParticipatePhase participatePhase = participatePhaseRepository.findById(participatePhaseId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.PARTICIPATE_PHASE_NOT_FOUND, participatePhaseId));

        // 페이즈 참여 정보가 해당 사용자 것인지 확인
        challengeUtil.checkParticipatePhaseOwnership(user, participatePhase);

        // 증거사진 최대 개수를 초과한다면 예외처리
        if(participatePhase.getCountEvidencePhoto() + images.size() > 5)
            throw new CustomException(CustomExceptionCode.TOO_MANY_EVIDENCE_PHOTO, participatePhase.getCountEvidencePhoto() + images.size());

        // 스레드 풀 생성
        ExecutorService executorService = Executors.newFixedThreadPool(images.size());

        images.forEach(image -> {
            executorService.submit(() -> {
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
            });
        });

        // 스레드 풀 종료
        executorService.shutdown();

        // 증거사진 개수 갱신
        participatePhase.increaseCountEvidencePhoto(images.size());
        participatePhaseRepository.save(participatePhase);
    }

    // 증거사진 삭제
    public void deleteEvidencePhoto(User user, Long evidencePhotoId)
    {
        // 증거사진
        EvidencePhoto evidencePhoto = evidencePhotoRepository.findById(evidencePhotoId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.EVIDENCE_PHOTO_NOT_FOUND, evidencePhotoId));

        // 페이즈 참여 정보
        ParticipatePhase participatePhase = evidencePhoto.getParticipatePhase();

        // 페이즈 참여 정보가 해당 사용자 것인지 확인
        challengeUtil.checkParticipatePhaseOwnership(user, participatePhase);

        // S3에서 삭제
        s3EvidencePhotoManager.delete(evidencePhoto.getFilename());

        // 증거사진 개수 갱신
        participatePhase.decreaseCountEvidencePhoto();
        participatePhaseRepository.save(participatePhase);

        // 증거사진 삭제
        evidencePhotoRepository.delete(evidencePhoto);
    }
}
