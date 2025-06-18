package Challenge.with_back.domain.challenge.dto;

import Challenge.with_back.common.entity.rdbms.Challenge;
import Challenge.with_back.common.entity.rdbms.ParticipateChallenge;
import Challenge.with_back.common.entity.rdbms.User;
import Challenge.with_back.common.enums.ChallengeColorTheme;
import Challenge.with_back.common.enums.ChallengeRole;
import Challenge.with_back.common.enums.ChallengeUnit;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class ChallengeDetailDto
{
    private ChallengeInfo challengeInfo;                // 챌린지 정보
    private List<ParticipantInfo> participantInfoList;  // 참가자 정보 리스트

    private Boolean isParticipatingChallenge;           // 요청자가 챌린지에 참여 중인가?

    // 요청자가 챌린지에 참여 중인 경우, 현재 페이즈 참여 정보
    // 요청자가 챌린지에 참여 중이지 않은 경우, 챌린지 로드맵 정보
    private ChallengeSubInfo challengeSubInfo;

    public static ChallengeDetailDto from(Challenge challenge,
                                          List<ParticipateChallenge> participateChallengeList,
                                          ChallengeSubInfo challengeSubInfo,
                                          boolean isParticipatingChallenge)
    {
        return ChallengeDetailDto.builder()
                .challengeInfo(ChallengeInfo.from(challenge))
                .participantInfoList(participateChallengeList.stream().map(ParticipantInfo::from).toList())
                .challengeSubInfo(challengeSubInfo)
                .isParticipatingChallenge(isParticipatingChallenge)
                .build();
    }

    @Getter
    @Builder
    private static class ChallengeInfo
    {
        private Long id;                            // 챌린지 ID
        private String icon;                        // 아이콘
        private ChallengeColorTheme colorTheme;     // 색 테마
        private String name;                        // 챌린지 이름
        private String description;                 // 챌린지 설명
        private int maxParticipantCount;            // 챌린지 최대 참여자 인원수
        private int goalCount;                      // 챌린지 목표 개수
        private ChallengeUnit unit;                 // 챌린지 단위
        private Boolean isPublic;                   // 챌린지 공개 여부
        private LocalDate startDate;                // 챌린지 시작 날짜

        protected static ChallengeInfo from(Challenge challenge)
        {
            return ChallengeInfo.builder()
                    .id(challenge.getId())
                    .icon(challenge.getIcon())
                    .colorTheme(challenge.getColorTheme())
                    .name(challenge.getName())
                    .description(challenge.getDescription())
                    .maxParticipantCount(challenge.getMaxParticipantCount())
                    .goalCount(challenge.getGoalCount())
                    .unit(challenge.getUnit())
                    .isPublic(challenge.isPublic())
                    .startDate(challenge.getCreatedAt().toLocalDate())
                    .build();
        }
    }

    @Getter
    @Builder
    private static class ParticipantInfo
    {
        private Long id;                        // 사용자 ID
        private String nickname;                // 닉네임
        private String profileImageUrl;         // 프로필 이미지 URL
        private ChallengeRole challengeRole;    // 챌린지 권한

        protected static ParticipantInfo from(ParticipateChallenge participateChallenge)
        {
            User user = participateChallenge.getUser();

            return ParticipantInfo.builder()
                    .id(user.getId())
                    .nickname(user.getNickname())
                    .profileImageUrl(user.getProfileImageUrl())
                    .challengeRole(participateChallenge.getChallengeRole())
                    .build();
        }
    }
}
