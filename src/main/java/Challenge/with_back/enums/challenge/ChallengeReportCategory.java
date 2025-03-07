package Challenge.with_back.enums.challenge;

import lombok.AllArgsConstructor;

// 챌린지 신고 분류 enum
@AllArgsConstructor
public enum ChallengeReportCategory
{
    CONTAIN_PROFANITY("비속어 포함"),
    CONTAIN_SEXUAL_CONTENT("성적 내용 포함");

    private final String description;
}
