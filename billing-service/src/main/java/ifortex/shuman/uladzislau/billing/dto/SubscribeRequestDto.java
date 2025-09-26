package ifortex.shuman.uladzislau.billing.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubscribeRequestDto {

  @NotBlank(message = "Price ID cannot be blank")
  private String priceId;
}