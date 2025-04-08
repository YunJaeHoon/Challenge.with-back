package Challenge.with_back.domain.email;

import Challenge.with_back.dto.response.CustomExceptionCode;
import Challenge.with_back.exception.CustomException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public interface EmailFactory
{
    // 이메일  생성
    Email createEmail(String to);

    // 이메일 전송
    @Async
    default void sendEmail(JavaMailSender javaMailSender, String to, Email email)
    {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();

            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setSubject(email.getSubject());
            mimeMessageHelper.setText(email.getContent(), true);

            javaMailSender.send(mimeMessage);
        } catch(Exception e) {
            throw new CustomException(CustomExceptionCode.SEND_EMAIL_ERROR, e.getMessage());
        }
    }
}
