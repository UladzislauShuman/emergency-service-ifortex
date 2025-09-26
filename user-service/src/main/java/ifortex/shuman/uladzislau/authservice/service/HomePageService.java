package ifortex.shuman.uladzislau.authservice.service;

import ifortex.shuman.uladzislau.authservice.dto.HomePageDetailsDto;
import ifortex.shuman.uladzislau.authservice.model.User;

public interface HomePageService {

  HomePageDetailsDto getHomePageDetails(User currentUser);
}