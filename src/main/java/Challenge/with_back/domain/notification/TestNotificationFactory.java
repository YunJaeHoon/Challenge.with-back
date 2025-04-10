package Challenge.with_back.domain.notification;

import Challenge.with_back.common.enums.NotificationType;
import Challenge.with_back.entity.rdbms.Notification;
import Challenge.with_back.entity.rdbms.User;
import Challenge.with_back.repository.rdbms.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TestNotificationFactory implements NotificationFactory
{
    private final NotificationRepository notificationRepository;

    @Override
    public NotificationMessage createNotification(User user)
    {
        Notification notification = Notification.builder()
                .user(user)
                .type(NotificationType.TEST)
                .title("테스트 알림 제목입니다.")
                .content("테스트 알림 내용입니다.")
                .isRead(false)
                .viewedAt(null)
                .build();

        notificationRepository.save(notification);

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
