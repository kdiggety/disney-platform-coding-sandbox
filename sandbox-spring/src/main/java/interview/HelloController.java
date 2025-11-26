package interview;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class HelloController {

  @GetMapping("/hello")
  String hello() {
    return "ok";
  }
}
