package Challenge.with_back.domain.friend.controller;

import Challenge.with_back.common.entity.rdbms.User;
import Challenge.with_back.common.response.success.CustomSuccessCode;
import Challenge.with_back.common.response.success.SuccessResponseDto;
import Challenge.with_back.common.security.CustomUserDetails;
import Challenge.with_back.domain.friend.dto.*;
import Challenge.with_back.domain.friend.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    // 친구 요청 수락
    @PostMapping("/friend-request/accept")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<SuccessResponseDto> acceptFriendRequest(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                  @RequestBody AcceptFriendRequestDto dto)
    {
        User receiver = userDetails.getUser();
        friendService.answerFriendRequest(receiver, dto.getFriendRequestId(), true);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .code(CustomSuccessCode.SUCCESS.name())
                        .message("친구 요청을 성공적으로 수락하였습니다.")
                        .data(null)
                        .build());
    }

    // 친구 요청 거절
    @PostMapping("/friend-request/reject")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<SuccessResponseDto> rejectFriendRequest(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                  @RequestBody RejectFreindRequestDto dto)
    {
        User receiver = userDetails.getUser();
        friendService.answerFriendRequest(receiver, dto.getFriendRequestId(), false);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .code(CustomSuccessCode.SUCCESS.name())
                        .message("친구 요청을 성공적으로 거절하였습니다.")
                        .data(null)
                        .build());
    }

    // 친구 차단
    @PostMapping("/friend-block")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<SuccessResponseDto> createFriendBlock(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                @RequestBody CreateFriendBlockDto dto)
    {
        User blockingUser = userDetails.getUser();
        friendService.createFriendBlock(blockingUser, dto.getBlockedUserId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponseDto.builder()
                        .code(CustomSuccessCode.SUCCESS.name())
                        .message("친구 차단을 성공하였습니다.")
                        .data(null)
                        .build());
    }

    // 친구 조회
    @GetMapping("/friend")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<SuccessResponseDto> getFriends(Pageable pageable, @AuthenticationPrincipal CustomUserDetails userDetails)
    {
        User user = userDetails.getUser();
        FriendListDto data = friendService.getFriends(user, pageable);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .code(CustomSuccessCode.SUCCESS.name())
                        .message("친구를 성공적으로 조회하였습니다.")
                        .data(data)
                        .build());
    }
}
