package Challenge.with_back.domain.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationProducer
{
    private final KafkaTemplate<String, NotificationMessage> notificationKafkaTemplate;

    public void send(NotificationMessage notificationMessage)
    {
        notificationKafkaTemplate.send("NOTIFICATION", notificationMessage);;
    }
}
