package Challenge.with_back.repository;

import Challenge.with_back.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long>
{
    Optional<VerificationCode> findByEmail(String email);
}
