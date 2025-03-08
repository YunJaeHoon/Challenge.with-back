package Challenge.with_back.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SuccessResponseDto
{
    private final String message;
    private final Object data;
}
