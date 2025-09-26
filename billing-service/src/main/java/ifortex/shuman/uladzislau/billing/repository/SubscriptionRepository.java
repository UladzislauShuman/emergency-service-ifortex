package ifortex.shuman.uladzislau.billing.repository;

import ifortex.shuman.uladzislau.billing.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

  @Query("SELECT s FROM Subscription s WHERE s.userId = :userId AND s.status = 'ACTIVE' AND s.endsAt > CURRENT_TIMESTAMP")
  Optional<Subscription> findActiveSubscriptionByUserId(@Param("userId") Long userId);
}