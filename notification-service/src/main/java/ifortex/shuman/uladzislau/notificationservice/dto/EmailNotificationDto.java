package ifortex.shuman.uladzislau.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailNotificationDto implements Serializable {

  private String to;
  private String subjectKey;
  private String bodyTemplateKey;
  private Map<String, Object> templateModel;
}