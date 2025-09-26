package ifortex.shuman.uladzislau.authservice.paramedic.repository;

import ifortex.shuman.uladzislau.authservice.paramedic.model.ParamedicApplication;
import ifortex.shuman.uladzislau.authservice.paramedic.model.ParamedicApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;

public interface ParamedicApplicationRepository extends JpaRepository<ParamedicApplication, Long> {

  Page<ParamedicApplication> findAllByStatus(ParamedicApplicationStatus status, Pageable pageable);

  Optional<ParamedicApplication> findTopByEmailOrderBySubmittedAtDesc(String email);

  boolean existsByEmailAndStatus(String email, ParamedicApplicationStatus status);

  Optional<ParamedicApplication> findByEmailAndStatusIn(String email,
      Set<ParamedicApplicationStatus> statuses);

  Optional<ParamedicApplication> findByEmailAndStatus(String email,
      ParamedicApplicationStatus status);
}