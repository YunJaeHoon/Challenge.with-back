package Challenge.with_back.common.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ExceptionResponseDto
{
    private String code;        // 에러 코드
    private String message;     // 메시지
    private Object data;        // 데이터
}
