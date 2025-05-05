package Challenge.with_back.common.repository.rdbms;

import Challenge.with_back.common.entity.rdbms.User;
import Challenge.with_back.common.enums.LoginMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>
{
    // 이메일과 로그인 방법으로 사용자 조회
    Optional<User> findByEmailAndLoginMethod(String email, LoginMethod loginMethod);
}
