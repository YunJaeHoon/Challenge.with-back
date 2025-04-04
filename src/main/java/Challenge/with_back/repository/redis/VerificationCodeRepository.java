package Challenge.with_back.repository.redis;

import Challenge.with_back.entity.redis.VerificationCode;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface VerificationCodeRepository extends CrudRepository<VerificationCode, String>
{
    Optional<VerificationCode> findByEmail(String email);
}
