package Challenge.with_back.domain.friend.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FriendDto
{
    private Long friendId;      // 친구 데이터 ID
    private Long userId;        // 친구를 맺은 사용자 ID

    private String email;               // 친구 이메일
    private String nickname;            // 친구 닉네임
    private String profileImageUrl;     // 친구 프로필 이미지 URL
}
