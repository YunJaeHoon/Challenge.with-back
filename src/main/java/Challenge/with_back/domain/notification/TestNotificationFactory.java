package Challenge.with_back.domain.notification;

import Challenge.with_back.common.enums.NotificationType;
import Challenge.with_back.common.entity.rdbms.Notification;
import Challenge.with_back.common.entity.rdbms.User;
import Challenge.with_back.common.repository.rdbms.NotificationRepository;
import Challenge.with_back.common.repository.rdbms.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("TEST")
public class TestNotificationFactory extends NotificationFactory
{
    // 생성자
    @Autowired
    public TestNotificationFactory(UserRepository userRepository, NotificationRepository notificationRepository) {
        super(userRepository, notificationRepository);
    }

    // 알림 엔티티 생성
    @Override
    protected Notification createNotificationEntity(User user, Object data)
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

    // 알림 내용을 반환 형식으로 파싱
    @Override
    public String parseContent(String rawContent)
    {
        return rawContent;
    }
}
