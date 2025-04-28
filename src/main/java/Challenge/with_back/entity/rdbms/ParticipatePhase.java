package Challenge.with_back.entity.rdbms;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        indexes = {
                @Index(name = "idx_userAndPhase", columnList = "user, phase")
        },
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user", "phase"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
    @JoinColumn(name = "phase")
    private Phase phase;

    // 현재 개수
    @Column(columnDefinition = "SMALLINT")
    private int currentCount;

    // 면제 여부
    @NotNull
    private boolean isExempt;

    // 한마디
    @Column(columnDefinition = "TEXT")
    private String comment;

    // 증거 사진 개수
    @NotNull
    @Column(columnDefinition = "TINYINT")
    private int countEvidencePhoto;

    @Builder
    public ParticipatePhase(User user, Phase phase, int currentCount, boolean isExempt, String comment, int countEvidencePhoto) {
        this.user = user;
        this.phase = phase;
        this.currentCount = currentCount;
        this.isExempt = isExempt;
        this.comment = comment;
        this.countEvidencePhoto = countEvidencePhoto;
    }

    // 증거 사진 개수 증가
    public void increaseCountEvidencePhoto(int value) {
        this.countEvidencePhoto += value;
    }

    // 증거 사진 개수 1개 감소
    public void decreaseCountEvidencePhoto() {
        this.countEvidencePhoto--;
    }

    // 한마디 수정
    public void updateComment(String comment) {
        this.comment = comment;
    }

    // 현재 개수 1 증가
    public void increaseCurrentCount() {
        this.currentCount++;
    }

    // 현재 개수 1 감소
    public void decreaseCurrentCount() {
        this.currentCount--;
    }
}
