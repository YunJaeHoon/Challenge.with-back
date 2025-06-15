package Challenge.with_back.common.entity.rdbms;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(
        indexes = {
                @Index(name = "idx_receiver", columnList = "receiver")
        },
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"sender", "receiver"})
        }
)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class FriendRequest extends BasicEntity
{
    @Id
    @Column(nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 친구 요청을 보낸 계정
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender")
    private User sender;

    // 친구 요청을 받은 계정
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver")
    private User receiver;

    // 친구 요청 알림
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification")
    private Notification notification;
}
