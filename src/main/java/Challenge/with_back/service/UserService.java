package Challenge.with_back.service;

import Challenge.with_back.dto.response.CustomExceptionCode;
import Challenge.with_back.dto.token.AccessTokenDto;
import Challenge.with_back.dto.user.BasicUserInfoDto;
import Challenge.with_back.dto.user.JoinDto;
import Challenge.with_back.entity.User;
import Challenge.with_back.entity.VerificationCode;
import Challenge.with_back.enums.account.AccountRole;
import Challenge.with_back.enums.account.LoginMethod;
import Challenge.with_back.exception.CustomException;
import Challenge.with_back.repository.UserRepository;
import Challenge.with_back.repository.VerificationCodeRepository;
import Challenge.with_back.security.JwtUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class UserService
{
    private final UserRepository userRepository;
    private final VerificationCodeRepository verificationCodeRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Value("${PROFILE_IMAGE_BUCKET_URL}")
    String profileImageBucketUrl;

    // 계정 생성
    @Transactional
    public void createUser(JoinDto dto, AccountRole role)
    {
        User user = User.builder()
                .loginMethod(LoginMethod.NORMAL)
                .email(dto.getEmail())
                .password(bCryptPasswordEncoder.encode(dto.getPassword()))
                .nickname(dto.getNickname())
                .profileImageUrl(profileImageBucketUrl + "/profile-image_basic.svg")
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

    // 사용자 기본 정보 조회
    public BasicUserInfoDto getBasicInfo(User user)
    {
        return BasicUserInfoDto.builder()
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .isPremium(user.getPremiumExpirationDate().isAfter(LocalDate.now()))
                .countUnreadNotification(user.getCountUnreadNotification())
                .build();
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

        // 계정 id 추출
        Long id = jwtUtil.getId(refreshToken);

        // 계정 존재 확인
        userRepository.findById(id)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.USER_NOT_FOUND, id));

        // Access token 발급
        String accessToken = jwtUtil.getToken(id, true);

        return AccessTokenDto.builder()
                .accessToken(accessToken)
                .build();
    }

    // 인증번호 일치 여부 확인
    @Transactional(noRollbackFor = CustomException.class)
    public void checkVerificationCodeCorrectness(String email, String code)
    {
        // 인증번호 존재 여부 확인
        VerificationCode verificationCode = verificationCodeRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.VERIFICATION_CODE_NOT_FOUND, email));

        // 인증번호 일치 여부 확인
        if(!verificationCode.getCode().equals(code))
        {
            if(verificationCode.getCountWrong() >= 5)
            {
                verificationCodeRepository.delete(verificationCode);

                throw new CustomException(CustomExceptionCode.TOO_MANY_WRONG_VERIFICATION_CODE, null);
            }
            else
            {
                verificationCode.increaseCountWrong();
                verificationCodeRepository.save(verificationCode);

                throw new CustomException(CustomExceptionCode.WRONG_VERIFICATION_CODE, null);
            }
        }
    }

    // 인증번호 만료 여부 확인
    @Transactional(noRollbackFor = CustomException.class)
    public void checkVerificationCodeExpiration(String email, String code)
    {
        // 인증번호 존재 여부 확인
        VerificationCode verificationCode = verificationCodeRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.VERIFICATION_CODE_NOT_FOUND, email));

        // 인증번호 만료 여부 확인
        if(LocalDateTime.now().isAfter(verificationCode.getCreatedAt().plusMinutes(10)))
        {
            verificationCodeRepository.delete(verificationCode);

            throw new CustomException(CustomExceptionCode.EXPIRED_VERIFICATION_CODE, null);
        }
    }

    // 인증번호 삭제
    @Transactional
    public void deleteVerificationCode(String email)
    {
        Optional<VerificationCode> verificationCode = verificationCodeRepository.findByEmail(email);
        verificationCode.ifPresent(verificationCodeRepository::delete);
    }

    // 일반 로그인 사용자 중복 여부 확인
    public void checkNormalUserDuplication(String email)
    {
        if(userRepository.findByEmailAndLoginMethod(email, LoginMethod.NORMAL).isPresent())
            throw new CustomException(CustomExceptionCode.ALREADY_EXISTING_USER, email);

    }
}
