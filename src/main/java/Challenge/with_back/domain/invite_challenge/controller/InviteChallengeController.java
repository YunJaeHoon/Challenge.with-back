package Challenge.with_back.domain.invite_challenge.controller;

import Challenge.with_back.common.entity.rdbms.User;
import Challenge.with_back.common.response.CustomSuccessCode;
import Challenge.with_back.common.response.SuccessResponseDto;
import Challenge.with_back.common.security.CustomUserDetails;
import Challenge.with_back.domain.challenge.dto.JoinChallengeDto;
import Challenge.with_back.domain.invite_challenge.dto.InviteChallengeDto;
import Challenge.with_back.domain.invite_challenge.service.InviteChallengeService;
import jakarta.validation.Valid;
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
public class InviteChallengeController
{
    private final InviteChallengeService inviteChallengeService;

    // 챌린지 초대
    @PostMapping("/invite-challenge")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<SuccessResponseDto> inviteChallenge(@Valid @RequestBody InviteChallengeDto dto,
                                                              @AuthenticationPrincipal CustomUserDetails userDetails)
    {
        User user = userDetails.getUser();
        inviteChallengeService.inviteChallenge(user, dto.getInviteUserIdList(), dto.getChallengeId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponseDto.builder()
                        .code(CustomSuccessCode.SUCCESS.name())
                        .message("챌린지 초대를 성공적으로 완료하였습니다.")
                        .data(null)
                        .build());
    }
}
