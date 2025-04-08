package Challenge.with_back.domain.notification;

import Challenge.with_back.entity.rdbms.User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public interface NotificationFactory
{
    // 알림 엔티티 및 메시지 생성
    NotificationMessage createNotification(User user);
}
