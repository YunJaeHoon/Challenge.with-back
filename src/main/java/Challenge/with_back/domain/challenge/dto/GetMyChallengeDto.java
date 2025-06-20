package Challenge.with_back.domain.challenge.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class GetMyChallengeDto
{
    private int countChallenge;             // 참여 중인 챌린지 개수
    private int maxChallengeCount;          // 챌린지 개수 상한값
    private List<ChallengeDto> challenges;  // 각 챌린지 정보

    @Getter
    @Builder
    public static class ChallengeDto
    {
        private Long challengeId;                       // 챌린지 ID
        private Long superAdminId;                      // 최고 관리자 ID
        private String icon;                            // 아이콘
        private String colorTheme;                      // 색 테마
        private String challengeName;                   // 챌린지 이름
        private String challengeDescription;            // 챌린지 설명
        private int maxParticipantCount;                // 챌린지 최대 참여자 인원수
        private int goalCount;                          // 챌린지 목표 개수
        private String unit;                            // 챌린지 단위
        private LocalDate challengeStartDate;           // 챌린지 시작 날짜
        private Long participateCurrentPhaseId;         // 현재 페이즈 참여 정보 ID
        private int currentPhaseNumber;                 // 현재 페이즈 번호
        private LocalDate currentPhaseStartDate;        // 현재 페이즈 시작 날짜
        private LocalDate currentPhaseEndDate;          // 현재 페이즈 종료 날짜
        private String currentPhaseName;                // 현재 페이즈 이름
        private String currentPhaseDescription;         // 현재 페이즈 설명
        private int completeCount;                      // 현재 페이즈 완료 개수
        private Boolean isExempt;                       // 현재 페이즈 면제 여부
        private String comment;                         // 현재 페이즈 한마디
        private long maxEvidencePhotoCount;             // 증거사진 최대 개수
        private List<EvidencePhotoDto> evidencePhotos;  // 각 증거사진 정보
    }

    @Getter
    @Builder
    public static class EvidencePhotoDto
    {
        private Long evidencePhotoId;
        private String url;
    }
}
