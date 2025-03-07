package Challenge.with_back.enums.account;

import lombok.AllArgsConstructor;

// 로그인 방식 enum
@AllArgsConstructor
public enum LoginMethod
{
    NORMAL("일반 로그인"),
    GOOGLE("구글 로그인"),
    KAKAO("카카오 로그인"),
    NAVER("네이버 로그인");

    private final String description;
}
