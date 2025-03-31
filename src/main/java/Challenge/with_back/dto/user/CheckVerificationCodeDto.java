package Challenge.with_back.dto.user;

import lombok.Getter;

@Getter
public class CheckVerificationCodeDto
{
    private String email;
    private String code;
}
