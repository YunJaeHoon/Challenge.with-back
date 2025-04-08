package Challenge.with_back.domain.notification;

import Challenge.with_back.common.enums.NotificationType;
import Challenge.with_back.entity.rdbms.Notification;
import lombok.*;

import java.time.LocalDateTime;

@Getter
public class NotificationMessage
{
    private final Long id;
    private final String type;
    private final String title;
    private final String content;
    private final boolean isRead;
    private final LocalDateTime createdAt;
    private final LocalDateTime viewedAt;

    NotificationMessage(Notification notification)
    {
        this.id = notification.getId();
        this.type = notification.getType().name();
        this.title = notification.getTitle();
        this.content = notification.getContent();
        this.isRead = false;
        this.createdAt = notification.getCreatedAt();
        this.viewedAt = notification.getViewedAt();
    }
}
