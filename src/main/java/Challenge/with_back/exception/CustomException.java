package Challenge.with_back.exception;

import Challenge.with_back.dto.response.CustomExceptionCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CustomException extends RuntimeException
{
    private CustomExceptionCode errorCode;
    private Object data;
}
