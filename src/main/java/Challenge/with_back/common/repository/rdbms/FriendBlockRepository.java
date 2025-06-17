package Challenge.with_back.common.repository.rdbms;

import Challenge.with_back.common.entity.rdbms.FriendBlock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FriendBlockRepository extends JpaRepository<FriendBlock, Long>
{
    // 차단한 사용자, 차단된 사용자로 친구 차단 데이터 조회
    Optional<FriendBlock> findByBlockingUserIdAndBlockedUserId(Long blockingUserId, Long blockedUserId);

    // 차단한 사용자로 친구 차단 데이터 조회
    Page<FriendBlock> findByBlockingUserId(Long blockingUserId, Pageable pageable);
}
