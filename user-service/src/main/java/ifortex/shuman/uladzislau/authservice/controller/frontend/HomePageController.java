package ifortex.shuman.uladzislau.authservice.controller.frontend;

import ifortex.shuman.uladzislau.authservice.dto.HomePageDetailsDto;
import ifortex.shuman.uladzislau.authservice.model.User;
import ifortex.shuman.uladzislau.authservice.service.HomePageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class HomePageController {

    private final HomePageService homePageService;

    @GetMapping("/details")
    public ResponseEntity<HomePageDetailsDto> getHomePageDetails(@AuthenticationPrincipal User currentUser) {
        HomePageDetailsDto details = homePageService.getHomePageDetails(currentUser);
        return ResponseEntity.ok(details);
    }
}