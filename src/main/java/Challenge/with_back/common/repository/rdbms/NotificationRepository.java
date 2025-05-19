package Challenge.with_back.common.repository.rdbms;

import Challenge.with_back.common.entity.rdbms.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long>
{
    // 알림 페이지 조회
    Page<Notification> findPageByUserId(Long userId, Pageable pageable);
}
