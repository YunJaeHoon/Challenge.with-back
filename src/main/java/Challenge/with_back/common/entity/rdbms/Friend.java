package Challenge.with_back.common.entity.rdbms;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(
        indexes = {
                @Index(name = "idx_user1AndUser2", columnList = "user1, user2"),
                @Index(name = "idx_user2AndUser1", columnList = "user2, user1"),
        },
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user1", "user2"})
        }
)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class Friend extends BasicEntity
{
    @Id
    @Column(nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 계정1
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user1")
    private User user1;

    // 계정2
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user2")
    private User user2;
}
