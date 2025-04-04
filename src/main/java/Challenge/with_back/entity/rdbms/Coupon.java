package Challenge.with_back.entity.rdbms;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Coupon extends BasicEntity
{
    @Id
    @Column(nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 계정
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user")
    private User user;

    // 이름
    @NotNull
    @Column(length = 255)
    private String name;

    // 설명
    @Column(columnDefinition = "TEXT")
    private String description;

    // 만료 날짜
    @NotNull
    private LocalDate expirationDate;

    @Builder
    public Coupon(User user, String name, String description, LocalDate expirationDate) {
        this.user = user;
        this.name = name;
        this.description = description;
        this.expirationDate = expirationDate;
    }
}
