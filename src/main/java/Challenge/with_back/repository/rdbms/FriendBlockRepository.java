package Challenge.with_back.repository.rdbms;

import Challenge.with_back.entity.rdbms.FriendBlock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FriendBlockRepository extends JpaRepository<FriendBlock, Long> {
}
