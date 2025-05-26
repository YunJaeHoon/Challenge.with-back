package Challenge.with_back.domain.account.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UserRoleDto
{
    private String role;        // 권한
    private Boolean isPremium;  // 프리미엄 여부
}
