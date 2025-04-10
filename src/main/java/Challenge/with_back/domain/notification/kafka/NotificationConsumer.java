package Challenge.with_back.domain.notification.kafka;

import Challenge.with_back.common.response.exception.CustomException;
import Challenge.with_back.common.response.exception.CustomExceptionCode;
import Challenge.with_back.domain.notification.NotificationMessage;
import Challenge.with_back.domain.notification.service.NotificationService;
import Challenge.with_back.repository.memory.SseEmitterRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
@RequiredArgsConstructor
public class NotificationConsumer
{
    private final SseEmitterRepository sseEmitterRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "NOTIFICATION", groupId = "my-consumer-group")
    public void consumeNotification(ConsumerRecord<String, String> record, Acknowledgment ack)
    {
        NotificationMessage notificationMessage;

        // 알림 메시지 가져오기
        try {
            notificationMessage = objectMapper.readValue(record.value(), NotificationMessage.class);
            ack.acknowledge();
        } catch (Exception e) {
            throw new CustomException(CustomExceptionCode.KAFKA_NOTIFICATION_ERROR, null);
        }

        // 연결 정보 가져오기
        SseEmitter sseEmitter = sseEmitterRepository.findByUserId(notificationMessage.getUserId())
                .orElseThrow(() -> new CustomException(CustomExceptionCode.EMITTER_NOT_FOUND, notificationMessage.getUserId()));

        // 알림 전송
        try {
            sseEmitter.send(SseEmitter.event()
                    .id(notificationMessage.getNotificationId().toString())
                    .name("NOTIFICATION")
                    .data(notificationMessage));
        } catch (Exception e) {
            throw new CustomException(CustomExceptionCode.EMITTER_CONNECTION_ERROR, null);
        }
    }
}
