package Challenge.with_back.domain.notification;

import Challenge.with_back.common.entity.rdbms.Notification;
import Challenge.with_back.common.entity.rdbms.User;
import Challenge.with_back.common.enums.NotificationType;
import Challenge.with_back.common.exception.CustomException;
import Challenge.with_back.common.exception.CustomExceptionCode;
import Challenge.with_back.common.repository.rdbms.NotificationRepository;
import Challenge.with_back.common.repository.rdbms.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FriendRequestNotificationFactory extends NotificationFactory
{
    // 생성자
    @Autowired
    public FriendRequestNotificationFactory(UserRepository userRepository, NotificationRepository notificationRepository) {
        super(userRepository, notificationRepository);
    }

    // 알림 엔티티 생성
    @Override
    protected Notification createNotificationEntity(User user, Object data)
    {
        Long friendRequestId;

        // 데이터를 Long 타입으로 변환 시도
        try {
            friendRequestId = (Long) data;
        } catch (ClassCastException e) {
            throw new CustomException(CustomExceptionCode.INVALID_NOTIFICATION_DATA, data);
        }

        return Notification.builder()
                .user(user)
                .type(NotificationType.FRIEND_REQUEST)
                .title("친구 요청이 들어왔습니다.")
                .content(friendRequestId.toString())
                .isRead(false)
                .viewedAt(null)
                .build();
    }
}
