package ifortex.shuman.uladzislau.billing.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubscribeResponseDto {

  private String redirectUrl;
}