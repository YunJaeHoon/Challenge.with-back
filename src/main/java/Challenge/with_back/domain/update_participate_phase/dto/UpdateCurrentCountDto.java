package Challenge.with_back.domain.update_participate_phase.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateCurrentCountDto
{
    private int value;  // 달성 개수 값
}
