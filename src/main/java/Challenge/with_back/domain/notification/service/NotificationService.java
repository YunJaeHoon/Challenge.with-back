package Challenge.with_back.domain.notification.service;

import Challenge.with_back.common.response.exception.CustomException;
import Challenge.with_back.common.response.exception.CustomExceptionCode;
import Challenge.with_back.domain.notification.NotificationMessage;
import Challenge.with_back.domain.notification.NotificationFactory;
import Challenge.with_back.domain.notification.TestNotificationFactory;
import Challenge.with_back.domain.notification.kafka.NotificationProducer;
import Challenge.with_back.entity.rdbms.Notification;
import Challenge.with_back.entity.rdbms.User;
import Challenge.with_back.repository.memory.SseEmitterRepository;
import Challenge.with_back.repository.rdbms.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
public class NotificationService
{
    private final NotificationRepository notificationRepository;
    private final SseEmitterRepository sseEmitterRepository;

    private final TestNotificationFactory testNotificationFactory;

    private final NotificationProducer notificationProducer;

    @Value("${SSE_EXPIRATION_TIME}")
    private static Long CONNECTION_EXPIRATION_TIME;

    // 알림 SSE 연결 생성
    @Transactional
    public SseEmitter createNotificationConnection(Long userId)
    {
        // SSE 연결 생성
        SseEmitter sseEmitter = new SseEmitter(CONNECTION_EXPIRATION_TIME);

        // SSE 연결 정보 저장
        sseEmitterRepository.save(userId, sseEmitter);

        // SSE 연결이 완료되거나 타임아웃 시, SSE 연결 정보 삭제
        sseEmitter.onCompletion(() -> sseEmitterRepository.deleteByUserId(userId));
        sseEmitter.onTimeout(() -> sseEmitterRepository.deleteByUserId(userId));

        // 알림을 위한 SSE 연결이 성공적으로 생성되었음을 클라이언트에게 전송
        try {
            sseEmitter.send(SseEmitter.event()
                    .id("id")
                    .name("NOTIFICATION")
                    .data("알림을 위한 SSE 연결이 성공적으로 생성되었습니다."));
        } catch (Exception e) {
            throw new CustomException(CustomExceptionCode.EMITTER_CONNECTION_ERROR, null);
        }

        return sseEmitter;
    }

    // 알림 조회
    public Page<NotificationMessage> getNotifications(Long userId, Pageable pageable)
    {
        // 사용자 ID로 알림 조회
        Page<Notification> notifications = notificationRepository.findAllByUserId(userId, pageable);

        // 알림이 존재하지 않는 경우, 예외 발생
        if (notifications.isEmpty())
            throw new CustomException(CustomExceptionCode.NOTIFICATION_NOT_FOUND, null);

        // 알림들을 NotificationMessage로 변환하여 반환
        return notifications.map((notification -> NotificationMessage.builder()
                .notificationId(notification.getId())
                .userId(notification.getUser().getId())
                .type(notification.getType().name())
                .title(notification.getTitle())
                .content(notification.getContent())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .viewedAt(notification.getViewedAt())
                .build()));
    }

    // 테스트 알림 전송
    @Transactional
    public void sendTestNotification(User user)
    {
        // 알림 메시지 생성
        NotificationMessage notificationMessage = testNotificationFactory.createNotification(user);

        // 알림 메시지 전송
        notificationProducer.send(notificationMessage);
    }
}
