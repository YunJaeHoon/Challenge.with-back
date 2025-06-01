package Challenge.with_back.common.repository.rdbms;

import Challenge.with_back.common.entity.rdbms.Friend;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long>
{
    // 사용자1, 사용자2로 친구 데이터 조회
    Optional<Friend> findByUser1IdAndUser2Id(Long user1Id, Long user2Id);

    @Query("SELECT f FROM Friend f WHERE f.user1.id = :userId OR f.user2.id = :userId")
    Page<Friend> findPageByUserId(@Param("userId") Long userId, Pageable pageable);
}
