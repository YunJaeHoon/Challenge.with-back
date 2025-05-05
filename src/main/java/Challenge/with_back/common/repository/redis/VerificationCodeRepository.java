package Challenge.with_back.common.repository.redis;

import Challenge.with_back.common.entity.redis.VerificationCode;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationCodeRepository extends CrudRepository<VerificationCode, String>
{
    Optional<VerificationCode> findByEmail(String email);
}
