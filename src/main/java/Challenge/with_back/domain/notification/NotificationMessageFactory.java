package Challenge.with_back.domain.notification;

import Challenge.with_back.common.enums.NotificationType;
import Challenge.with_back.common.response.exception.CustomException;
import Challenge.with_back.common.response.exception.CustomExceptionCode;
import Challenge.with_back.entity.rdbms.Notification;
import Challenge.with_back.entity.rdbms.User;
import Challenge.with_back.repository.memory.SseEmitterRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public interface NotificationMessageFactory
{
    // 알림 엔티티 생성
    @Transactional
    Notification createNotification(User user);

    // 알림 메시지 생성
    default NotificationMessage createNotificationMessage(Notification notification) {
        return new NotificationMessage(notification);
    }

    // 알림 전송
    @Transactional
    default void send(User user, SseEmitterRepository sseEmitterRepository, String sseName)
    {
        // 알림 엔티티 생성
        Notification notification = createNotification(user);

        // 알림 메시지 생성
        NotificationMessage notificationMessage = createNotificationMessage(notification);

        // 연결 정보 가져오기
        SseEmitter sseEmitter = sseEmitterRepository.findByUserId(user.getId())
                .orElseThrow(() -> new CustomException(CustomExceptionCode.EMITTER_NOT_FOUND, user.getId()));

        // 알림 전송
        try {
            sseEmitter.send(SseEmitter.event()
                    .id(notification.getId().toString())
                    .name(sseName)
                    .data(notificationMessage));
        } catch (Exception e) {
            throw new CustomException(CustomExceptionCode.EMITTER_CONNECTION_ERROR, null);
        }
    }
}
