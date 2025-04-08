package Challenge.with_back.repository.rdbms;

import Challenge.with_back.entity.rdbms.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long>
{
    Page<Notification> findAllByUserId(Long userId, Pageable pageable);
}
