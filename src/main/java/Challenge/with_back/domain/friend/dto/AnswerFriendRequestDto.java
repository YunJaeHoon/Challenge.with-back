package Challenge.with_back.domain.friend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class AnswerFriendRequestDto
{
    @NotNull(message = "친구 요청 데이터 ID를 입력해주세요.")
    private Long friendRequestId;
}
