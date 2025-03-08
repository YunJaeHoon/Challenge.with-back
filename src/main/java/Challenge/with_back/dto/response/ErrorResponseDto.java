package Challenge.with_back.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponseDto
{
    private final String code;
    private final String message;
    private final Object data;
}
