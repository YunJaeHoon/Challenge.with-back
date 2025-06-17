package Challenge.with_back.common.entity.rdbms;

import Challenge.with_back.common.enums.NotificationType;
import Challenge.with_back.common.enums.NotificationTypeConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
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

    // 알림 내용 변경
    // 내용
    @NotNull
    @Column(columnDefinition = "TEXT")
    private String content;

    // 열람했는가?
    @NotNull
    private boolean isRead;

    // 열람 날짜
    private LocalDateTime viewedAt;

    @OneToMany(mappedBy = "notification", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FriendRequest> friendRequestList;

    @OneToMany(mappedBy = "notification", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InviteChallenge> inviteChallengeList;

    // 알림을 읽음으로 표시
    public void markAsRead() {
        this.isRead = true;
        this.viewedAt = LocalDateTime.now();
    }
}
