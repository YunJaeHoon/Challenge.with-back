package Challenge.with_back.common.repository.rdbms;

import Challenge.with_back.common.entity.rdbms.FAQ;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FAQRepository extends JpaRepository<FAQ, Long> {
}
