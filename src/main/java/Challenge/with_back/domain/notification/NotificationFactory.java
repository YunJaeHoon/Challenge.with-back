package Challenge.with_back.domain.notification;

import Challenge.with_back.common.enums.NotificationType;
import Challenge.with_back.entity.rdbms.Notification;
import Challenge.with_back.entity.rdbms.User;
import Challenge.with_back.repository.rdbms.UserRepository;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public abstract class NotificationFactory
{
    private final UserRepository userRepository;

    // 알림 엔티티 및 메시지 생성
    @Transactional
    public NotificationMessage createNotification(User user)
    {
        // 알림 엔티티 생성
        Notification notification = createNotificationEntity(user);

        // 사용자 읽지 않은 알림 개수 1 증가
        user.increaseCountUnreadNotification();
        userRepository.save(user);

        // 알림 메시지 생성 및 반환
        return createNotificationMessage(user, notification);
    }

    // 알림 엔티티 생성
    abstract Notification createNotificationEntity(User user);

    // 알림 메시지 생성
    NotificationMessage createNotificationMessage(User user, Notification notification)
    {
        return NotificationMessage.builder()
                .notificationId(notification.getId())
                .userId(user.getId())
                .type(notification.getType().name())
                .title(notification.getTitle())
                .content(notification.getContent())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .viewedAt(notification.getViewedAt())
                .build();
    }
}
