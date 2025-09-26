package ifortex.shuman.uladzislau.authservice.paramedic.service.implemention;

import ifortex.shuman.uladzislau.authservice.paramedic.model.ParamedicApplication;
import ifortex.shuman.uladzislau.authservice.paramedic.repository.ParamedicApplicationRepository;
import ifortex.shuman.uladzislau.authservice.paramedic.service.ParamedicApplicationService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ParamedicApplicationServiceImpl implements ParamedicApplicationService {

  private final ParamedicApplicationRepository repository;

  @Override
  public ParamedicApplication save(ParamedicApplication application) {
    return Optional.ofNullable(application)
        .map(repository::save)
        .orElseThrow(() -> new IllegalArgumentException("Application must not be null"));
  }
}
