package Challenge.with_back.domain.friend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class SendFriendRequestDto
{
    @NotNull(message = "친구 요청을 보낼 사용자 ID를 입력해주세요.")
    private Long receiverId;
}
