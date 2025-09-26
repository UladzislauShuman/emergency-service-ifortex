package ifortex.shuman.uladzislau.authservice.dto;

import ifortex.shuman.uladzislau.authservice.paramedic.dto.KycApplicationSummaryDto;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class HomePageDetailsDto {
    private UserDto user;
    private Set<String> permissions;
    private KycApplicationSummaryDto kycApplication;
}