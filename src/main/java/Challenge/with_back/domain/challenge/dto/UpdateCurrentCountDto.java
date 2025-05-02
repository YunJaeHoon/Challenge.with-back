package Challenge.with_back.domain.challenge.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateCurrentCountDto
{
    private int value;  // 달성 개수 값
}
