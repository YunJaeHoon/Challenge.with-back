package Challenge.with_back.repository.rdbms;

import Challenge.with_back.entity.rdbms.Phase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhaseRepository extends JpaRepository<Phase, Long> {
}
