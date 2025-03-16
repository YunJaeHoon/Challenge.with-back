package Challenge.with_back.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CustomSuccessCode
{
    SUCCESS("응답 분기가 없는 요청에 대한 성공 응답");

    private final String message;
}
