package Challenge.with_back.entity.rdbms;

import Challenge.with_back.enums.InquiryCategory;
import Challenge.with_back.enums.InquiryCategoryConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Inquiry extends BasicEntity
{
    @Id
    @Column(nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 문의 계정
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user")
    private User user;

    // 분류
    @NotNull
    @Convert(converter = InquiryCategoryConverter.class)
    private InquiryCategory category;

    // 질문
    @NotNull
    @Column(columnDefinition = "TEXT")
    private String question;

    // 답변
    @NotNull
    @Column(columnDefinition = "TEXT")
    private String answer;

    // 답변 날짜
    @NotNull
    private LocalDateTime answerDate;

    @Builder
    public Inquiry(User user, InquiryCategory category, String question, String answer, LocalDateTime answerDate) {
        this.user = user;
        this.category = category;
        this.question = question;
        this.answer = answer;
        this.answerDate = answerDate;
    }
}
