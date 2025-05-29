package Challenge.with_back.domain.notification.controller;

import Challenge.with_back.common.response.success.CustomSuccessCode;
import Challenge.with_back.common.response.success.SuccessResponseDto;
import Challenge.with_back.domain.notification.dto.NotificationListDto;
import Challenge.with_back.common.entity.rdbms.User;
import Challenge.with_back.common.security.CustomUserDetails;
import Challenge.with_back.domain.notification.service.NotificationService;
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
public class NotificationController
{
    private final NotificationService notificationService;

    // 알림 조회
    @GetMapping("/notification")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<SuccessResponseDto> getNotifications(Pageable pageable, @AuthenticationPrincipal CustomUserDetails userDetails)
    {
        User user = userDetails.getUser();
        NotificationListDto data = notificationService.getNotifications(user, pageable);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .code(CustomSuccessCode.SUCCESS.name())
                        .message("알림 조회를 성공적으로 마쳤습니다.")
                        .data(data)
                        .build());
    }
    
    // 알림 삭제
    @DeleteMapping("/notification/{id}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<SuccessResponseDto> deleteNotification(@PathVariable("id") Long notificationId, @AuthenticationPrincipal CustomUserDetails userDetails)
    {
        User user = userDetails.getUser();
        notificationService.deleteNotification(notificationId, user);
        
        return ResponseEntity.status(HttpStatus.OK)
                       .body(SuccessResponseDto.builder()
                                     .code(CustomSuccessCode.SUCCESS.name())
                                     .message("알림을 성공적으로 삭제하였습니다.")
                                     .data(null)
                                     .build());
    }

    // 테스트 알림 전송
    @PostMapping("/notification/send/test")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<SuccessResponseDto> sendTestNotification(@AuthenticationPrincipal CustomUserDetails userDetails)
    {
        User user = userDetails.getUser();
        notificationService.sendTestNotification(user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponseDto.builder()
                        .code(CustomSuccessCode.SUCCESS.name())
                        .message("테스트 알림을 성공적으로 전송하였습니다.")
                        .data(null)
                        .build());
    }
}
