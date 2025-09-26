package ifortex.shuman.uladzislau.billing.repository;

import ifortex.shuman.uladzislau.billing.model.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PlanRepository extends JpaRepository<Plan, Integer> {

  Optional<Plan> findByPlanCode(String planCode);
}