package Challenge.with_back.entity.rdbms;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    @Builder
    public FriendBlock(User blockingUser, User blockedUser) {
        this.blockingUser = blockingUser;
        this.blockedUser = blockedUser;
    }
}
