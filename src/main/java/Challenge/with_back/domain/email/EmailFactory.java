package Challenge.with_back.domain.email;

import Challenge.with_back.domain.email.kafka.EmailProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public abstract class EmailFactory
{
    private final EmailProducer emailProducer;
    
    // 이메일 전송
    @Transactional
    public void sendEmail(String to)
    {
        Email email = createEmail(to);
        emailProducer.send(email);
    }
    
    // 이메일  생성
    abstract Email createEmail(String to);
}
