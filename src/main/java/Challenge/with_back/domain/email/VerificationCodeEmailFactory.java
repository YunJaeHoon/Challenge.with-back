package Challenge.with_back.domain.email;

import Challenge.with_back.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VerificationCodeEmailFactory implements EmailFactory
{
    private final UserService userService;

    @Override
    public Email createEmail(String to)
    {
        // 인증번호 생성
        String authenticationCode = userService.createVerificationCode(to);

        // 이메일 내용 생성
        String content = String.format(
                """
                    <div style="display: flex; flex-direction: column; align-items: center; margin: 50px;">
                        <div style="display: flex; flex-direction: column; align-items: center; margin-top: 50px;">
                            <div>
                                <img src="https://s3.ap-northeast-2.amazonaws.com/challenge.with-basic/LogoImage.svg" />
                                <img src="https://s3.ap-northeast-2.amazonaws.com/challenge.with-basic/LogoText.svg" style="margin-left: 10px;" />
                            </div>
                        </div>
                        <div style="width: 120%%; height: 0.62px; overflow: visible; background-color: #D4D4D4; margin-top: 50px;"></div>
                        <div style="font-size: 2.5rem; font-weight: 600; color: #373737; margin-top: 100px;">%s</div>
                        <div style="width: 100%%; font-size: 1.125rem; font-weight: 400; color: #373737; margin-top: 70px; margin-bottom: 70px;">
                            Challenge.with 회원가입을 위한 인증번호입니다. <br />
                            위 인증번호를 입력하여 본인 확인을 해주시기 바랍니다.
                        </div>
                    </div>
                """,
                authenticationCode
        );

        return VerificationCodeEmail.builder()
                .subject("Challenge.with 인증번호")
                .content(content)
                .build();
    }
}
