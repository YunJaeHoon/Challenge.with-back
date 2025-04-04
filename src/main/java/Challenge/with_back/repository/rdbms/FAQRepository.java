package Challenge.with_back.repository.rdbms;

import Challenge.with_back.entity.rdbms.FAQ;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FAQRepository extends JpaRepository<FAQ, Long> {
}
