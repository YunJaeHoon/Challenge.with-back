package Challenge.with_back.common.repository.rdbms;

import Challenge.with_back.common.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
}
