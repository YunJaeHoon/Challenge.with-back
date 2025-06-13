package Challenge.with_back.common.entity.rdbms;

import Challenge.with_back.common.enums.LoginMethod;
import Challenge.with_back.common.enums.LoginMethodConverter;
import Challenge.with_back.common.enums.AccountRole;
import Challenge.with_back.common.enums.AccountRoleConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Entity
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
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

    // 프리미엄인지 확인
    public boolean isPremium() {
        return premiumExpirationDate.isAfter(LocalDate.now());
    }

    // 참여 가능한 챌린지 최대 개수 확인
    public int getMaxChallengeCount() {
        return isPremium() ? 20 : 3;
    }
}
