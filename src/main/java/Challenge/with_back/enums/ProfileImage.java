package Challenge.with_back.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 프로필 이미지 enum
@AllArgsConstructor
@Getter
public enum ProfileImage
{
	BASIC("/profile-image_basic.svg");
	
	private final String url;
}
