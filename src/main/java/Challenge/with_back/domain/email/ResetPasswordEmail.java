package Challenge.with_back.domain.email;

public class ResetPasswordEmail extends Email
{
    ResetPasswordEmail(String password)
    {
        super();

        this.subject = "Challenge.with 비밀번호 초기화";
        this.content = String.format(
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
                            Challenge.with 사용자 비밀번호가 정상적으로 초기화되었습니다. <br />
                            위 비밀번호를 입력하여 로그인 해주시기 바랍니다.
                        </div>
                    </div>
                """,
                password
        );
    }
}
