package ifortex.shuman.uladzislau.authservice;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.TimeZone;

@SpringBootApplication
@EnableAsync
@EnableRetry
@EnableFeignClients
@ComponentScan(
		basePackages = {
				"ifortex.shuman.uladzislau.authservice"
		})
@EnableJpaRepositories(
		basePackages = {
				"ifortex.shuman.uladzislau.authservice.repository",
				"ifortex.shuman.uladzislau.authservice.paramedic.repository"
		})
@EntityScan(basePackages = {
		"ifortex.shuman.uladzislau.authservice.model",
		"ifortex.shuman.uladzislau.authservice.paramedic.model"
})
public class AuthserviceApplication {
	@PostConstruct
	void init() {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}
	public static void main(String[] args) {
		SpringApplication.run(AuthserviceApplication.class, args);
	}
}
