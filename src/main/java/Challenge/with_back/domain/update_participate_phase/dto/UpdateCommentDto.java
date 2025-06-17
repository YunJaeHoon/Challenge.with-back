package Challenge.with_back.domain.update_participate_phase.dto;

import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateCommentDto
{
    @Size(max = 1000, message = "한마디는 최대 1000자까지 입력할 수 있습니다.")
    private String comment; // 한마디
}
