package Challenge.with_back.repository.rdbms;

import Challenge.with_back.entity.rdbms.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
