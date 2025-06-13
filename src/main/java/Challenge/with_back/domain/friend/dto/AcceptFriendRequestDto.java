package Challenge.with_back.domain.friend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class AcceptFriendRequestDto
{
    @NotNull(message = "수락할 친구 요청 데이터 ID를 입력해주세요.")
    private Long friendRequestId;
}
