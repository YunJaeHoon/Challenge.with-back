package Challenge.with_back.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum CustomExceptionCode
{
    // OAuth 2.0 예외
    PROVIDER_NOT_FOUND(HttpStatus.NOT_FOUND, "제공자를 찾을 수 없습니다."),
    NOT_VALID_PROVIDER(HttpStatus.UNAUTHORIZED, "제공자를 찾을 수 없습니다."),

    // 계정 관련 예외
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),

    UNEXPECTED_ERROR(HttpStatus.UNAUTHORIZED, "예기치 못한 에러가 발생하였습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
