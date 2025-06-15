package Challenge.with_back.common.entity.rdbms;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(
        indexes = {
                @Index(name = "idx_senderAndReceiverAndChallenge", columnList = "sender, receiver, challenge")
        },
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"sender", "receiver", "challenge"})
        }
)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class InviteChallenge extends BasicEntity
{
    @Id
    @Column(nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 초대한 계정
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender")
    private User sender;

    // 초대받은 계정
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver")
    private User receiver;

    // 챌린지
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge")
    private Challenge challenge;

    // 챌린지 초대 알림
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification")
    private Notification notification;
}
