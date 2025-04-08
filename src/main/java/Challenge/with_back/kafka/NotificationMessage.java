package Challenge.with_back.kafka;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationMessage
{
    private Long id;
    private String title;
    private String content;
    private boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime viewDate;

    private boolean isFriendRequest;
    private String sender;
    private String receiver;
}
