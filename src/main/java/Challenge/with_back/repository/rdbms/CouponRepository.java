package Challenge.with_back.repository.rdbms;

import Challenge.with_back.entity.rdbms.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
}
