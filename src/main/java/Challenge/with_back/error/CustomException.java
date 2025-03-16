package Challenge.with_back.error;

import Challenge.with_back.dto.response.CustomErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CustomException extends RuntimeException
{
    private CustomErrorCode errorCode;
    private Object data;
}
