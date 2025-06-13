package Challenge.with_back.domain.email;

import Challenge.with_back.common.entity.rdbms.User;
import Challenge.with_back.common.enums.LoginMethod;
import Challenge.with_back.common.exception.CustomException;
import Challenge.with_back.common.exception.CustomExceptionCode;
import Challenge.with_back.common.repository.rdbms.UserRepository;
import Challenge.with_back.domain.account.service.AccountService;
import Challenge.with_back.domain.account.util.AccountValidator;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class ResetPasswordEmailFactory extends EmailFactory
{
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    
    // 생성자
    public ResetPasswordEmailFactory(JavaMailSender javaMailSender, UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        super(javaMailSender);
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    @Transactional
    public Email createEmail(String to)
    {
        // 무작위 비밀번호를 생성하는 랜덤 객체
        SecureRandom secureRandom = new SecureRandom();

        // 무작위 비밀번호 생성
        String password = IntStream.range(0, 10)
                .map(i -> secureRandom.nextInt(26) + 'a')
                .mapToObj(c -> String.valueOf((char) c))
                .collect(Collectors.joining());

        // 계정 존재 확인
        User user = userRepository.findByEmailAndLoginMethod(to, LoginMethod.NORMAL)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.USER_NOT_FOUND, to));

        // 비밀번호 변경
        user.resetPassword(bCryptPasswordEncoder.encode(password));
        userRepository.save(user);

        return Email.builder()
                       .to(to)
                       .subject("Challenge.with 비밀번호 초기화")
                       .content(String.format(
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
                       ))
                       .build();
    }
}
