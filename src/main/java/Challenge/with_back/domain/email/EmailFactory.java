package Challenge.with_back.domain.email;

import Challenge.with_back.response.exception.CustomException;
import Challenge.with_back.response.exception.CustomExceptionCode;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public abstract class EmailFactory
{
    private final JavaMailSender javaMailSender;
    
    // 이메일 전송
    @Transactional
    @Async
    public void sendEmail(String to)
    {
        // 이메일 생성
        Email email = createEmail(to);

        // 이메일 전송
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();

            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            mimeMessageHelper.setTo(email.getTo());
            mimeMessageHelper.setSubject(email.getSubject());
            mimeMessageHelper.setText(email.getContent(), true);

            javaMailSender.send(mimeMessage);
        } catch (Exception exception) {
            throw new CustomException(CustomExceptionCode.SEND_EMAIL_ERROR, null);
        }
    }
    
    // 이메일  생성
    abstract Email createEmail(String to);
}
