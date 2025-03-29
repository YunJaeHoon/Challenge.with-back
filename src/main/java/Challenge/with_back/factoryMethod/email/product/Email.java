package Challenge.with_back.factoryMethod.email.product;

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
