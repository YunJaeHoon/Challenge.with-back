package Challenge.with_back.common.entity.rdbms;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"challenge", "number"})
        }
)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class Phase extends BasicEntity
{
    @Id
    @Column(nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 챌린지
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "challenge",
            foreignKey = @ForeignKey(
                    name = "fk_phase_to_challenge",
                    foreignKeyDefinition = "FOREIGN KEY (challenge) REFERENCES challenge(id) ON DELETE CASCADE"
            )
    )
    private Challenge challenge;

    // 이름
    @NotNull
    @Column(length = 255)
    private String name;

    // 설명
    @Column(columnDefinition = "TEXT")
    private String description;

    // 번호
    @NotNull
    private int number;

    // 시작 날짜
    @NotNull
    private LocalDate startDate;

    // 종료 날짜
    @NotNull
    private LocalDate endDate;
}
