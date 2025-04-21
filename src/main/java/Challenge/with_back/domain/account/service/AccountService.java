package Challenge.with_back.domain.account.service;

import Challenge.with_back.domain.account.dto.*;
import Challenge.with_back.domain.email.kafka.EmailProducer;
import Challenge.with_back.domain.notification.WelcomeNotificationFactory;
import Challenge.with_back.domain.account.util.AccountUtil;
import Challenge.with_back.domain.email.ResetPasswordEmailFactory;
import Challenge.with_back.domain.email.VerificationCodeEmailFactory;
import Challenge.with_back.common.response.exception.CustomExceptionCode;
import Challenge.with_back.repository.redis.CheckVerificationCodeRepository;
import Challenge.with_back.security.dto.AccessTokenDto;
import Challenge.with_back.entity.rdbms.User;
import Challenge.with_back.common.enums.AccountRole;
import Challenge.with_back.common.enums.LoginMethod;
import Challenge.with_back.common.response.exception.CustomException;
import Challenge.with_back.repository.rdbms.UserRepository;
import Challenge.with_back.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AccountService
{
    private final UserRepository userRepository;
    private final CheckVerificationCodeRepository checkVerificationCodeRepository;

    private final AccountUtil accountUtil;
    private final JwtUtil jwtUtil;

    private final VerificationCodeEmailFactory verificationCodeEmailFactory;
    private final ResetPasswordEmailFactory resetPasswordEmailFactory;
    private final WelcomeNotificationFactory welcomeNotificationFactory;
    
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Value("${PROFILE_IMAGE_BUCKET_URL}")
    String profileImageBucketUrl;

    // 회원가입
    @Transactional
    public void join(JoinDto dto)
    {
        // 인증번호 확인 정보 존재 여부 확인
        if(checkVerificationCodeRepository.findByEmail(dto.getEmail()).isEmpty())
            throw new CustomException(CustomExceptionCode.CHECK_VERIFICATION_CODE_NOT_FOUND, dto.getEmail());

        // 계정 중복 확인
        accountUtil.shouldNotExistingUser(dto.getEmail());

        // 데이터 형식 체크
        accountUtil.checkPasswordFormat(dto.getPassword());
        accountUtil.checkNicknameFormat(dto.getNickname());

        // 계정 생성
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
                .accountRole(AccountRole.USER)
                .build();

        // 생성한 계정 저장
        userRepository.save(user);

        // 회원가입 환영 알림 생성
        welcomeNotificationFactory.createNotification(user);
    }

    // 계정 권한 확인
    public UserRoleDto getRole(User user)
    {
        return UserRoleDto.builder()
                .role(user.getAccountRole().name())
                .build();
    }

    // 사용자 기본 정보 조회
    public BasicUserInfoDto getBasicInfo(User user)
    {
        return BasicUserInfoDto.builder()
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .role(user.getAccountRole().name())
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

    // 이메일 인증번호 전송
    @Transactional
    public void sendVerificationCode(SendVerificationCodeDto dto)
    {
        // 이메일 전송
        verificationCodeEmailFactory.sendEmail(dto.getEmail());
    }

    // 이메일 인증번호 확인: 회원가입
    public void checkVerificationCodeForJoin(CheckVerificationCodeDto dto)
    {
        // 인증번호 확인
        accountUtil.checkVerificationCodeCorrectness(dto.getEmail(), dto.getCode());

        // 계정 중복 확인
        accountUtil.shouldNotExistingUser(dto.getEmail());
    }

    // 이메일 인증번호 확인: 비밀번호 초기화
    public void checkVerificationCodeForResetPassword(CheckVerificationCodeDto dto)
    {
        // 인증번호 확인
        accountUtil.checkVerificationCodeCorrectness(dto.getEmail(), dto.getCode());

        // 계정 존재 확인
        accountUtil.shouldExistingUser(dto.getEmail());
    }

    // 비밀번호 초기화
    @Transactional
    public void resetPassword(CheckVerificationCodeDto dto)
    {
        // 인증번호 확인
        accountUtil.checkVerificationCodeCorrectness(dto.getEmail(), dto.getCode());

         // 이메일 전송
        resetPasswordEmailFactory.sendEmail(dto.getEmail());
    }
}
