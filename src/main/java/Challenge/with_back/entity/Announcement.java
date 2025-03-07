package Challenge.with_back.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

// 공지사항
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Announcement extends BasicEntity
{
    @Id
    @Column(nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 제목
    @NotNull
    @Column(length = 255)
    private String title;

    // 내용
    @NotNull
    @Column(columnDefinition = "TEXT")
    private String content;

    @Builder
    public Announcement(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
