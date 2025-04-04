package Challenge.with_back.dto.user;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UserRoleDto
{
    private String role;    // 권한
}
