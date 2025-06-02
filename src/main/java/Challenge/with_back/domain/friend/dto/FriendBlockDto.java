package Challenge.with_back.domain.friend.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FriendBlockDto
{
    private Long friendBlockId;         // 친구 차단 데이터 ID
    private Long userId;                // 차단한 사용자 ID

    private String email;               // 차단한 사용자 이메일
    private String nickname;            // 차단한 사용자 닉네임
    private String profileImageUrl;     // 차단한 사용자 프로필 이미지 URL
}
