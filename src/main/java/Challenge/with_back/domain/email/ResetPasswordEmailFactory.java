package Challenge.with_back.domain.email;

import Challenge.with_back.entity.rdbms.User;
import Challenge.with_back.repository.rdbms.UserRepository;
import Challenge.with_back.utils.UserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class ResetPasswordEmailFactory implements EmailFactory
{
    private final UserUtil userUtil;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

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
        User user = userUtil.shouldExistingUser(to);

        // 비밀번호 변경
        user.resetPassword(bCryptPasswordEncoder.encode(password));
        userRepository.save(user);

        return new ResetPasswordEmail(password);
    }
}
