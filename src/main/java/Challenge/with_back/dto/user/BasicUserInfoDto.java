package Challenge.with_back.dto.user;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BasicUserInfoDto
{
    private String email;
    private String nickname;
    private String profileImageUrl;
    private String role;
    private Boolean isPremium;
    private int countUnreadNotification;
}
