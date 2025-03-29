package Challenge.with_back.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class Email
{
    String subject;
    String content;
}
