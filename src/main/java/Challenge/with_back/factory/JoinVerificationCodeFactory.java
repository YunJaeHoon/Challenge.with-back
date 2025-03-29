package Challenge.with_back.factory;

import Challenge.with_back.entity.VerificationCode;
import Challenge.with_back.product.Email;
import Challenge.with_back.product.JoinVerificationCode;
import Challenge.with_back.repository.VerificationCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JoinVerificationCodeFactory implements EmailFactory
{
    private final VerificationCodeRepository verificationCodeRepository;

    // 이메일 제목 및 내용 생성
    @Override
    @Transactional
    public Email createEmail(String to)
    {
        // 인증번호 생성
        String authenticationCode = createVerificationCode(to);

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

        return JoinVerificationCode.builder()
                .subject("Challenge,with 인증번호")
                .content(content)
                .build();
    }

    // 인증번호 생성
    private String createVerificationCode(String to)
    {
        // 무작위 인증번호를 생성하는 랜덤 객체
        SecureRandom secureRandom = new SecureRandom();

        // 무작위 인증번호 생성
        String authenticationNumber = secureRandom.ints(6, 0, 10)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining());

        // 해당 이메일을 통해 이미 인증번호를 발급했다면 삭제
        Optional<VerificationCode> existingVerificationCode = verificationCodeRepository.findByEmail(to);
        existingVerificationCode.ifPresent(verificationCodeRepository::delete);

        // 새로운 인증번호 정보 등록
        VerificationCode verificationCode = VerificationCode.builder()
                .email(to)
                .code(authenticationNumber)
                .countWrong(5)
                .build();

        verificationCodeRepository.save(verificationCode);

        return authenticationNumber;
    }
}
