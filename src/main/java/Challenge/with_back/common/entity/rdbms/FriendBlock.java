package Challenge.with_back.common.entity.rdbms;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class FriendBlock extends BasicEntity
{
    @Id
    @Column(nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 차단한 계정
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocking_user")
    private User blockingUser;

    // 차단된 계정
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_user")
    private User blockedUser;
}
