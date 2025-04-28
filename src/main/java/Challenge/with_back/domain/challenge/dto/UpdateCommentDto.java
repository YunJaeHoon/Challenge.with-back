package Challenge.with_back.domain.challenge.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateCommentDto
{
    private String comment;     // 한마디
}
