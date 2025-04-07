package Challenge.with_back.domain.email;

import lombok.*;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder(access = AccessLevel.PROTECTED)
public class Email
{
    String subject;
    String content;
}
