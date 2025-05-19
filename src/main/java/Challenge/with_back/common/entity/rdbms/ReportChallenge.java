package Challenge.with_back.common.entity.rdbms;

import Challenge.with_back.common.enums.ChallengeReportCategory;
import Challenge.with_back.common.enums.ChallengeReportCategoryConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class ReportChallenge extends BasicEntity
{
    @Id
    @Column(nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 신고자 계정
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user")
    private User user;

    // 신고 대상 챌린지
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge")
    private Challenge challenge;

    // 분류
    @NotNull
    @Convert(converter = ChallengeReportCategoryConverter.class)
    private ChallengeReportCategory category;

    // 세부 내용
    @Column(columnDefinition = "TEXT")
    private String detail;
}
