package Challenge.with_back.common.response.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CustomException extends RuntimeException
{
    private CustomExceptionCode errorCode;
    private Object data;
}
