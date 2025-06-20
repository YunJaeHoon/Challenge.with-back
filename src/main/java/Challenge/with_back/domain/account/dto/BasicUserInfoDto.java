package Challenge.with_back.domain.account.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BasicUserInfoDto
{
    private String role;
    private Boolean isPremium;
    private String profileImageUrl;
    private int countUnreadNotification;
}
