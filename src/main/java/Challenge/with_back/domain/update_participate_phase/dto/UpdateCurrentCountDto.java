package Challenge.with_back.domain.update_participate_phase.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateCurrentCountDto
{
    @NotNull(message = "달성 개수를 입력해주세요.")
    @Min(value = 0, message = "달성 개수는 0개 이상이어야 합니다.")
    private int value;  // 달성 개수 값
}
