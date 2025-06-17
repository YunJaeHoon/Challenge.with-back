package Challenge.with_back.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 챌린지 색 테마 enum
@AllArgsConstructor
@Getter
public enum ChallengeColorTheme
{
    RED("빨강"),
    ORANGE("주황"),
    YELLOW("노랑"),
    GREEN("초록"),
    SKYBLUE("하늘"),
    BLUE("파랑"),
    PURPLE("보라"),
    PINK("분홍"),
    GRAY("회색");

    private final String description;
}
