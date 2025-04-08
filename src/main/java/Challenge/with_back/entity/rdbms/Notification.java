package Challenge.with_back.entity.rdbms;

import Challenge.with_back.common.enums.AccountRole;
import Challenge.with_back.common.enums.AccountRoleConverter;
import Challenge.with_back.common.enums.NotificationType;
import Challenge.with_back.common.enums.NotificationTypeConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    @NotNull
    private LocalDateTime viewDate;

    @Builder
    public Notification(User user, NotificationType type, String title, String content, boolean isRead, LocalDateTime viewDate) {
        this.user = user;
        this.type = type;
        this.title = title;
        this.content = content;
        this.isRead = isRead;
        this.viewDate = viewDate;
    }
}
