package Challenge.with_back.domain.notification.controller;

import Challenge.with_back.common.response.success.CustomSuccessCode;
import Challenge.with_back.common.response.success.SuccessResponseDto;
import Challenge.with_back.domain.notification.NotificationMessage;
import Challenge.with_back.security.CustomUserDetails;
import Challenge.with_back.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class NotificationController
{
    private final NotificationService notificationService;

    // 알림 SSE 연결 생성
    @PostMapping("/notification/connect")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<SuccessResponseDto> createNotificationConnection(@AuthenticationPrincipal CustomUserDetails userDetails)
    {
        Long userId = userDetails.getUser().getId();
        SseEmitter sseEmitter = notificationService.createNotificationConnection(userId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponseDto.builder()
                        .code(CustomSuccessCode.SUCCESS.name())
                        .message("알림 SSE 연결을 성공적으로 생성하였습니다.")
                        .data(sseEmitter)
                        .build());
    }

    // 알림 조회
    @GetMapping("/notification")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<SuccessResponseDto> getNotifications(Pageable pageable, @AuthenticationPrincipal CustomUserDetails userDetails)
    {
        Long userId = userDetails.getUser().getId();
        Page<NotificationMessage> data = notificationService.getNotifications(userId, pageable);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .code(CustomSuccessCode.SUCCESS.name())
                        .message("알림 조회를 성공적으로 마쳤습니다.")
                        .data(data)
                        .build());
    }
}
