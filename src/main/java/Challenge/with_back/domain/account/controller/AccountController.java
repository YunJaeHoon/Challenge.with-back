package Challenge.with_back.domain.account.controller;

import Challenge.with_back.common.response.success.CustomSuccessCode;
import Challenge.with_back.common.response.success.SuccessResponseDto;
import Challenge.with_back.domain.account.dto.*;
import Challenge.with_back.domain.account.service.AccountService;
import Challenge.with_back.common.entity.rdbms.User;
import Challenge.with_back.common.security.CustomUserDetails;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AccountController
{
	private final AccountService accountService;
	
	// 회원가입
	@PostMapping("/join")
	@PreAuthorize("permitAll()")
	public ResponseEntity<SuccessResponseDto> join(@RequestBody JoinDto dto)
	{
		accountService.join(dto);
		
		return ResponseEntity.status(HttpStatus.CREATED)
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
		BasicUserInfoDto dto = accountService.getBasicInfo(user);
		
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
	public ResponseEntity<SuccessResponseDto> reissueAccessToken(
			@CookieValue(name = "refreshToken", required = false) String refreshToken,
			HttpServletResponse response)
	{
		Cookie cookie = accountService.reissueAccessToken(refreshToken);
		response.addCookie(cookie);
		
		return ResponseEntity.status(HttpStatus.OK)
					   .body(SuccessResponseDto.builder()
									 .code(CustomSuccessCode.SUCCESS.name())
									 .message("Access token을 성공적으로 재발급하였습니다.")
									 .data(null)
									 .build());
	}
	
	// 이메일 인증번호 전송
	@PostMapping("/send-verification-code")
	@PreAuthorize("permitAll()")
	public ResponseEntity<SuccessResponseDto> sendVerificationCode(@RequestBody SendVerificationCodeDto dto)
	{
		accountService.sendVerificationCode(dto);
		
		return ResponseEntity.status(HttpStatus.OK)
					   .body(SuccessResponseDto.builder()
									 .code(CustomSuccessCode.SUCCESS.name())
									 .message("인증번호 이메일을 성공적으로 전송하였습니다.")
									 .data(null)
									 .build());
	}
	
	// 이메일 인증번호 확인: 회원가입
	@PostMapping("/check-verification-code/join")
	@PreAuthorize("permitAll()")
	public ResponseEntity<SuccessResponseDto> checkVerificationCodeForJoin(@RequestBody CheckVerificationCodeDto dto)
	{
		accountService.checkVerificationCodeForJoin(dto);
		
		return ResponseEntity.status(HttpStatus.OK)
					   .body(SuccessResponseDto.builder()
									 .code(CustomSuccessCode.SUCCESS.name())
									 .message("회원가입을 위한 인증번호를 성공적으로 확인하였습니다.")
									 .data(null)
									 .build());
	}
	
	// 이메일 인증번호 확인: 비밀번호 초기화
	@PostMapping("/check-verification-code/reset-password")
	@PreAuthorize("permitAll()")
	public ResponseEntity<SuccessResponseDto> checkVerificationCodeForResetPassword(@RequestBody CheckVerificationCodeDto dto)
	{
		accountService.checkVerificationCodeForResetPassword(dto);
		
		return ResponseEntity.status(HttpStatus.OK)
					   .body(SuccessResponseDto.builder()
									 .code(CustomSuccessCode.SUCCESS.name())
									 .message("비밀번호 초기화를 위한 인증번호를 성공적으로 확인하였습니다.")
									 .data(null)
									 .build());
	}
	
	// 비밀번호 초기화
	@PostMapping("/user/reset-password")
	@PreAuthorize("permitAll()")
	public ResponseEntity<SuccessResponseDto> resetPassword(@RequestBody CheckVerificationCodeDto dto)
	{
		accountService.resetPassword(dto);
		
		return ResponseEntity.status(HttpStatus.OK)
					   .body(SuccessResponseDto.builder()
									 .code(CustomSuccessCode.SUCCESS.name())
									 .message("비밀번호를 성공적으로 초기화하였습니다.")
									 .data(null)
									 .build());
	}
}
