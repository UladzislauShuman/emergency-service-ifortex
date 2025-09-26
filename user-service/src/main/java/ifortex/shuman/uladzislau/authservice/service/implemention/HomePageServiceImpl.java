package ifortex.shuman.uladzislau.authservice.service.implemention;

import ifortex.shuman.uladzislau.authservice.dto.HomePageDetailsDto;
import ifortex.shuman.uladzislau.authservice.model.User;
import ifortex.shuman.uladzislau.authservice.model.UserRole;
import ifortex.shuman.uladzislau.authservice.paramedic.dto.KycApplicationSummaryDto;
import ifortex.shuman.uladzislau.authservice.paramedic.repository.ParamedicApplicationRepository;
import ifortex.shuman.uladzislau.authservice.service.HomePageService;
import ifortex.shuman.uladzislau.authservice.service.permission.PermissionService;
import ifortex.shuman.uladzislau.authservice.util.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HomePageServiceImpl implements HomePageService {

  private final UserMapper userMapper;
  private final PermissionService permissionService;
  private final ParamedicApplicationRepository paramedicApplicationRepository;

  @Override
  public HomePageDetailsDto getHomePageDetails(User currentUser) {
    HomePageDetailsDto.HomePageDetailsDtoBuilder detailsBuilder = HomePageDetailsDto.builder()
        .user(userMapper.toUserDto(currentUser))
        .permissions(permissionService.calculatePermissionsForUser(currentUser));

    if (currentUser.getRole().getName().equals(UserRole.ROLE_PARAMEDIC)) {
      paramedicApplicationRepository.findTopByEmailOrderBySubmittedAtDesc(currentUser.getEmail())
          .ifPresent(app -> {
            KycApplicationSummaryDto summaryDto = KycApplicationSummaryDto.builder()
                .status(app.getStatus())
                .submittedAt(app.getSubmittedAt())
                .reviewedAt(app.getReviewedAt())
                .rejectionReason(app.getRejectionReason())
                .build();
            detailsBuilder.kycApplication(summaryDto);
          });
    }

    return detailsBuilder.build();
  }
}