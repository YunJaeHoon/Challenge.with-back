package Challenge.with_back.entity.rdbms;

import Challenge.with_back.enums.LoginMethod;
import Challenge.with_back.enums.LoginMethodConverter;
import Challenge.with_back.enums.AccountRole;
import Challenge.with_back.enums.AccountRoleConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class User extends BasicEntity
{
    @Id
    @Column(nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 로그인 방식
    @NotNull
    @Convert(converter = LoginMethodConverter.class)
    private LoginMethod loginMethod;

    // 이메일
    @NotNull
    @Email
    @Column(length = 255)
    private String email;

    // 비밀번호
    @Column(length = 255)
    private String password;

    // 닉네임
    @NotNull
    @Column(length = 50)
    private String nickname;

    // 프로필 이미지 URL
    @NotNull
    @Column(length = 255)
    private String profileImageUrl;

    // 자기소개
    @NotNull
    @Column(columnDefinition = "TEXT")
    private String selfIntroduction;

    // 마케팅 이메일 수신에 동의했는가?
    @NotNull
    private boolean allowEmailMarketing;

    // 프리미엄 만료 날짜
    @NotNull
    private LocalDate premiumExpirationDate;

    // 읽지 않은 알림 개수
    @NotNull
    private int countUnreadNotification;

    // 결제 정보 수신 이메일
    @NotNull
    @Column(length = 255)
    private String paymentInformationEmail;

    // 권한
    @NotNull
    @Convert(converter = AccountRoleConverter.class)
    private AccountRole accountRole;
    
    // 참여 중인 챌린지 개수
    @NotNull
    private int countParticipateChallenge;

    @Builder
    public User(LoginMethod loginMethod, String email, String password, String nickname, String profileImageUrl, String selfIntroduction, boolean allowEmailMarketing, LocalDate premiumExpirationDate, int countUnreadNotification, String paymentInformationEmail, AccountRole accountRole, int countParticipateChallenge) {
        this.loginMethod = loginMethod;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.selfIntroduction = selfIntroduction;
        this.allowEmailMarketing = allowEmailMarketing;
        this.premiumExpirationDate = premiumExpirationDate;
        this.countUnreadNotification = countUnreadNotification;
        this.paymentInformationEmail = paymentInformationEmail;
        this.accountRole = accountRole;
        this.countParticipateChallenge = countParticipateChallenge;
    }

    // 비밀번호 초기화
    public void resetPassword(String newPassword) {
        this.password = newPassword;
    }

    // 읽지 않은 알림 개수 1개 증가
    public void increaseCountUnreadNotification() {
        this.countUnreadNotification++;
    }
    
    // 읽지 않은 알림 개수 1개 감소
    public void decreaseCountUnreadNotification() {
        this.countUnreadNotification--;
    }

    // 참여 중인 챌린지 개수 1개 증가
    public void increaseCountParticipateChallenge() {
        this.countParticipateChallenge++;
    }

    // 참여 중인 챌린지 개수 1개 감소
    public void decreaseCountParticipateChallenge() {
        this.countParticipateChallenge--;
    }
}
