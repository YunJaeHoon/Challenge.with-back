package Challenge.with_back.domain.challenge.controller;

import Challenge.with_back.common.response.success.CustomSuccessCode;
import Challenge.with_back.common.response.success.SuccessResponseDto;
import Challenge.with_back.domain.challenge.dto.CreateChallengeDto;
import Challenge.with_back.domain.challenge.service.ChallengeService;
import Challenge.with_back.entity.rdbms.User;
import Challenge.with_back.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChallengeController
{
	private final ChallengeService challengeService;
	
	// 챌린지 생성
	@PostMapping("/challenge")
	@PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
	public ResponseEntity<SuccessResponseDto> createChallenge(@RequestBody CreateChallengeDto dto,
															  @AuthenticationPrincipal CustomUserDetails userDetails)
	{
		User user = userDetails.getUser();
		challengeService.createChallenge(dto, user);
		
		return ResponseEntity.status(HttpStatus.CREATED)
					   .body(SuccessResponseDto.builder()
									 .code(CustomSuccessCode.SUCCESS.name())
									 .message("챌린지 생성을 성공적으로 완료하였습니다.")
									 .data(null)
									 .build());
	}
}
