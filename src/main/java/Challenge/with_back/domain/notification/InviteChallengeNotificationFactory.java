package Challenge.with_back.domain.notification;

import Challenge.with_back.common.entity.rdbms.FriendRequest;
import Challenge.with_back.common.entity.rdbms.InviteChallenge;
import Challenge.with_back.common.entity.rdbms.Notification;
import Challenge.with_back.common.entity.rdbms.User;
import Challenge.with_back.common.enums.NotificationType;
import Challenge.with_back.common.exception.CustomException;
import Challenge.with_back.common.exception.CustomExceptionCode;
import Challenge.with_back.common.repository.rdbms.InviteChallengeRepository;
import Challenge.with_back.common.repository.rdbms.NotificationRepository;
import Challenge.with_back.common.repository.rdbms.UserRepository;
import lombok.Builder;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("INVITE_CHALLENGE")
public class InviteChallengeNotificationFactory extends NotificationFactory
{
    private final InviteChallengeRepository inviteChallengeRepository;

    @Value("${PROFILE_IMAGE_BUCKET_URL}")
    static String profileImageBucketUrl;

    // 생성자
    @Autowired
    public InviteChallengeNotificationFactory(UserRepository userRepository, NotificationRepository notificationRepository, InviteChallengeRepository inviteChallengeRepository) {
        super(userRepository, notificationRepository);
        this.inviteChallengeRepository = inviteChallengeRepository;
    }

    // 알림 엔티티 생성
    @Override
    protected Notification createNotificationEntity(User user, Object data)
    {
        Long inviteChallengeId;

        // 데이터를 Long 타입으로 변환 시도
        try {
            inviteChallengeId = (Long) data;
        } catch (ClassCastException e) {
            throw new CustomException(CustomExceptionCode.INVALID_NOTIFICATION_DATA, data);
        }

        return Notification.builder()
                .user(user)
                .type(NotificationType.INVITE_CHALLENGE)
                .title("챌린지에 초대되었습니다.")
                .content(inviteChallengeId.toString())
                .isRead(false)
                .viewedAt(null)
                .build();
    }

    // 알림 내용을 반환 형식으로 파싱
    @Override
    public Object parseContent(String rawContent)
    {
        // 챌린지 초대 데이터 ID
        Long inviteChallengeId;

        try {
            inviteChallengeId = Long.parseLong(rawContent);
        } catch (ClassCastException e) {
            throw new CustomException(CustomExceptionCode.INVALID_NOTIFICATION_DATA, rawContent);
        }

        // 챌린지 초대 데이터 조회
        InviteChallenge inviteChallenge = inviteChallengeRepository.findById(inviteChallengeId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.INVITE_CHALLENGE_NOT_FOUND, inviteChallengeId));

        return Content.from(inviteChallenge);
    }

    // 알림 내용 클래스
    @Getter
    @Builder
    private static class Content
    {
        /// 챌린지 초대 데이터 정보
        Long inviteChallengeId;     // 챌린지 초대 데이터 ID

        /// 챌린지 초대를 보낸 사용자 정보
        Long userId;                    // 사용자 ID
        String userNickname;            // 닉네임
        String userProfileImageUrl;     // 프로필 이미지 URL

        /// 챌린지 정보
        Long challengeId;   // 챌린지 ID
        private String challengeIcon;
        private String challengeColorTheme;
        private String challengeName;
        private String challengeDescription;
        private int challengeGoalCount;
        private String challengeUnit;

        public static Content from(InviteChallenge inviteChallenge)
        {
            return Content.builder()
                    .inviteChallengeId(inviteChallenge.getId())
                    .userId(inviteChallenge.getSender().getId())
                    .userNickname(inviteChallenge.getSender().getNickname())
                    .userProfileImageUrl(profileImageBucketUrl + inviteChallenge.getSender().getProfileImageUrl())
                    .challengeId(inviteChallenge.getChallenge().getId())
                    .challengeIcon(inviteChallenge.getChallenge().getIcon())
                    .challengeColorTheme(inviteChallenge.getChallenge().getColorTheme().name())
                    .challengeName(inviteChallenge.getChallenge().getName())
                    .challengeDescription(inviteChallenge.getChallenge().getDescription())
                    .challengeGoalCount(inviteChallenge.getChallenge().getGoalCount())
                    .challengeUnit(inviteChallenge.getChallenge().getUnit().name())
                    .build();
        }
    }
}
