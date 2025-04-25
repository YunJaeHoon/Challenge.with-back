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
        private String iconUrl;                     // 아이콘 URL
        private String challengeName;               // 챌린지 이름
        private String challengeDescription;        // 챌린지 설명
        private int maxParticipantCount;            // 챌린지 최대 참여자 인원수
        private int goalCount;                      // 챌린지 목표 개수
        private String unit;                        // 챌린지 단위
        private LocalDate challengeStartDate;       // 챌린지 시작 날짜
        private int countPhase;                     // 페이즈 개수
        private LocalDate currentPhaseStartDate;    // 현재 페이즈 시작 날짜
        private LocalDate currentPhaseEndDate;      // 현재 페이즈 종료 날짜
        private String currentPhaseName;            // 현재 페이즈 이름
        private int completeCount;                  // 현재 페이즈 완료 개수
        private Boolean isExempt;                   // 현재 페이즈 면제 여부
        private String comment;                     // 현재 페이즈 한마디
        private int countEvidencePhoto;             // 현재 페이즈 증거사진 개수
        private List<String> evidencePhotoUrls;     // 각 증거사진 정보
    }
}
