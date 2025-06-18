package Challenge.with_back.domain.friend.controller;

import Challenge.with_back.common.entity.rdbms.User;
import Challenge.with_back.common.response.SuccessResponseDto;
import Challenge.with_back.common.security.CustomUserDetails;
import Challenge.with_back.domain.friend.dto.*;
import Challenge.with_back.domain.friend.service.FriendService;
import jakarta.validation.Valid;
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
                                                                @Valid @RequestBody SendFriendRequestDto dto)
    {
        User sender = userDetails.getUser();
        friendService.sendFriendRequest(sender, dto.getReceiverId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponseDto.builder()
                        .message("친구 요청을 성공하였습니다.")
                        .data(null)
                        .build());
    }

    // 친구 요청 수락
    @PostMapping("/friend-request/accept")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<SuccessResponseDto> acceptFriendRequest(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                  @Valid @RequestBody AnswerFriendRequestDto dto)
    {
        User receiver = userDetails.getUser();
        friendService.answerFriendRequest(receiver, dto.getFriendRequestId(), true);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("친구 요청을 성공적으로 수락하였습니다.")
                        .data(null)
                        .build());
    }

    // 친구 요청 거절
    @PostMapping("/friend-request/reject")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<SuccessResponseDto> rejectFriendRequest(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                  @Valid @RequestBody AnswerFriendRequestDto dto)
    {
        User receiver = userDetails.getUser();
        friendService.answerFriendRequest(receiver, dto.getFriendRequestId(), false);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("친구 요청을 성공적으로 거절하였습니다.")
                        .data(null)
                        .build());
    }

    // 친구 차단
    @PostMapping("/friend-block")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<SuccessResponseDto> createFriendBlock(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                @Valid @RequestBody CreateFriendBlockDto dto)
    {
        User blockingUser = userDetails.getUser();
        friendService.createFriendBlock(blockingUser, dto.getBlockedUserId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponseDto.builder()
                        .message("친구 차단을 성공하였습니다.")
                        .data(null)
                        .build());
    }

    // 친구 차단 해제
    @DeleteMapping("/friend-block/{friendBlockId}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<SuccessResponseDto> deleteFriendBlock(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                @PathVariable Long friendBlockId)
    {
        User blockingUser = userDetails.getUser();
        friendService.deleteFriendBlock(blockingUser, friendBlockId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("친구 차단을 성공적으로 해제하였습니다.")
                        .data(null)
                        .build());
    }

    // 친구 리스트 조회
    @GetMapping("/friend")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<SuccessResponseDto> getFriendList(Pageable pageable, @AuthenticationPrincipal CustomUserDetails userDetails)
    {
        User user = userDetails.getUser();
        FriendListDto data = friendService.getFriendList(user, pageable);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("친구 리스트를 성공적으로 조회하였습니다.")
                        .data(data)
                        .build());
    }

    // 친구 차단 리스트 조회
    @GetMapping("/friend-block")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<SuccessResponseDto> getFriendBlockList(Pageable pageable, @AuthenticationPrincipal CustomUserDetails userDetails)
    {
        User user = userDetails.getUser();
        FriendBlockListDto data = friendService.getFriendBlockList(user, pageable);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("친구 차단 리스트를 성공적으로 조회하였습니다.")
                        .data(data)
                        .build());
    }
}
