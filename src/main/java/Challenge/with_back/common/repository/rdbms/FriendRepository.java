package Challenge.with_back.common.repository.rdbms;

import Challenge.with_back.common.entity.rdbms.Friend;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FriendRepository extends JpaRepository<Friend, Long> {
}
