package Challenge.with_back.domain.challenge.controller;

import Challenge.with_back.domain.challenge.dto.*;
import Challenge.with_back.common.response.SuccessResponseDto;
import Challenge.with_back.domain.challenge.service.ChallengeService;
import Challenge.with_back.common.entity.rdbms.User;
import Challenge.with_back.common.security.CustomUserDetails;
import jakarta.validation.Valid;
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
    public ResponseEntity<SuccessResponseDto> createChallenge(@Valid @RequestBody CreateChallengeDto dto,
                                                              @AuthenticationPrincipal CustomUserDetails userDetails)
    {
        User user = userDetails.getUser();
        challengeService.createChallenge(dto, user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponseDto.builder()
                        .message("챌린지 생성을 성공적으로 완료하였습니다.")
                        .data(null)
                        .build());
    }

    // 챌린지 가입
    @PostMapping("/challenge/join")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<SuccessResponseDto> joinChallenge(@Valid @RequestBody JoinChallengeDto dto,
                                                            @AuthenticationPrincipal CustomUserDetails userDetails)
    {
        User user = userDetails.getUser();
        challengeService.joinChallenge(user, dto.getChallengeId(), false);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("챌린지 가입을 성공적으로 완료하였습니다.")
                        .data(null)
                        .build());
    }

    // 챌린지 초대
    @PostMapping("/invite-challenge")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<SuccessResponseDto> inviteChallenge(@Valid @RequestBody InviteChallengeDto dto,
                                                              @AuthenticationPrincipal CustomUserDetails userDetails)
    {
        User user = userDetails.getUser();
        challengeService.inviteChallenge(user, dto.getInviteUserIdList(), dto.getChallengeId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponseDto.builder()
                        .message("챌린지 초대를 성공적으로 완료하였습니다.")
                        .data(null)
                        .build());
    }

    // 챌린지 초대 수락
    @PostMapping("/invite-challenge/accept")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<SuccessResponseDto> acceptInviteChallenge(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                    @Valid @RequestBody AnswerInviteChallengeDto dto)
    {
        User receiver = userDetails.getUser();
        challengeService.answerInviteChallenge(receiver, dto.getInviteChallengeId(), true);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("챌린지 초대를 성공적으로 수락하였습니다.")
                        .data(null)
                        .build());
    }

    // 챌린지 초대 거절
    @PostMapping("/invite-challenge/reject")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<SuccessResponseDto> rejectInviteChallenge(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                    @Valid @RequestBody AnswerInviteChallengeDto dto)
    {
        User receiver = userDetails.getUser();
        challengeService.answerInviteChallenge(receiver, dto.getInviteChallengeId(), false);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("챌린지 초대를 성공적으로 거절하였습니다.")
                        .data(null)
                        .build());
    }

    // 현재 진행 중인 내 챌린지 조회
    @GetMapping("/challenge/me/ongoing")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<SuccessResponseDto> getMyChallenges(@AuthenticationPrincipal CustomUserDetails userDetails)
    {
        User user = userDetails.getUser();
        GetMyChallengeDto data = challengeService.getMyChallenges(user);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("현재 진행 중인 내 챌린지 조회를 성공적으로 완료하였습니다.")
                        .data(data)
                        .build());
    }

    // 챌린지 삭제
    @DeleteMapping("/challenge/{challengeId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<SuccessResponseDto> deleteChallenge(@PathVariable Long challengeId)
    {
        challengeService.deleteChallenge(challengeId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponseDto.builder()
                        .message("챌린지 삭제를 성공적으로 완료하였습니다.")
                        .data(null)
                        .build());
    }

    // 챌린지 상세 조회
    @GetMapping("/challenge/{challengeId}")
    @PreAuthorize("permitAll")
    public ResponseEntity<SuccessResponseDto> getChallenge(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                           @PathVariable Long challengeId)
    {
        User user = userDetails != null ? userDetails.getUser() : null;
        ChallengeDetailDto data = challengeService.getChallenge(user, challengeId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("챌린지 상세 조회를 성공적으로 완료하였습니다.")
                        .data(data)
                        .build());
    }
}
