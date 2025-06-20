package Challenge.with_back.common.entity.rdbms;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

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
    @Column(length = 18)
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

    @OneToMany(mappedBy = "phase", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ParticipatePhase> participatePhaseList;

    // 증거사진 최대 개수 계산
    public long countMaxEvidencePhoto() {
        return ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }
}
