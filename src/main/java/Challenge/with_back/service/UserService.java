package Challenge.with_back.service;

import Challenge.with_back.dto.user.JoinDto;
import Challenge.with_back.entity.User;
import Challenge.with_back.enums.account.AccountRole;
import Challenge.with_back.enums.account.LoginMethod;
import Challenge.with_back.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class UserService
{
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    // 회원가입
    @Transactional
    public void join(JoinDto dto)
    {
        User user = User.builder()
                .loginMethod(LoginMethod.NORMAL)
                .email(dto.getEmail())
                .password(bCryptPasswordEncoder.encode(dto.getPassword()))
                .nickname(dto.getNickname())
                .profileImageUrl("/기본경로")
                .selfIntroduction("")
                .allowEmailMarketing(dto.isAllowEmailMarketing())
                .premiumExpirationDate(LocalDate.now().minusDays(1))
                .countUnreadNotification(0)
                .paymentInformationEmail(dto.getEmail())
                .accountRole(AccountRole.USER)
                .build();

        userRepository.save(user);
    }
}
