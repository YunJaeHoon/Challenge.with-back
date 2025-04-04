package Challenge.with_back.repository.rdbms;

import Challenge.with_back.entity.rdbms.ReportChallenge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportChallengeRepository extends JpaRepository<ReportChallenge, Long> {
}
