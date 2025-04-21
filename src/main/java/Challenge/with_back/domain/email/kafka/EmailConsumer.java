package Challenge.with_back.domain.email.kafka;

import Challenge.with_back.domain.email.Email;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailConsumer
{
    private final ObjectMapper objectMapper;
    private final JavaMailSender javaMailSender;

    @KafkaListener(topics = "EMAIL", groupId = "my-consumer-group")
    public void consumeNotification(ConsumerRecord<String, String> record)
    {
        // 알림 메시지 가져오기
        try {
            Email email = objectMapper.readValue(record.value(), Email.class);
            
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            mimeMessageHelper.setTo(email.getTo());
            mimeMessageHelper.setSubject(email.getSubject());
            mimeMessageHelper.setText(email.getContent(), true);
            
            javaMailSender.send(mimeMessage);
        } catch (Exception ignored) {}
    }
}
