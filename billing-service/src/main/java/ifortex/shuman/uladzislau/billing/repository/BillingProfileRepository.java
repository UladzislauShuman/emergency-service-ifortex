package ifortex.shuman.uladzislau.billing.repository;

import ifortex.shuman.uladzislau.billing.model.BillingProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BillingProfileRepository extends JpaRepository<BillingProfile, Long> {

  Optional<BillingProfile> findByUserId(Long userId);
}