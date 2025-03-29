package Challenge.with_back.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 인증 코드
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class VerificationCode extends BasicEntity
{
    @Id
    @Column(nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 이메일
    @NotNull
    @Column(length = 255)
    private String email;

    // 인증 번호
    @NotNull
    @Column(columnDefinition = "char(8)")
    private String code;

    // 틀린 개수
    @NotNull
    @Column(columnDefinition = "TINYINT")
    private int countWrong;

    @Builder
    public VerificationCode(String email, String code, int countWrong) {
        this.email = email;
        this.code = code;
        this.countWrong = countWrong;
    }
}
