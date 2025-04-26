package Challenge.with_back.domain.challenge.controller;

import Challenge.with_back.aop.annotation.PremiumOnly;
import Challenge.with_back.response.success.CustomSuccessCode;
import Challenge.with_back.response.success.SuccessResponseDto;
import Challenge.with_back.domain.challenge.dto.CreateChallengeDto;
import Challenge.with_back.domain.challenge.dto.GetMyChallengeDto;
import Challenge.with_back.domain.challenge.service.ChallengeService;
import Challenge.with_back.entity.rdbms.User;
import Challenge.with_back.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    // 내 챌린지 조회
    @GetMapping("/challenge/me")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<SuccessResponseDto> getMyChallenges(@AuthenticationPrincipal CustomUserDetails userDetails)
    {
        User user = userDetails.getUser();
        GetMyChallengeDto data = challengeService.getMyChallenges(user);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .code(CustomSuccessCode.SUCCESS.name())
                        .message("내 챌린지 조회를 성공적으로 완료하였습니다.")
                        .data(data)
                        .build());
    }
}
