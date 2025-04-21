package Challenge.with_back.domain.notification.service;

import Challenge.with_back.common.response.exception.CustomException;
import Challenge.with_back.common.response.exception.CustomExceptionCode;
import Challenge.with_back.domain.notification.dto.NotificationDto;
import Challenge.with_back.domain.notification.TestNotificationFactory;
import Challenge.with_back.domain.notification.dto.NotificationListDto;
import Challenge.with_back.entity.rdbms.Notification;
import Challenge.with_back.entity.rdbms.User;
import Challenge.with_back.repository.rdbms.NotificationRepository;
import Challenge.with_back.repository.rdbms.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService
{
	private final UserRepository userRepository;
	private final NotificationRepository notificationRepository;
	
	private final TestNotificationFactory testNotificationFactory;
	
	// 알림 조회
	public NotificationListDto getNotifications(Long userId, Pageable pageable)
	{
		// 사용자 조회
		User user = userRepository.findById(userId)
							.orElseThrow(() -> new CustomException(CustomExceptionCode.USER_NOT_FOUND, userId));
		
		// 사용자 ID로 알림 조회
		Page<Notification> notificationPage = notificationRepository.findAllByUserId(userId, pageable);
		
		// 알림이 존재하지 않는 경우, 예외 발생
		if (notificationPage.isEmpty())
			throw new CustomException(CustomExceptionCode.NOTIFICATION_NOT_FOUND, null);
		
		// 알림 페이지를 알림 리스트로 변경
		List<NotificationDto> notificationList = notificationPage.stream()
			.map(notification -> {
				boolean isRead = notification.isRead();
				
				if (!isRead) {
					notification.markAsRead();
					user.decreaseCountUnreadNotification();
					
					notificationRepository.save(notification);
				}
				
				return NotificationDto.builder()
						   .notificationId(notification.getId())
						   .userId(notification.getUser().getId())
						   .type(notification.getType().name())
						   .title(notification.getTitle())
						   .content(notification.getContent())
						   .isRead(isRead)
						   .createdAt(notification.getCreatedAt())
						   .viewedAt(notification.getViewedAt())
						   .build();
			}).toList();
		
		userRepository.save(user);
		
		return NotificationListDto.builder()
					   .content(notificationList)
					   .isLast(notificationPage.isLast())
					   .build();
	}
	
	// 테스트 알림 생성
	@Transactional
	public void sendTestNotification(User user)
	{
		testNotificationFactory.createNotification(user);
	}
}
