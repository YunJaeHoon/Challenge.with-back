package Challenge.with_back.domain.email;

import Challenge.with_back.entity.redis.VerificationCode;
import Challenge.with_back.repository.redis.VerificationCodeRepository;
import Challenge.with_back.utils.UserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class VerificationCodeEmailFactory implements EmailFactory
{
    private final UserUtil userUtil;
    private final VerificationCodeRepository verificationCodeRepository;

    @Override
    @Transactional
    public Email createEmail(String to)
    {
        // 무작위 인증번호를 생성하는 랜덤 객체
        SecureRandom secureRandom = new SecureRandom();

        // 무작위 인증번호 생성
        String code = secureRandom.ints(6, 0, 10)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining());

        // 해당 이메일을 통해 이미 인증번호를 발급했다면 삭제
        userUtil.deleteVerificationCode(to);

        // 새로운 인증번호 정보 등록
        VerificationCode verificationCode = VerificationCode.builder()
                .email(to)
                .code(code)
                .countWrong(1)
                .build();

        // 생성한 인증번호 저장
        verificationCodeRepository.save(verificationCode);

        return new VerificationCodeEmail(code);
    }
}
