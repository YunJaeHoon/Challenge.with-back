package Challenge.with_back.common.repository.rdbms;

import Challenge.with_back.common.entity.rdbms.Friend;
import Challenge.with_back.common.entity.rdbms.FriendRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long>
{
    Optional<FriendRequest> findBySenderIdAndReceiverId(Long senderId, Long receiverId);
}
