package Challenge.with_back.service;

import Challenge.with_back.domain.email.Email;
import Challenge.with_back.domain.email.ResetPasswordEmailFactory;
import Challenge.with_back.domain.email.VerificationCodeEmailFactory;
import Challenge.with_back.dto.response.CustomExceptionCode;
import Challenge.with_back.dto.token.AccessTokenDto;
import Challenge.with_back.dto.user.*;
import Challenge.with_back.entity.rdbms.User;
import Challenge.with_back.entity.redis.VerificationCode;
import Challenge.with_back.enums.account.AccountRole;
import Challenge.with_back.enums.account.LoginMethod;
import Challenge.with_back.exception.CustomException;
import Challenge.with_back.repository.rdbms.UserRepository;
import Challenge.with_back.repository.redis.VerificationCodeRepository;
import Challenge.with_back.security.JwtUtil;
import Challenge.with_back.utils.UserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class UserService
{
    private final UserRepository userRepository;
    private final VerificationCodeRepository verificationCodeRepository;

    private final UserUtil userUtil;
    private final JwtUtil jwtUtil;

    private final VerificationCodeEmailFactory verificationCodeEmailFactory;
    private final ResetPasswordEmailFactory resetPasswordEmailFactory;

    private final JavaMailSender javaMailSender;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Value("${PROFILE_IMAGE_BUCKET_URL}")
    String profileImageBucketUrl;

    // 회원가입
    @Transactional
    public void join(JoinDto dto)
    {
        // 인증번호 일치 여부 확인
        userUtil.checkVerificationCodeCorrectness(dto.getEmail(), dto.getCode());

        // 계정 중복 확인
        userUtil.shouldNotExistingUser(dto.getEmail());

        // 데이터 형식 체크
        userUtil.checkPasswordFormat(dto.getPassword());
        userUtil.checkNicknameFormat(dto.getNickname());

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

        // 인증번호 정보 삭제
        userUtil.deleteVerificationCode(dto.getEmail());
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
        // 무작위 인증번호를 생성하는 랜덤 객체
        SecureRandom secureRandom = new SecureRandom();

        // 무작위 인증번호 생성
        String code = secureRandom.ints(6, 0, 10)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining());

        // 해당 이메일을 통해 이미 인증번호를 발급했다면 삭제
        userUtil.deleteVerificationCode(dto.getEmail());

        // 새로운 인증번호 정보 등록
        VerificationCode verificationCode = VerificationCode.builder()
                .email(dto.getEmail())
                .code(code)
                .countWrong(1)
                .build();

        // 생성한 인증번호 저장
        verificationCodeRepository.save(verificationCode);

        // 이메일 생성
        Email email = verificationCodeEmailFactory.createEmail(code);

        // 이메일 전송
        verificationCodeEmailFactory.sendEmail(javaMailSender, dto.getEmail(), email);
    }

    // 이메일 인증번호 확인: 회원가입
    public void checkVerificationCodeForJoin(CheckVerificationCodeDto dto)
    {
        // 인증번호 확인
        userUtil.checkVerificationCodeCorrectness(dto.getEmail(), dto.getCode());

        // 계정 중복 확인
        userUtil.shouldNotExistingUser(dto.getEmail());
    }

    // 이메일 인증번호 확인: 비밀번호 초기화
    public void checkVerificationCodeForResetPassword(CheckVerificationCodeDto dto)
    {
        // 인증번호 확인
        userUtil.checkVerificationCodeCorrectness(dto.getEmail(), dto.getCode());

        // 계정 존재 확인
        userUtil.shouldExistingUser(dto.getEmail());
    }

    // 비밀번호 초기화
    @Transactional
    public void resetPassword(CheckVerificationCodeDto dto)
    {
        // 인증번호 확인
        userUtil.checkVerificationCodeCorrectness(dto.getEmail(), dto.getCode());

        // 계정 존재 확인
        User user = userUtil.shouldExistingUser(dto.getEmail());

        // 무작위 비밀번호를 생성하는 랜덤 객체
        SecureRandom secureRandom = new SecureRandom();

        // 무작위 비밀번호 생성
        String password = IntStream.range(0, 10)
                .map(i -> secureRandom.nextInt(26) + 'a')
                .mapToObj(c -> String.valueOf((char) c))
                .collect(Collectors.joining());

        // 변경사항 저장
        user.resetPassword(bCryptPasswordEncoder.encode(password));
        userRepository.save(user);

        // 이메일 생성
        Email email = resetPasswordEmailFactory.createEmail(password);

        // 이메일 전송
        resetPasswordEmailFactory.sendEmail(javaMailSender, dto.getEmail(), email);
    }
}
