package Challenge.with_back.domain.notification.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationDto
{
    private final Long notificationId;
    private final Long userId;

    private final String type;
    private final String title;
    private final String content;
    private final Boolean isRead;
    private final LocalDateTime createdAt;
    private final LocalDateTime viewedAt;
}
