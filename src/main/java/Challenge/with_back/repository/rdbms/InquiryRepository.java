package Challenge.with_back.repository.rdbms;

import Challenge.with_back.entity.rdbms.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
}
