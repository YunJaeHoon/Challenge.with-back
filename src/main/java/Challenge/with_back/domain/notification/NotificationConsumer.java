package Challenge.with_back.domain.notification;

import Challenge.with_back.common.response.exception.CustomException;
import Challenge.with_back.common.response.exception.CustomExceptionCode;
import Challenge.with_back.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationConsumer
{
    private final NotificationService notificationService;;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "NOTIFICATION", groupId = "my-consumer-group")
    public void consumeNotification(ConsumerRecord<String, String> record, Acknowledgment ack)
    {
        try {
            NotificationMessage notificationMessage = objectMapper.readValue(record.value(), NotificationMessage.class);
            notificationService.send(notificationMessage.getUserId(), notificationMessage);
            ack.acknowledge();
        } catch (Exception e) {
            throw new CustomException(CustomExceptionCode.KAFKA_NOTIFICATION_ERROR, null);
        }
    }
}
