package Challenge.with_back.common.response.success;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CustomSuccessCode
{
    SUCCESS("응답 분기가 없는 요청에 대한 성공 응답"),

    // 로그인
    SUCCESS_REMEMBER("remember-me 체크를 했을 때의 성공"),
    SUCCESS_FORGET("remember-me 체크를 하지 않았을 때의 성공");

    private final String message;
}
