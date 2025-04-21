package Challenge.with_back.domain.email.kafka;

import Challenge.with_back.domain.email.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailProducer
{
    private final KafkaTemplate<String, Email> notificationKafkaTemplate;

    public void send(Email email)
    {
        notificationKafkaTemplate.send("EMAIL", email);;
    }
}
