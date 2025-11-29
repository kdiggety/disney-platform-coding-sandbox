package interview.identity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableConfigurationProperties
@ConfigurationPropertiesScan
public class App {
  public static void main(String[] args) {
    SpringApplication.run(App.class, args);
  }

  @RestController
  static class HealthController {
    @GetMapping("/healthz")
    public String health() {
      return "ok";
    }
  }
}
