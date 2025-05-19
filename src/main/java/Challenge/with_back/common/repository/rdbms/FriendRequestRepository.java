package Challenge.with_back.common.repository.rdbms;

import Challenge.with_back.common.entity.rdbms.FriendRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
}
