package Challenge.with_back.service;

import Challenge.with_back.dto.response.CustomExceptionCode;
import Challenge.with_back.dto.token.AccessTokenDto;
import Challenge.with_back.dto.user.JoinDto;
import Challenge.with_back.entity.User;
import Challenge.with_back.enums.account.AccountRole;
import Challenge.with_back.enums.account.LoginMethod;
import Challenge.with_back.exception.CustomException;
import Challenge.with_back.repository.UserRepository;
import Challenge.with_back.security.JwtUtil;
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
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    // 계정 생성
    @Transactional
    public void createUser(JoinDto dto, AccountRole role)
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
                .accountRole(role)
                .build();

        userRepository.save(user);
    }

    // 권한 확인
    public AccountRole getRole(User user)
    {
        return user.getAccountRole();
    }

    // Access token 재발급
    public AccessTokenDto reissueAccessToken(String refreshToken)
    {
        // Refresh token 존재 여부 체크
        if(refreshToken == null)
            throw new CustomException(CustomExceptionCode.REFRESH_TOKEN_NOT_FOUND, refreshToken);

        // 토큰이 유효한지 체크
        if(!jwtUtil.checkToken(refreshToken))
            throw new CustomException(CustomExceptionCode.INVALID_REFRESH_TOKEN, refreshToken);

        // User 엔티티 추출
        Long id = jwtUtil.getId(refreshToken);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.USER_NOT_FOUND, id));

        // Access token 발급
        String accessToken = jwtUtil.getToken(id, true);

        return AccessTokenDto.builder()
                .accessToken(accessToken)
                .build();
    }
}
