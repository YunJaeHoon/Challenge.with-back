package Challenge.with_back.domain.notification;

import Challenge.with_back.common.enums.NotificationType;
import Challenge.with_back.entity.rdbms.Notification;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationMessage
{
    private final Long id;
    private final String type;
    private final String title;
    private final String content;
    private final boolean isRead;
    private final LocalDateTime createdAt;
    private final LocalDateTime viewedAt;
}
