package Challenge.with_back.domain.notification;

import Challenge.with_back.common.entity.rdbms.Notification;
import Challenge.with_back.common.entity.rdbms.User;
import Challenge.with_back.common.repository.rdbms.NotificationRepository;
import Challenge.with_back.common.repository.rdbms.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public abstract class NotificationFactory
{
	private final UserRepository userRepository;
	private final NotificationRepository notificationRepository;
	
	// 알림 엔티티 및 메시지 생성
	@Transactional
	public Notification createNotification(User user, Object data)
	{
		// 알림 엔티티 생성
		Notification notification = createNotificationEntity(user, data);
		notificationRepository.save(notification);
		
		// 사용자 읽지 않은 알림 개수 1 증가
		user.increaseCountUnreadNotification();
		userRepository.save(user);

		return notification;
	}
	
	// 알림 엔티티 생성
	@Transactional
	protected abstract Notification createNotificationEntity(User user, Object data);

	// 알림 내용을 반환 형식으로 파싱
	public abstract Object parseContent(String rawContent);
}
