package Challenge.with_back.entity;

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
public class Phase extends BasicEntity
{
    @Id
    @Column(nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 챌린지
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge")
    private Challenge challenge;

    // 이름
    @NotNull
    @Column(length = 255)
    private String name;

    // 설명
    @Column(columnDefinition = "TEXT")
    private String description;

    // 번호
    @NotNull
    private int number;

    // 시작 날짜
    @NotNull
    private LocalDate startDate;

    @Builder
    public Phase(Challenge challenge, String name, String description, int number, LocalDate startDate) {
        this.challenge = challenge;
        this.name = name;
        this.description = description;
        this.number = number;
        this.startDate = startDate;
    }
}
