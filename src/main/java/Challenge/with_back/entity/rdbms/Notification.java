package Challenge.with_back.entity.rdbms;

import Challenge.with_back.enums.NotificationType;
import Challenge.with_back.enums.NotificationTypeConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Notification extends BasicEntity
{
    @Id
    @Column(nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 계정
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user")
    private User user;

    // 종류
    @NotNull
    @Convert(converter = NotificationTypeConverter.class)
    private NotificationType type;

    // 제목
    @NotNull
    @Column(length = 255)
    private String title;

    // 내용
    @NotNull
    @Column(columnDefinition = "TEXT")
    private String content;

    // 열람했는가?
    @NotNull
    private boolean isRead;

    // 열람 날짜
    private LocalDateTime viewedAt;

    @Builder
    public Notification(User user, NotificationType type, String title, String content, boolean isRead, LocalDateTime viewedAt) {
        this.user = user;
        this.type = type;
        this.title = title;
        this.content = content;
        this.isRead = isRead;
        this.viewedAt = viewedAt;
    }

    // 알림을 읽음으로 표시
    public void markAsRead() {
        this.isRead = true;
        this.viewedAt = LocalDateTime.now();
    }
}
