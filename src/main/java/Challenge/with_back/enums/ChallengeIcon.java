package Challenge.with_back.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 챌린지 아이콘 enum
@AllArgsConstructor
@Getter
public enum ChallengeIcon
{
	BASIC1("/challenge-icon_basic1.svg"),
	BASIC2("/challenge-icon_basic2.svg"),
	BASIC3("/challenge-icon_basic3.svg"),
	BASIC4("/challenge-icon_basic4.svg"),
	BASIC5("/challenge-icon_basic5.svg");
	
	private final String url;
}
