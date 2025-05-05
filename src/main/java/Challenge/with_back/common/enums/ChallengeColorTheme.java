package Challenge.with_back.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 챌린지 색 테마 enum
@AllArgsConstructor
@Getter
public enum ChallengeColorTheme
{
    RED("빨강", "#FF3D00"),
    ORANGE("주황", "#FF9000"),
    YELLOW("노랑", "#FFD400"),
    GREEN("초록", "#30DE20"),
    BLUE("파랑", "#59C5FF"),
    WHITE("하양", "#FFFFFF"),
    GRAY("회색", "#C4C4C4");

    private final String description;
    private final String color;
}
