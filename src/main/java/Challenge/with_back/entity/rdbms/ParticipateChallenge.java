package Challenge.with_back.entity.rdbms;

import Challenge.with_back.enums.ChallengeRole;
import Challenge.with_back.enums.ChallengeRoleConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(
        indexes = {
                @Index(name = "idx_userAndChallenge", columnList = "user, challenge"),
                @Index(name = "idx_user_by_createdAt", columnList = "user, created_at DESC")
        },
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user", "challenge"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ParticipateChallenge extends BasicEntity
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

    // 참가 대상 챌린지
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge")
    private Challenge challenge;

    // 각오 한마디
    @Column(columnDefinition = "TEXT")
    private String determination;

    // 역할
    @NotNull
    @Convert(converter = ChallengeRoleConverter.class)
    private ChallengeRole challengeRole;

    // 성공 횟수
    @NotNull
    private int countSuccess;

    // 면제 횟수
    @NotNull
    private int countExemption;

    // 공개인가?
    @NotNull
    private boolean isPublic;

    // 마지막 활동 날짜
    @NotNull
    private LocalDate lastActiveDate;

    @Builder
    public ParticipateChallenge(User user, Challenge challenge, String determination, ChallengeRole challengeRole, int countSuccess, int countExemption, boolean isPublic, LocalDate lastActiveDate) {
        this.user = user;
        this.challenge = challenge;
        this.determination = determination;
        this.challengeRole = challengeRole;
        this.countSuccess = countSuccess;
        this.countExemption = countExemption;
        this.isPublic = isPublic;
        this.lastActiveDate = lastActiveDate;
    }

    // 마지막 활동 날짜 갱신
    public void renewLastActiveDate() {
        this.lastActiveDate = LocalDate.now();
    }

    // 성공 개수 1개 증가
    public void increaseCountSuccess() {
        this.countSuccess++;
    }

    // 성공 개수 1개 감소
    public void decreaseCountSuccess() {
        this.countSuccess--;
    }

    // 성공 개수 1개 증가
    public void increaseCountExemption() {
        this.countExemption++;
    }

    // 성공 개수 1개 감소
    public void decreaseCountExemption() {
        this.countExemption--;
    }
}
