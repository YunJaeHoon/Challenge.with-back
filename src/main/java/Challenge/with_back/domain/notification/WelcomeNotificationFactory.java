package Challenge.with_back.domain.notification;

import Challenge.with_back.common.enums.NotificationType;
import Challenge.with_back.entity.rdbms.Notification;
import Challenge.with_back.entity.rdbms.User;
import Challenge.with_back.repository.rdbms.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class WelcomeNotificationFactory implements NotificationFactory
{
    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public NotificationMessage createNotification(User user)
    {
        Notification notification = Notification.builder()
                .user(user)
                .type(NotificationType.WELCOME)
                .title("회원가입을 환영합니다.")
                .content("챌린지를 등록하거나 참여해보세요!")
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
