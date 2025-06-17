package Challenge.with_back.common.repository.rdbms;

import Challenge.with_back.common.entity.rdbms.FriendRequest;
import Challenge.with_back.common.entity.rdbms.InviteChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InviteChallengeRepository extends JpaRepository<InviteChallenge, Long>
{
    // 송신자 ID 및 수신자 ID로 친구 요청 데이터 조회
    Optional<InviteChallenge> findBySenderIdAndReceiverIdAndChallengeId(Long senderId, Long receiverId, Long challengeId);
}
