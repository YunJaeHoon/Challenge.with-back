package Challenge.with_back.common.entity.rdbms;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.util.List;

@Entity
@Table(
        indexes = {
                @Index(name = "idx_userAndPhase", columnList = "user, phase")
        },
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user", "phase"})
        }
)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class ParticipatePhase extends BasicEntity
{
    @Id
    @Column(nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 참가 계정
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user")
    private User user;

    // 참가 대상 챌린지 페이즈
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "phase",
            foreignKey = @ForeignKey(
                    name = "fk_participate_phase_to_phase",
                    foreignKeyDefinition = "FOREIGN KEY (phase) REFERENCES phase(id) ON DELETE CASCADE"
            )
    )
    private Phase phase;

    // 현재 개수
    @Column(columnDefinition = "SMALLINT")
    @ColumnDefault("0")
    private int currentCount;

    // 면제 여부
    @NotNull
    private boolean isExempt;

    // 한마디
    @Column(columnDefinition = "TEXT")
    private String comment;

    @OneToMany(mappedBy = "participatePhase", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EvidencePhoto> evidencePhotoList;

    // 한마디 수정
    public void updateComment(String comment) {
        this.comment = comment;
    }

    // 현재 개수 1 증가
    public void updateCurrentCount(int value) {
        this.currentCount = value;
    }

    // 면제 여부 토글
    public void toggleIsExempt() {
        this.isExempt = !this.isExempt;
    }
}
