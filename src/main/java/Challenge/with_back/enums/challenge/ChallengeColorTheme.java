package Challenge.with_back.enums.challenge;

import lombok.AllArgsConstructor;

// 챌린지 색 테마 enum
@AllArgsConstructor
public enum ChallengeColorTheme
{
    RED("빨강"),
    ORANGE("주황"),
    YELLOW("노랑"),
    GREEN("초록"),
    BLUE("파랑"),
    WHITE("하양"),
    BLACK("검정");

    private final String description;
}
