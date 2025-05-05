package Challenge.with_back.common.repository.redis;

import Challenge.with_back.common.entity.redis.CheckVerificationCode;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CheckVerificationCodeRepository extends CrudRepository<CheckVerificationCode, String>
{
    Optional<CheckVerificationCode> findByEmail(String email);
}
