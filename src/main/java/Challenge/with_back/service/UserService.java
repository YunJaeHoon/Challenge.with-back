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
    private final JavaMailSender javaMailSender;

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

    // 회원가입 인증번호 생성
    @Transactional
    public String createVerificationCode(String email)
    {
        // 인증번호 생성
        Random random = new Random();
        StringBuilder authenticationNumber = new StringBuilder();
        for(int i = 0; i < 6; i++) {
            int word = random.nextInt(10);
            authenticationNumber.append(word);
        }

        Optional<VerificationCode> existingVerificationCode = verificationCodeRepository.findByEmail(email);
        existingVerificationCode.ifPresent(verificationCodeRepository::delete);

        VerificationCode verificationCode = VerificationCode.builder()
                .email(email)
                .code(authenticationNumber.toString())
                .countWrong(5)
                .build();

        verificationCodeRepository.save(verificationCode);

        return authenticationNumber.toString();
    }

    // 회원가입 인증번호 전송 이메일 내용 생성
    public String getEmailContentForJoinVerificationCode(String verificationCode)
    {
        return String.format(
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
                            Challenge.with 회원가입을 위한 인증번호입니다. <br />
                            위 인증번호를 입력하여 본인 확인을 해주시기 바랍니다.
                        </div>
                    </div>
                """,
                verificationCode
        );
    }

    // 이메일 전송
    public void sendEmail(String email, String subject, String content)
    {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();

            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            mimeMessageHelper.setTo(email);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(content, true);

            javaMailSender.send(mimeMessage);
        } catch(Exception e) {
            throw new CustomException(CustomExceptionCode.SEND_EMAIL_ERROR, e.getMessage());
        }
    }
}
