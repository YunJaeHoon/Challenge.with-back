package Challenge.with_back.common.entity.rdbms;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(
        indexes = {
                @Index(name = "idx_participatePhase", columnList = "participate_phase")
        }
)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
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
    @JoinColumn(
            name = "participatePhase",
            foreignKey = @ForeignKey(
                    name = "fk_participate_phase_to_challenge",
                    foreignKeyDefinition = "FOREIGN KEY (participate_phase) REFERENCES participate_phase(id) ON DELETE CASCADE"
            )
    )
    private ParticipatePhase participatePhase;

    // 증거사진 URL
    @NotNull
    @Column(length = 255)
    @Setter
    private String photoUrl;

    // S3 이미지 파일 이름
    @NotNull
    @Column(length = 255)
    @Setter
    private String filename;
}
