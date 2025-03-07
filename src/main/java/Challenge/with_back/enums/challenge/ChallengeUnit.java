package Challenge.with_back.enums.challenge;

import lombok.AllArgsConstructor;

// 챌린지 단위 enum
@AllArgsConstructor
public enum ChallengeUnit
{
    DAILY("매일"),
    WEEKLY("매주"),
    MONTHLY("매월");

    private final String description;
}
