package Challenge.with_back.common.repository.rdbms;

import Challenge.with_back.common.entity.ReportChallenge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportChallengeRepository extends JpaRepository<ReportChallenge, Long> {
}
