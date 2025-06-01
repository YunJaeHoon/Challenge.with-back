package Challenge.with_back.common.repository.rdbms;

import Challenge.with_back.common.entity.rdbms.Friend;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long>
{
    // 사용자1, 사용자2로 친구 데이터 조회
    Optional<Friend> findByUser1IdAndUser2Id(Long user1Id, Long user2Id);
}
