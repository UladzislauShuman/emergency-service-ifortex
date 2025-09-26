package ifortex.shuman.uladzislau.authservice.dto;

import java.io.Serializable;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailNotificationDto implements Serializable {

  private String to;
  private String subjectKey;
  private String bodyTemplateKey;
  private Map<String, Object> templateModel;
}