package Challenge.with_back.domain.email;

import lombok.*;

@Getter
@Builder(access = AccessLevel.PROTECTED)
public class Email
{
    String to;
    String subject;
    String content;
}
