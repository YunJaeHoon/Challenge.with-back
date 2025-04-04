package Challenge.with_back.service;

import Challenge.with_back.dto.response.CustomExceptionCode;
import Challenge.with_back.dto.token.AccessTokenDto;
import Challenge.with_back.dto.user.BasicUserInfoDto;
import Challenge.with_back.entity.rdbms.User;
import Challenge.with_back.entity.redis.VerificationCode;
import Challenge.with_back.enums.account.AccountRole;
import Challenge.with_back.enums.account.LoginMethod;
import Challenge.with_back.exception.CustomException;
import Challenge.with_back.repository.rdbms.UserRepository;
import Challenge.with_back.repository.redis.VerificationCodeRepository;
import Challenge.with_back.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    public void createUser(String email,
                           String password,
                           String nickname,
                           boolean allowEmailMarketing,
                           AccountRole role)
    {
        User user = User.builder()
                .loginMethod(LoginMethod.NORMAL)
                .email(email)
                .password(bCryptPasswordEncoder.encode(password))
                .nickname(nickname)
                .profileImageUrl(profileImageBucketUrl + "/profile-image_basic.svg")
                .selfIntroduction("")
                .allowEmailMarketing(allowEmailMarketing)
                .premiumExpirationDate(LocalDate.now().minusDays(1))
                .countUnreadNotification(0)
                .paymentInformationEmail(email)
                .accountRole(role)
                .build();

        userRepository.save(user);
    }

    // 비밀번호 형식 체크
    public void checkPasswordFormat(String password)
    {
        // 8 ~ 20자
        // 영문, 숫자, 특수문자를 모두 포함
        String regex = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?])[A-Za-z\\d!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]{8,20}$";

        if(!Pattern.matches(regex, password))
            throw new CustomException(CustomExceptionCode.INVALID_PASSWORD_FORMAT, password);
    }

    // 닉네임 형식 체크
    public void checkNicknameFormat(String nickname)
    {
        // 2 ~ 12자
        // 영문, 한글, 숫자만 허용
        String regex = "^[A-Za-z0-9가-힣]{2,12}$";

        if(!Pattern.matches(regex, nickname))
            throw new CustomException(CustomExceptionCode.INVALID_NICKNAME_FORMAT, nickname);
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

    // 인증번호 생성
    @Transactional
    public String createVerificationCode(String to)
    {
        // 무작위 인증번호를 생성하는 랜덤 객체
        SecureRandom secureRandom = new SecureRandom();

        // 무작위 인증번호 생성
        String authenticationNumber = secureRandom.ints(6, 0, 10)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining());

        // 해당 이메일을 통해 이미 인증번호를 발급했다면 삭제
        deleteVerificationCode(to);

        // 새로운 인증번호 정보 등록
        VerificationCode verificationCode = VerificationCode.builder()
                .email(to)
                .code(authenticationNumber)
                .countWrong(1)
                .build();

        verificationCodeRepository.save(verificationCode);

        return authenticationNumber;
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

    // 인증번호 삭제
    @Transactional
    public void deleteVerificationCode(String email)
    {
        Optional<VerificationCode> verificationCode = verificationCodeRepository.findByEmail(email);
        verificationCode.ifPresent(verificationCodeRepository::delete);
    }

    // 일반 로그인 사용자 계정 중복 확인
    public void shouldNotExistingUser(String email)
    {
        if(userRepository.findByEmailAndLoginMethod(email, LoginMethod.NORMAL).isPresent())
            throw new CustomException(CustomExceptionCode.ALREADY_EXISTING_USER, email);
    }

    // 일반 로그인 사용자 계정 존재 확인
    public User shouldExistingUser(String email)
    {
        return userRepository.findByEmailAndLoginMethod(email, LoginMethod.NORMAL)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.USER_NOT_FOUND, email));
    }

    // 비밀번호 초기화
    @Transactional
    public String resetPassword(String email)
    {
        // 사용자 존재 여부 확인
        User user = shouldExistingUser(email);

        // 무작위 비밀번호를 생성하는 랜덤 객체
        SecureRandom secureRandom = new SecureRandom();

        // 무작위 비밀번호 생성
        String randomPassword = IntStream.range(0, 10)
                .map(i -> secureRandom.nextInt(26) + 'a')
                .mapToObj(c -> String.valueOf((char) c))
                .collect(Collectors.joining());

        // 변경사항 저장
        user.resetPassword(bCryptPasswordEncoder.encode(randomPassword));
        userRepository.save(user);

        return randomPassword;
    }
}
