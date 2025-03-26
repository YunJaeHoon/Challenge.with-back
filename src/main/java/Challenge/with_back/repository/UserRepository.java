package Challenge.with_back.repository;

import Challenge.with_back.entity.User;
import Challenge.with_back.enums.account.LoginMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>
{
    Optional<User> findByEmailAndLoginMethod(String email, LoginMethod loginMethod);
}
