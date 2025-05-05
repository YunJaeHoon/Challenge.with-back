package Challenge.with_back.domain.notification;

import Challenge.with_back.common.enums.NotificationType;
import Challenge.with_back.common.entity.rdbms.Notification;
import Challenge.with_back.common.entity.rdbms.User;
import Challenge.with_back.common.repository.rdbms.NotificationRepository;
import Challenge.with_back.common.repository.rdbms.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class TestNotificationFactory extends NotificationFactory
{
    // 생성자
    public TestNotificationFactory(UserRepository userRepository, NotificationRepository notificationRepository) {
        super(userRepository, notificationRepository);
    }

    @Override
    public Notification createNotificationEntity(User user)
    {
		return Notification.builder()
				.user(user)
				.type(NotificationType.TEST)
				.title("테스트 알림 제목입니다.")
				.content("테스트 알림 내용입니다.")
				.isRead(false)
				.viewedAt(null)
				.build();
    }
}
