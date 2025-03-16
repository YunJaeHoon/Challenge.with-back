package Challenge.with_back.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum CustomErrorCode
{
    EXAMPLE(HttpStatus.NOT_FOUND, "예시입니다");

    private final HttpStatus httpStatus;
    private final String message;
}
