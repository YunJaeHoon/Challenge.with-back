package Challenge.with_back.controller;

import Challenge.with_back.dto.response.CustomSuccessCode;
import Challenge.with_back.dto.response.SuccessResponseDto;
import Challenge.with_back.dto.token.AccessTokenDto;
import Challenge.with_back.dto.user.BasicUserInfoDto;
import Challenge.with_back.dto.user.CheckVerificationCodeDto;
import Challenge.with_back.dto.user.JoinDto;
import Challenge.with_back.dto.user.SendVerificationCodeDto;
import Challenge.with_back.entity.User;
import Challenge.with_back.enums.account.AccountRole;
import Challenge.with_back.factory.email.ResetPasswordEmailFactory;
import Challenge.with_back.factory.email.VerificationCodeEmailFactory;
import Challenge.with_back.security.CustomUserDetails;
import Challenge.with_back.security.JwtUtil;
import Challenge.with_back.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController
{
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final VerificationCodeEmailFactory verificationCodeEmailFactory;
    private final ResetPasswordEmailFactory resetPasswordEmailFactory;
    private final JavaMailSender javaMailSender;

    // 회원가입
    @PostMapping("/join")
    @PreAuthorize("permitAll()")
    public ResponseEntity<SuccessResponseDto> join(@RequestBody JoinDto dto)
    {
        // 인증번호 일치 여부 확인
        userService.checkVerificationCodeCorrectness(dto.getEmail(), dto.getCode());

        // 계정 중복 확인
        userService.checkNormalUserDuplication(dto.getEmail());

        // 데이터 형식 체크
        userService.checkPasswordFormat(dto.getPassword());
        userService.checkNicknameFormat(dto.getNickname());

        // 계정 생성
        userService.createUser(dto.getEmail(), dto.getPassword(), dto.getNickname(), dto.isAllowEmailMarketing(), AccountRole.USER);

        // 인증번호 정보 삭제
        userService.deleteVerificationCode(dto.getEmail());

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .code(CustomSuccessCode.SUCCESS.name())
                        .message("회원가입을 성공적으로 완료하였습니다.")
                        .data(null)
                        .build());
    }

    // 사용자 기본 정보 조회
    @GetMapping("/user/basic-info")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<SuccessResponseDto> getBasicInfo(@AuthenticationPrincipal CustomUserDetails userDetails)
    {
        User user = userDetails.getUser();
        BasicUserInfoDto dto = userService.getBasicInfo(user);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .code(CustomSuccessCode.SUCCESS.name())
                        .message("사용자 기본 정보를 성공적으로 조회하였습니다.")
                        .data(dto)
                        .build());
    }

    // Access token 재발급
    @PostMapping("/reissue-access-token")
    @PreAuthorize("permitAll()")
    public ResponseEntity<SuccessResponseDto> reissueAccessToken(@CookieValue(name = "refreshToken", required = false) String refreshToken)
    {
        AccessTokenDto dto = userService.reissueAccessToken(refreshToken);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .code(CustomSuccessCode.SUCCESS.name())
                        .message("Access token을 성공적으로 재발급하였습니다.")
                        .data(dto)
                        .build());
    }

    // 이메일 인증번호 전송
    @PostMapping("/send-verification-code")
    @PreAuthorize("permitAll()")
    public ResponseEntity<SuccessResponseDto> sendVerificationCode(@RequestBody SendVerificationCodeDto dto)
    {
        verificationCodeEmailFactory.sendEmail(javaMailSender, dto.getEmail());

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .code(CustomSuccessCode.SUCCESS.name())
                        .message("인증번호 이메일을 성공적으로 전송하였습니다.")
                        .data(null)
                        .build());
    }

    // 이메일 인증번호 확인
    @PostMapping("/check-verification-code")
    @PreAuthorize("permitAll()")
    public ResponseEntity<SuccessResponseDto> checkVerificationCode(@RequestBody CheckVerificationCodeDto dto)
    {
        userService.checkVerificationCodeCorrectness(dto.getEmail(), dto.getCode());
        userService.checkVerificationCodeExpiration(dto.getEmail(), dto.getCode());

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .code(CustomSuccessCode.SUCCESS.name())
                        .message("인증번호를 성공적으로 확인하였습니다.")
                        .data(null)
                        .build());
    }

    // 일반 로그인 사용자 계정 중복 확인
    @GetMapping("/user/check-normal-user-duplication")
    @PreAuthorize("permitAll()")
    public ResponseEntity<SuccessResponseDto> checkNormalUserDuplication(@RequestParam String email)
    {
        userService.checkNormalUserDuplication(email);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .code(CustomSuccessCode.SUCCESS.name())
                        .message("일반 로그인 사용자 계정이 중복되지 않습니다.")
                        .data(null)
                        .build());
    }

    // 비밀번호 초기화
    @PostMapping("/user/reset-password")
    @PreAuthorize("permitAll()")
    public ResponseEntity<SuccessResponseDto> resetPassword(@RequestBody CheckVerificationCodeDto dto)
    {
        userService.checkVerificationCodeCorrectness(dto.getEmail(), dto.getCode());
        resetPasswordEmailFactory.sendEmail(javaMailSender, dto.getEmail());
        userService.deleteVerificationCode(dto.getEmail());

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .code(CustomSuccessCode.SUCCESS.name())
                        .message("비밀번호를 성공적으로 초기화하였습니다.")
                        .data(null)
                        .build());
    }
}
