package Challenge.with_back.domain.account.dto;

import lombok.Getter;

@Getter
public class JoinDto
{
    private String email;
    private String password;
    private String nickname;
    private boolean allowEmailMarketing;
}
