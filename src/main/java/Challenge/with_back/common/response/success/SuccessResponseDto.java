package Challenge.with_back.common.response.success;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SuccessResponseDto
{
    private String code;        // 성공 코드
    private String message;     // 메시지
    private Object data;        // 데이터
}
