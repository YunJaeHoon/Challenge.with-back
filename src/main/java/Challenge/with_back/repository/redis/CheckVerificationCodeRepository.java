package Challenge.with_back.repository.redis;

import Challenge.with_back.entity.redis.CheckVerificationCode;
import Challenge.with_back.entity.redis.VerificationCode;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CheckVerificationCodeRepository extends CrudRepository<CheckVerificationCode, String>
{
    Optional<CheckVerificationCode> findByEmail(String email);
}
