package Challenge.with_back.domain.friend.controller;

import Challenge.with_back.common.entity.rdbms.User;
import Challenge.with_back.common.response.success.CustomSuccessCode;
import Challenge.with_back.common.response.success.SuccessResponseDto;
import Challenge.with_back.common.security.CustomUserDetails;
import Challenge.with_back.domain.friend.dto.SendFriendRequestDto;
import Challenge.with_back.domain.friend.service.FriendService;
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
public class FriendController
{
    private final FriendService friendService;

    // 친구 요청
    @PostMapping("/friend-request")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<SuccessResponseDto> sendFriendRequest(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                @RequestBody SendFriendRequestDto dto)
    {
        User sender = userDetails.getUser();
        friendService.sendFriendRequest(sender, dto.getReceiverId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponseDto.builder()
                        .code(CustomSuccessCode.SUCCESS.name())
                        .message("친구 요청을 성공하였습니다.")
                        .data(null)
                        .build());
    }
}
