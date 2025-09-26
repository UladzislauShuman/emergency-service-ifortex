package ifortex.shuman.uladzislau.authservice.paramedic.service.implemention;

import ifortex.shuman.uladzislau.authservice.paramedic.model.ParamedicApplication;
import ifortex.shuman.uladzislau.authservice.paramedic.repository.ParamedicApplicationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParamedicApplicationServiceImplTest {

  @Mock
  private ParamedicApplicationRepository repository;

  @InjectMocks
  private ParamedicApplicationServiceImpl paramedicApplicationService;

  @Test
  void save_withValidApplication_shouldCallRepositorySaveAndReturnApplication() {
    ParamedicApplication applicationToSave = new ParamedicApplication();
    applicationToSave.setId(1L);

    when(repository.save(any(ParamedicApplication.class))).thenReturn(applicationToSave);

    ParamedicApplication savedApplication = paramedicApplicationService.save(applicationToSave);

    assertThat(savedApplication).isNotNull();
    assertThat(savedApplication.getId()).isEqualTo(1L);
    verify(repository).save(applicationToSave);
  }

  @Test
  void save_withNullApplication_shouldThrowIllegalArgumentException() {
    assertThatThrownBy(() -> paramedicApplicationService.save(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Application must not be null");
  }
}