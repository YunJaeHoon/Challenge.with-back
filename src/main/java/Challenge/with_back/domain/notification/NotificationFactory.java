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
	public void createNotification(User user)
	{
		// 알림 엔티티 생성
		Notification notification = createNotificationEntity(user);
		notificationRepository.save(notification);
		
		// 사용자 읽지 않은 알림 개수 1 증가
		user.increaseCountUnreadNotification();
		userRepository.save(user);
	}
	
	// 알림 엔티티 생성
	abstract Notification createNotificationEntity(User user);
}
