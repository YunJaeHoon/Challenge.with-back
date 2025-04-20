package Challenge.with_back.domain.notification;

import Challenge.with_back.common.enums.NotificationType;
import Challenge.with_back.entity.rdbms.Notification;
import Challenge.with_back.entity.rdbms.User;
import Challenge.with_back.repository.rdbms.NotificationRepository;
import Challenge.with_back.repository.rdbms.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
public class TestNotificationFactory extends NotificationFactory
{
    private final NotificationRepository notificationRepository;

    public TestNotificationFactory(UserRepository userRepository, NotificationRepository notificationRepository) {
        super(userRepository);
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional
    public Notification createNotificationEntity(User user)
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

        return notification;
    }
}
