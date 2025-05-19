package Challenge.with_back.common.entity.rdbms;

import Challenge.with_back.common.enums.InquiryCategory;
import Challenge.with_back.common.enums.InquiryCategoryConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
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
}
