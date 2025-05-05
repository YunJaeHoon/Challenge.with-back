package Challenge.with_back.common.repository.rdbms;

import Challenge.with_back.common.entity.rdbms.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
}
