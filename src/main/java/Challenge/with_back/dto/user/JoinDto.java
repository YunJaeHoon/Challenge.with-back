package Challenge.with_back.dto.user;

import lombok.Getter;

@Getter
public class JoinDto
{
    private String email;
    private String password;
    private String nickname;
    private boolean allowEmailMarketing;
    private String code;
}
