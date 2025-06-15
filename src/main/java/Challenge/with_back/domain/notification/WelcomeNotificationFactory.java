package Challenge.with_back.domain.notification;

import Challenge.with_back.common.enums.NotificationType;
import Challenge.with_back.common.entity.rdbms.Notification;
import Challenge.with_back.common.entity.rdbms.User;
import Challenge.with_back.common.repository.rdbms.NotificationRepository;
import Challenge.with_back.common.repository.rdbms.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class WelcomeNotificationFactory extends NotificationFactory
{
    // 생성자
    @Autowired
    public WelcomeNotificationFactory(UserRepository userRepository, NotificationRepository notificationRepository) {
        super(userRepository, notificationRepository);
    }

    // 알림 엔티티 생성
    @Override
    protected Notification createNotificationEntity(User user, Object data)
    {
		return Notification.builder()
				.user(user)
				.type(NotificationType.WELCOME)
				.title("회원가입을 환영합니다.")
				.content("챌린지를 등록하거나 참여해보세요!")
				.isRead(false)
				.viewedAt(null)
				.build();
    }
}
