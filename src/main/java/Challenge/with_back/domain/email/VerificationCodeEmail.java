package Challenge.with_back.domain.email;

public class VerificationCodeEmail extends Email
{
    VerificationCodeEmail(String subject, String content) {
        super(subject, content);
    }
}
