package Challenge.with_back.common.repository.rdbms;

import Challenge.with_back.common.entity.rdbms.FriendRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long>
{
    // 송신자 ID 및 수신자 ID로 친구 요청 데이터 조회
    Optional<FriendRequest> findBySenderIdAndReceiverId(Long senderId, Long receiverId);

    // 특정 시점 이전의 친구 요청 데이터 페이지 조회
    Page<FriendRequest> findByCreatedAtBefore(LocalDateTime localDateTime, Pageable pageable);
}
