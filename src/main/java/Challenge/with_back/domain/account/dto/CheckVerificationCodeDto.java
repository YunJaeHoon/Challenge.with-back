package Challenge.with_back.domain.account.dto;

import lombok.Getter;

@Getter
public class CheckVerificationCodeDto
{
    private String email;
    private String code;
}
