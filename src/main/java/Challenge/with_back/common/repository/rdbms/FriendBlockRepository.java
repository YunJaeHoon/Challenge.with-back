package Challenge.with_back.common.repository.rdbms;

import Challenge.with_back.common.entity.rdbms.FriendBlock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FriendBlockRepository extends JpaRepository<FriendBlock, Long> {
}
