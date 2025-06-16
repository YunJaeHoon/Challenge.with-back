package Challenge.with_back.domain.notification.service;

import Challenge.with_back.common.exception.CustomException;
import Challenge.with_back.common.exception.CustomExceptionCode;
import Challenge.with_back.domain.notification.dto.NotificationDto;
import Challenge.with_back.domain.notification.TestNotificationFactory;
import Challenge.with_back.domain.notification.dto.NotificationListDto;
import Challenge.with_back.common.entity.rdbms.Notification;
import Challenge.with_back.common.entity.rdbms.User;
import Challenge.with_back.common.repository.rdbms.NotificationRepository;
import Challenge.with_back.common.repository.rdbms.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class NotificationService
{
	private final UserRepository userRepository;
	private final NotificationRepository notificationRepository;
	
	private final TestNotificationFactory testNotificationFactory;

	/// 서비스
	
	// 알림 조회
	@Transactional
	public NotificationListDto getNotifications(User user, Pageable pageable)
	{
		// 사용자 ID로 알림 조회
		Page<Notification> notificationPage = notificationRepository.findPageByUserId(user.getId(), pageable);
		
		// 알림이 존재하지 않는 경우, 예외 처리
		if(notificationPage.isEmpty()) {
			throw new CustomException(CustomExceptionCode.NOTIFICATION_NOT_FOUND, Map.of(
					"pageSize", pageable.getPageSize(),
					"currentPage", pageable.getPageNumber(),
					"totalPage", notificationPage.getTotalPages()
			));
		}
		
		// 알림 페이지를 알림 리스트로 변경
		List<NotificationDto> notificationList = notificationPage.stream()
			.map(notification -> {

				// 알림 읽음 여부
				boolean isRead = notification.isRead();

				// 알림을 읽지 않았다면 읽음으로 변경
				if(!isRead)
				{
					// 알림을 읽음으로 표시
					notification.markAsRead();

					// 사용자의 읽지 않은 알림 개수 감소
					user.decreaseCountUnreadNotification();
				}
				
				return NotificationDto.from(notification, isRead);

			}).toList();
		
		userRepository.save(user);
		
		return NotificationListDto.builder()
					   .content(notificationList)
					   .isLast(notificationPage.isLast())
					   .build();
	}
	
	// 알림 삭제
	@Transactional
	public void deleteNotification(Long notificationId, User user)
	{
		// 알림 조회
		Notification notification = notificationRepository.findById(notificationId)
				.orElseThrow(() -> new CustomException(CustomExceptionCode.NOTIFICATION_NOT_FOUND, notificationId));
		
		// 해당 사용자의 알림인지 확인
		if(!Objects.equals(notification.getUser().getId(), user.getId()))
			throw new CustomException(CustomExceptionCode.NOTIFICATION_OWNERSHIP_INVALID, user.getId());
		
		// 알림 삭제
		deleteNotificationEntity(notification);
	}
	
	// 테스트 알림 생성
	@Transactional
	public void sendTestNotification(User user)
	{
		testNotificationFactory.createNotification(user, null);
	}

	/// 공통 로직

	// 알림 엔티티 삭제
	@Transactional
	public void deleteNotificationEntity(Notification notification)
	{
		// 읽지 않은 알림이라면, 사용자의 읽지 않은 알림 개수 감소
		if(!notification.isRead()) {
			notification.getUser().decreaseCountUnreadNotification();
		}

		notificationRepository.delete(notification);
	}
}
