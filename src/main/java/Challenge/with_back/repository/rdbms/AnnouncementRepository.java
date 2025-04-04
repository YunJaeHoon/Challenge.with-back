package Challenge.with_back.repository.rdbms;

import Challenge.with_back.entity.rdbms.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
}
