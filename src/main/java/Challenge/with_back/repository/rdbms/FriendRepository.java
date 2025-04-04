package Challenge.with_back.repository.rdbms;

import Challenge.with_back.entity.rdbms.Friend;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FriendRepository extends JpaRepository<Friend, Long> {
}
