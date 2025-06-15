package Challenge.with_back.domain.account.service;

import Challenge.with_back.common.entity.redis.CheckVerificationCode;
import Challenge.with_back.common.entity.redis.VerificationCode;
import Challenge.with_back.common.enums.ProfileImage;
import Challenge.with_back.common.repository.rdbms.ParticipateChallengeRepository;
import Challenge.with_back.common.repository.redis.VerificationCodeRepository;
import Challenge.with_back.common.security.CustomUserDetails;
import Challenge.with_back.common.security.jwt.Token;
import Challenge.with_back.domain.account.dto.*;
import Challenge.with_back.domain.notification.WelcomeNotificationFactory;
import Challenge.with_back.domain.account.util.AccountValidator;
import Challenge.with_back.domain.email.ResetPasswordEmailFactory;
import Challenge.with_back.domain.email.VerificationCodeEmailFactory;
import Challenge.with_back.common.exception.CustomExceptionCode;
import Challenge.with_back.common.repository.redis.CheckVerificationCodeRepository;
import Challenge.with_back.common.entity.rdbms.User;
import Challenge.with_back.common.enums.AccountRole;
import Challenge.with_back.common.enums.LoginMethod;
import Challenge.with_back.common.exception.CustomException;
import Challenge.with_back.common.repository.rdbms.UserRepository;
import Challenge.with_back.common.security.jwt.JwtUtil;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AccountService
{
    private final UserRepository userRepository;
    private final VerificationCodeRepository verificationCodeRepository;
    private final CheckVerificationCodeRepository checkVerificationCodeRepository;
    private final ParticipateChallengeRepository participateChallengeRepository;

    private final AccountValidator accountValidator;
    private final JwtUtil jwtUtil;

    private final VerificationCodeEmailFactory verificationCodeEmailFactory;
    private final ResetPasswordEmailFactory resetPasswordEmailFactory;
    private final WelcomeNotificationFactory welcomeNotificationFactory;
    
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Value("${PROFILE_IMAGE_BUCKET_URL}")
    String profileImageBucketUrl;

    /// 서비스

    // 회원가입
    @Transactional
    public void join(JoinDto dto)
    {
        // 인증번호 확인 정보 존재 여부 확인
        if(checkVerificationCodeRepository.findByEmail(dto.getEmail()).isEmpty())
            throw new CustomException(CustomExceptionCode.CHECK_VERIFICATION_CODE_NOT_FOUND, dto.getEmail());

        // 계정 중복 확인
        if(userRepository.findByEmailAndLoginMethod(dto.getEmail(), LoginMethod.NORMAL).isPresent())
            throw new CustomException(CustomExceptionCode.ALREADY_EXISTING_USER, dto.getEmail());

        // 데이터 형식 체크
        accountValidator.checkPasswordFormat(dto.getPassword());
        accountValidator.checkNicknameFormat(dto.getNickname());

        // 계정 생성
        User user = User.builder()
                .loginMethod(LoginMethod.NORMAL)
                .email(dto.getEmail())
                .password(bCryptPasswordEncoder.encode(dto.getPassword()))
                .nickname(dto.getNickname())
                .profileImageUrl(ProfileImage.BASIC.getUrl())
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
        welcomeNotificationFactory.createNotification(user, null);
    }

    // 사용자 기본 정보 조회
    @Transactional(readOnly = true)
    public BasicUserInfoDto getBasicInfo(User user)
    {
        return BasicUserInfoDto.builder()
                .role(user.getAccountRole().name())
                .isPremium(user.isPremium())
                .profileImageUrl(profileImageBucketUrl + user.getProfileImageUrl())
                .countUnreadNotification(user.getCountUnreadNotification())
                .build();
    }

    // Access token 재발급
    @Transactional(readOnly = true)
    public Cookie reissueAccessToken(String refreshToken)
    {
        // Refresh token 존재 여부 체크
        if(refreshToken == null)
            throw new CustomException(CustomExceptionCode.REFRESH_TOKEN_NOT_FOUND, null);

        // 토큰이 유효한지 체크
        if(!jwtUtil.checkToken(refreshToken))
            throw new CustomException(CustomExceptionCode.INVALID_REFRESH_TOKEN, refreshToken);

        // 계정 id 추출
        Long id = jwtUtil.getId(refreshToken);

        // 계정 존재 확인
        userRepository.findById(id)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.USER_NOT_FOUND, id));

        // Access token 발급
        String accessToken = jwtUtil.getToken(id, Token.ACCESS_TOKEN);

        return jwtUtil.parseTokenToCookie(accessToken, Token.ACCESS_TOKEN);
    }

    // 이메일 인증번호 전송
    @Transactional
    public void sendVerificationCode(SendVerificationCodeDto dto)
    {
        // 이메일 전송
        verificationCodeEmailFactory.sendEmail(dto.getEmail());
    }

    // 이메일 인증번호 확인: 회원가입
    @Transactional
    public void checkVerificationCodeForJoin(CheckVerificationCodeDto dto)
    {
        // 인증번호 확인
        checkVerificationCodeCorrectness(dto.getEmail(), dto.getCode());

        // 계정 중복 확인
        if(userRepository.findByEmailAndLoginMethod(dto.getEmail(), LoginMethod.NORMAL).isPresent())
            throw new CustomException(CustomExceptionCode.ALREADY_EXISTING_USER, dto.getEmail());
    }

    // 이메일 인증번호 확인: 비밀번호 초기화
    @Transactional
    public void checkVerificationCodeForResetPassword(CheckVerificationCodeDto dto)
    {
        // 인증번호 확인
        checkVerificationCodeCorrectness(dto.getEmail(), dto.getCode());

        // 계정 존재 확인
        if(userRepository.findByEmailAndLoginMethod(dto.getEmail(), LoginMethod.NORMAL).isPresent())
            throw new CustomException(CustomExceptionCode.ALREADY_EXISTING_USER, dto.getEmail());
    }

    // 비밀번호 초기화
    @Transactional
    public void resetPassword(CheckVerificationCodeDto dto)
    {
        // 인증번호 확인
        checkVerificationCodeCorrectness(dto.getEmail(), dto.getCode());

         // 이메일 전송
        resetPasswordEmailFactory.sendEmail(dto.getEmail());
    }

    /// 공통 로직

    // Authentication 데이터에서 User 엔티티 추출
    public User getUserFromAuthentication(Authentication authentication)
    {
        if(authentication == null || !authentication.isAuthenticated())
            throw new CustomException(CustomExceptionCode.NOT_LOGIN, null);

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof CustomUserDetails userDetails))
            throw new CustomException(CustomExceptionCode.INVALID_AUTHENTICATION, null);

        // 사용자 객체 반환
        return userDetails.getUser();
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

        // 새로운 인증번호 확인 정보 등록
        CheckVerificationCode checkVerificationCode = CheckVerificationCode.builder()
                .email(email)
                .build();

        // 생성한 인증번호 확인 정보 저장
        checkVerificationCodeRepository.save(checkVerificationCode);
    }

    // 사용자가 참여 중인 챌린지 개수가 최대인지 확인
    @Transactional(readOnly = true)
    public boolean isParticipatingInMaxChallenges(User user)
    {
        return participateChallengeRepository.countAllOngoing(user.getId()) >= user.getMaxChallengeCount();
    }
}
