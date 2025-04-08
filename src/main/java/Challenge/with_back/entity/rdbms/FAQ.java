package Challenge.with_back.entity.rdbms;

import Challenge.with_back.common.enums.InquiryCategory;
import Challenge.with_back.common.enums.InquiryCategoryConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 자주 묻는 질문
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class FAQ extends BasicEntity
{
    @Id
    @Column(nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Builder
    public FAQ(InquiryCategory category, String question, String answer) {
        this.category = category;
        this.question = question;
        this.answer = answer;
    }
}
