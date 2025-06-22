package Challenge.with_back.domain.challenge.dto;

import Challenge.with_back.common.elastic_search.ChallengeDocument;
import Challenge.with_back.common.entity.Challenge;
import Challenge.with_back.common.entity.User;
import Challenge.with_back.common.enums.ChallengeColorTheme;
import Challenge.with_back.common.enums.ChallengeUnit;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class BasicChallengeInfoPageDto
{
    private List<BasicChallengeInfo> basicChallengeInfoList;    // 챌린지 정보 리스트
    private int currentPage;                                    // 현재 페이지
    private int totalPages;                                     // 전체 페이지 개수

    public static BasicChallengeInfoPageDto of(Map<ChallengeDocument, Integer> map, int currentPage, int totalPages)
    {
        return BasicChallengeInfoPageDto.builder()
                .basicChallengeInfoList(map.entrySet().stream().map(entry ->
                        BasicChallengeInfo.from(entry.getKey(), entry.getValue()))
                        .toList())
                .currentPage(currentPage)
                .totalPages(totalPages)
                .build();
    }

    @Getter
    @Builder
    private static class BasicChallengeInfo
    {
        private ChallengeInfo challengeInfo;    // 챌린지 정보
        private SuperAdminInfo superAdminInfo;  // 최고 관리자 정보

        protected static BasicChallengeInfo from(ChallengeDocument challengeDocument, int currentParticipantCount)
        {
            return BasicChallengeInfo.builder()
                    .challengeInfo(ChallengeInfo.from(challengeDocument, currentParticipantCount))
                    .superAdminInfo(SuperAdminInfo.of(challengeDocument.getSuperAdminId(), challengeDocument.getSuperAdminNickname(), challengeDocument.getSuperAdminProfileImageUrl()))
                    .build();
        }

        @Getter
        @Builder
        private static class ChallengeInfo
        {
            private Long challengeId;               // 챌린지 ID
            private String icon;                    // 아이콘
            private ChallengeColorTheme colorTheme; // 색 테마
            private String name;                    // 챌린지 이름
            private String description;             // 챌린지 설명
            private int currentParticipantCount;    // 챌린지 현재 참여자 인원수
            private int maxParticipantCount;        // 챌린지 최대 참여자 인원수
            private int goalCount;                  // 챌린지 목표 개수
            private ChallengeUnit unit;             // 챌린지 단위
            private LocalDate startDate;            // 챌린지 시작 날짜

            protected static ChallengeInfo from(ChallengeDocument challengeDocument, int currentParticipantCount)
            {
                return ChallengeInfo.builder()
                        .challengeId(challengeDocument.getId())
                        .icon(challengeDocument.getIcon())
                        .colorTheme(ChallengeColorTheme.valueOf(challengeDocument.getColorTheme()))
                        .name(challengeDocument.getName())
                        .description(challengeDocument.getDescription())
                        .currentParticipantCount(currentParticipantCount)
                        .maxParticipantCount(challengeDocument.getMaxParticipantCount())
                        .goalCount(challengeDocument.getGoalCount())
                        .unit(ChallengeUnit.valueOf(challengeDocument.getUnit()))
                        .startDate(challengeDocument.getCreatedAt().toLocalDate())
                        .build();
            }
        }

        @Getter
        @Builder
        private static class SuperAdminInfo
        {
            private Long userId;                    // 사용자 ID
            private String nickname;                // 닉네임
            private String profileImageUrl;         // 프로필 이미지 URL

            protected static SuperAdminInfo of(Long userId, String nickname, String profileImageUrl)
            {
                return SuperAdminInfo.builder()
                        .userId(userId)
                        .nickname(nickname)
                        .profileImageUrl(profileImageUrl)
                        .build();
            }
        }
    }
}
