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
                @Index(name = "idx_participatePhase", columnList = "participate_phase")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class EvidencePhoto extends BasicEntity
{
    @Id
    @Column(nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 챌린지 페이즈 참가
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participate_phase")
    private ParticipatePhase participatePhase;

    // 증거 사진 URL
    @NotNull
    @Column(length = 255)
    private String photoUrl;

    @Builder
    public EvidencePhoto(ParticipatePhase participatePhase, String photoUrl) {
        this.participatePhase = participatePhase;
        this.photoUrl = photoUrl;
    }
}
