package ifortex.shuman.uladzislau.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class PlanDto {

  private String priceId;
  private String productName;
  private BigDecimal amount;
  private String currency;
  private String interval;
}