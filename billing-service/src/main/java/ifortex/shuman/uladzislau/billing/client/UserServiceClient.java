package ifortex.shuman.uladzislau.billing.client;

import ifortex.shuman.uladzislau.billing.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "${app.services.user-service.url}")
public interface UserServiceClient {

    @GetMapping("/api/internal/users/{userId}")
    UserDto getUserById(@PathVariable("userId") Long userId);
}