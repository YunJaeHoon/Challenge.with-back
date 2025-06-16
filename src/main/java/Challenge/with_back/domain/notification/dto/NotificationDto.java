package Challenge.with_back.domain.notification.dto;

import Challenge.with_back.common.entity.rdbms.Notification;
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
    private final Object content;
    private final Boolean isRead;
    private final LocalDateTime createdAt;
    private final LocalDateTime viewedAt;

    public static NotificationDto from(Notification notification, Object content, boolean isRead)
    {
        return NotificationDto.builder()
                .notificationId(notification.getId())
                .userId(notification.getUser().getId())
                .type(notification.getType().name())
                .title(notification.getTitle())
                .content(content)
                .isRead(isRead)
                .createdAt(notification.getCreatedAt())
                .viewedAt(notification.getViewedAt())
                .build();
    }
}
