package Challenge.with_back.domain.notification;

import Challenge.with_back.common.entity.rdbms.FriendRequest;
import Challenge.with_back.common.entity.rdbms.Notification;
import Challenge.with_back.common.entity.rdbms.User;
import Challenge.with_back.common.enums.NotificationType;
import Challenge.with_back.common.exception.CustomException;
import Challenge.with_back.common.exception.CustomExceptionCode;
import Challenge.with_back.common.repository.rdbms.FriendRequestRepository;
import Challenge.with_back.common.repository.rdbms.NotificationRepository;
import Challenge.with_back.common.repository.rdbms.UserRepository;
import lombok.Builder;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("FRIEND_REQUEST")
public class FriendRequestNotificationFactory extends NotificationFactory
{
    private final FriendRequestRepository friendRequestRepository;

    @Value("${PROFILE_IMAGE_BUCKET_URL}")
    String profileImageBucketUrl;

    // 생성자
    @Autowired
    public FriendRequestNotificationFactory(UserRepository userRepository, NotificationRepository notificationRepository, FriendRequestRepository friendRequestRepository) {
        super(userRepository, notificationRepository);
        this.friendRequestRepository = friendRequestRepository;
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

    // 알림 내용을 반환 형식으로 파싱
    @Override
    public Object parseContent(String rawContent)
    {
        // 친구 요청 데이터 ID
        Long friendRequestId;

        try {
            friendRequestId = Long.parseLong(rawContent);
        } catch (ClassCastException e) {
            throw new CustomException(CustomExceptionCode.INVALID_NOTIFICATION_DATA, rawContent);
        }

        // 친구 요청 데이터 조회
        FriendRequest friendRequest = friendRequestRepository.findById(friendRequestId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.FRIEND_REQUEST_NOT_FOUND, friendRequestId));

        return Content.from(friendRequest, profileImageBucketUrl);
    }

    // 알림 내용 클래스
    @Getter
    @Builder
    private static class Content
    {
        /// 친구 요청 데이터 정보
        Long friendRequestId;   // 친구 요청 데이터 ID

        /// 친구 요청을 보낸 사용자 정보
        Long userId;                // 사용자 ID
        String nickname;            // 닉네임
        String profileImageUrl;     // 프로필 이미지 URL

        public static Content from(FriendRequest friendRequest, String profileImageBucketUrl)
        {
            return Content.builder()
                    .friendRequestId(friendRequest.getId())
                    .userId(friendRequest.getSender().getId())
                    .nickname(friendRequest.getSender().getNickname())
                    .profileImageUrl(profileImageBucketUrl + friendRequest.getSender().getProfileImageUrl())
                    .build();
        }
    }
}
