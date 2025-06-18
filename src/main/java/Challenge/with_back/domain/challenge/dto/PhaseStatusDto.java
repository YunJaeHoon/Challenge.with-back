package Challenge.with_back.domain.challenge.dto;

import Challenge.with_back.common.entity.rdbms.*;
import Challenge.with_back.common.enums.ChallengeRole;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class PhaseStatusDto extends ChallengeSubInfo
{
    private UserInfo userInfo;                          // 사용자 정보
    private PhaseInfo phaseInfo;                        // 페이즈 정보
    private ParticipatePhaseInfo participatePhaseInfo;  // 페이즈 참여 정보

    public static PhaseStatusDto from(ParticipateChallenge participateChallenge, ParticipatePhase participatePhase, List<EvidencePhoto> evidencePhotoList)
    {
        return PhaseStatusDto.builder()
                .userInfo(UserInfo.from(participateChallenge))
                .phaseInfo(PhaseInfo.from(participatePhase.getPhase()))
                .participatePhaseInfo(ParticipatePhaseInfo.from(participatePhase, evidencePhotoList))
                .build();
    }

    @Getter
    @Builder
    private static class UserInfo
    {
        private Long id;                        // 사용자 ID
        private String nickname;                // 닉네임
        private String profileImageUrl;         // 프로필 이미지 URL
        private ChallengeRole challengeRole;    // 챌린지 권한
        private String determination;           // 각오 한마디

        protected static UserInfo from(ParticipateChallenge participateChallenge)
        {
            User user = participateChallenge.getUser();

            return UserInfo.builder()
                    .id(user.getId())
                    .nickname(user.getNickname())
                    .profileImageUrl(user.getProfileImageUrl())
                    .challengeRole(participateChallenge.getChallengeRole())
                    .determination(participateChallenge.getDetermination())
                    .build();
        }
    }

    @Getter
    @Builder
    private static class PhaseInfo
    {
        private Long id;                           // 페이즈 ID
        private int number;                        // 페이즈 번호
        private String name;                       // 페이즈 이름
        private String description;                // 페이즈 설명
        private LocalDate startDate;               // 페이즈 시작 날짜
        private LocalDate endDate;                 // 페이즈 종료 날짜

        protected static PhaseInfo from(Phase phase)
        {
            return PhaseInfo.builder()
                    .id(phase.getId())
                    .number(phase.getNumber())
                    .name(phase.getName())
                    .description(phase.getDescription())
                    .startDate(phase.getStartDate())
                    .endDate(phase.getEndDate())
                    .build();
        }
    }

    @Getter
    @Builder
    private static class ParticipatePhaseInfo
    {
        private Long id;                        // 페이즈 참여 데이터 ID
        private int completeCount;              // 페이즈 완료 개수
        private Boolean isExempt;               // 면제 여부
        private String comment;                 // 한마디
        private long maxEvidencePhotoCount;     // 증거사진 최대 개수

        // 증거사진 정보 리스트
        private List<EvidencePhotoInfo> evidencePhotoInfoList;

        protected static ParticipatePhaseInfo from(ParticipatePhase participatePhase, List<EvidencePhoto> evidencePhotoList)
        {
            Phase phase = participatePhase.getPhase();

            return ParticipatePhaseInfo.builder()
                    .id(participatePhase.getId())
                    .completeCount(participatePhase.getCurrentCount())
                    .isExempt(participatePhase.isExempt())
                    .comment(participatePhase.getComment())
                    .maxEvidencePhotoCount(phase.countMaxEvidencePhoto())
                    .evidencePhotoInfoList(evidencePhotoList.stream().map(EvidencePhotoInfo::from).toList())
                    .build();
        }

        @Getter
        @Builder
        private static class EvidencePhotoInfo
        {
            private Long id;        // 증거사진 ID
            private String url;     // 증거사진 URL

            protected static EvidencePhotoInfo from(EvidencePhoto evidencePhoto)
            {
                return EvidencePhotoInfo.builder()
                        .id(evidencePhoto.getId())
                        .url(evidencePhoto.getPhotoUrl())
                        .build();
            }
        }
    }
}
